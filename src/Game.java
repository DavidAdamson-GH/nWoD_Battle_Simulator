import java.util.Scanner;

import ai.BattlerAI;
import ai.RandomBattler;

/**
 * Contains the game logic
 * 
 * @author David Adamson
 *
 */
public class Game {

	public static void main(String[] args) {
		WoDCharacter player = new BasicPlayerCharacter();
		WoDCharacter enemy = new BasicPlayerCharacter();
		BattlerAI enemy_ai = new RandomBattler();
		
		Scanner scan = new Scanner(System.in);
		/* The game begins */
		while(true){
			/* Each turn, player and enemy HP is displayed */
			System.out.println("Player HP: "+player.getCurrentHealth()+"     Willpower: "+player.getCurrentWillpower());
			System.out.println("Enemy HP: "+enemy.getCurrentHealth()+"     Willpower: "+enemy.getCurrentWillpower());
			System.out.println();
			
			/* Check if any character is dead yet */
			byte death_code = checkForDeath(player.getCurrentHealth(), enemy.getCurrentHealth());
			player.setCurrentDefense(player.getDefense());
			enemy.setCurrentDefense(enemy.getDefense());
			switch(death_code){
			case 1 :
				System.out.println("The game ends in a draw!");
				return;
			case 2 :
				System.out.println("The enemy wins...");
				return;
			case 3 :
				System.out.println("You win!");
				return;
			}
			
			/* Player goes first. This might change in a future update. */
			System.out.println("Enter M for melee, or R for ranged");
			String input;
			boolean willpower_spent = false;
			boolean allout_attack = false;
			do{
				/* Clean up the input, and only accept M or R */
				input = scan.nextLine().trim().toUpperCase();
			}while(!input.equals("M") && !input.equals("R"));
			if(player.getCurrentWillpower() > 0){
				System.out.println("Would you like to spend a Willpower point? (Y/N)");
				String willpower_decision;
				do{
					willpower_decision = scan.nextLine().trim().toUpperCase();
				}while(!willpower_decision.equals("Y") && !willpower_decision.equals("N"));
				if(willpower_decision.equals("Y")){
					player.setCurrentWillpower(player.getCurrentWillpower() - 1);
					willpower_spent = true;
				}
			}
			if(input.equals("M")){
				System.out.println("Would you like to make an All-Out Attack?");
				String allout_decision;
				do{
					allout_decision = scan.nextLine().trim().toUpperCase();
				}while(!allout_decision.equals("Y") && !allout_decision.equals("N"));
				if(allout_decision.equals("Y")){
					allout_attack = true;
					player.setCurrentDefense(0);
				}
				/* To hit with a melee weapon, you must get at least 1 success with 
				 * Dexterity + Weaponry - Enemy_Defense 
				 */
				int successes = DiceRoller.getSuccesses(player.getDexterity() + player.getWeaponry() - enemy.getDefense());
				if(willpower_spent){
					/* Spending Willpower allows for 3 more dice while rolling to hit */
					successes += DiceRoller.getSuccesses(3);
				}
				if(successes > 0){
					System.out.println("You hit!");
					/* Damage is determined by making a Strength roll, then adding the default weapon damage */
					int damage = 1 + DiceRoller.getSuccesses(player.getStrength());
					if(allout_attack){
						/* All-Out Attack allows for 2 more dice while rolling to damage */
						damage += DiceRoller.getSuccesses(2);
					}
					/* Enemy's melee armor value is subtracted from damage */
					int final_damage = damage - enemy.getMeleeArmor();
					if(final_damage <= (damage * -1)){
						/* Final damage is made 0 if enemy's armor is enough to make the final damage
						 * a completely inverted version of the preliminary damage
						 * eg. 2 -> -2 or 3 -> -3
						 */
						final_damage = 0;
					}else if(final_damage < 1){
						/* Damage is usually a minimum of 1 unless enemy armor is especially strong */
						final_damage = 1;
					}
					System.out.println("You inflicted "+final_damage+" damage!");
					enemy.setCurrentHealth(enemy.getCurrentHealth() - final_damage);
				}else{
					System.out.println("You missed!");
				}
			}else if(input.equals("R")){
				/* To hit with a ranged weapon, you must get at least 1 success with 
				 * Dexterity + Firearms 
				 */
				int successes = DiceRoller.getSuccesses(player.getDexterity() + player.getFirearms());
				if(willpower_spent){
					/* Spending Willpower allows for 3 more dice while rolling to hit */
					successes += DiceRoller.getSuccesses(3);
				}
				if(successes > 0){
					System.out.println("You hit!");
					/* Ranged weapons have constant damage values */
					int damage = 2;
					/* Enemy's ranged armor value is subtracted from damage */
					int final_damage = damage - enemy.getRangedArmor();
					/* Same damage rules apply as for melee attacks */
					if(final_damage <= (enemy.getRangedArmor() * -1)){
						final_damage = 0;
					}else if(final_damage < 1){
						final_damage = 1;
					}
					System.out.println("You inflicted "+final_damage+" damage!");
					enemy.setCurrentHealth(enemy.getCurrentHealth() - final_damage);
				}else{
					System.out.println("You missed!");
				}
			}
			
			/* Check if the enemy is dead - the enemy can't make a move if it's dead! */
			if(enemy.getCurrentHealth() <= 0){
				/* Skip to the next turn, where the game will end */
				continue;
			}
			
			/* Let the AI decide the enemy's move */
			String enemy_move = enemy_ai.getDecision(player.getCurrentHealth(), enemy.getCurrentHealth());
			
			if(enemy_move.equals("M")){
				int successes = DiceRoller.getSuccesses(enemy.getDexterity() + enemy.getWeaponry() - player.getDefense());
				if(successes > 0){
					System.out.println("The enemy hit!");
					int damage = 1 + DiceRoller.getSuccesses(enemy.getStrength());
					/* Player's melee armor value is subtracted from damage */
					int final_damage = damage - player.getMeleeArmor();
					if(final_damage <= (damage * -1)){
						/* Final damage is made 0 if player's armor is enough to make the final damage
						 * a completely inverted version of the preliminary damage
						 * eg. 2 -> -2 or 3 -> -3
						 */
						final_damage = 0;
					}else if(final_damage < 1){
						/* Damage is usually a minimum of 1 unless enemy armor is especially strong */
						final_damage = 1;
					}
					System.out.println("The enemy inflicted "+final_damage+" damage!");
					player.setCurrentHealth(player.getCurrentHealth() - final_damage);
				}else{
					System.out.println("The enemy missed!");
				}
			}else if(enemy_move.equals("R")){
				int successes = DiceRoller.getSuccesses(enemy.getDexterity() + enemy.getFirearms());
				if(successes > 0){
					System.out.println("The enemy hit!");
					/* Ranged weapons have constant damage values */
					int damage = 2;
					/* Player's ranged armor value is subtracted from damage */
					int final_damage = damage - player.getRangedArmor();
					if(final_damage <= (player.getRangedArmor() * -1)){
						final_damage = 0;
					}else if(final_damage < 1){
						final_damage = 1;
					}
					System.out.println("The enemy inflicted "+final_damage+" damage!");
					player.setCurrentHealth(player.getCurrentHealth() - final_damage);
				}else{
					System.out.println("The enemy missed!");
				}
			}
		}
	}
	
	/**
	 * Checks if a character is dead, and returns a code detailing the results
	 * 
	 * @param player_hp
	 * @param enemy_hp
	 * @return 0 if both characters are alive, 1 if both are dead, 2 if player is dead, 3 if enemy is dead
	 */
	private static byte checkForDeath(int player_hp, int enemy_hp){
		if(player_hp > 0 && enemy_hp > 0){
			/* Both characters are still alive */
			return 0;
		}
		if(player_hp <= 0 && enemy_hp <= 0){
			/* Both characters are dead - a rare occurrence */
			return 1;
		}
		if(player_hp <= 0){
			/* The player character is dead */
			return 2;
		}
		/* If code reaches here, the enemy character is dead */
		return 3;
	}
	
}
