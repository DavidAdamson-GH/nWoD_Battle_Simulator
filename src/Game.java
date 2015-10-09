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

	private static boolean display_rolls = false;
	
	public static void main(String[] args) {
		WoDCharacter player = new BasicPlayerCharacter();
		WoDCharacter enemy = new BasicPlayerCharacter();
		BattlerAI enemy_ai = new RandomBattler();
		
		Scanner scan = new Scanner(System.in);
		
		/* Configure game options before game starts */
		String input;
		do{
			System.out.println("Should dice rolls be displayed this game? (Y/N)");
			/* Clean up the input, and only accept Y or N */
			input = scan.nextLine().trim().toUpperCase();
			if(!input.equals("Y") && !input.equals("N")){
				System.out.println("Please respond with either Y or N");
			}
		}while(!input.equals("Y") && !input.equals("N"));
		if(input.equals("Y")){
			System.out.println("Rolls WILL be displayed");
			display_rolls = true;
		}else{
			System.out.println("Rolls will NOT be displayed");
			display_rolls = false;
		}
		
		/* The game begins */
		while(true){
			/* Each turn, player and enemy HP is displayed */
			System.out.println("Player HP: "+player.getCurrentHealth()+"     Willpower: "+player.getCurrentWillpower());
			System.out.println("Enemy HP: "+enemy.getCurrentHealth()+"     Willpower: "+enemy.getCurrentWillpower());
			System.out.println();
			
			/* Check if any character is dead yet */
			byte death_code = checkForDeath(player.getCurrentHealth(), enemy.getCurrentHealth());
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
			
			/* Reset player's Defense back to full */
			player.setCurrentDefense(player.getDefense());
			
			System.out.println("Enter M for melee, R for ranged, or D to dodge");
			boolean willpower_spent = false;
			boolean allout_attack = false;
			do{
				/* Clean up the input, and only accept M, R or D */
				input = scan.nextLine().trim().toUpperCase();
			}while(!input.equals("M") && !input.equals("R") && !input.equals("D"));
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
				
				int successes = DiceRoller.getSuccesses(player.getDexterity() + player.getWeaponry() - enemy.getCurrentDefense(),
						display_rolls);
				if(willpower_spent){
					/* Spending Willpower allows for 3 more dice while rolling to hit */
					successes += DiceRoller.getSuccesses(3, display_rolls);
				}
				if(successes > 0){
					System.out.println("You hit!");
					/* Damage is determined by making a Strength roll, then adding the default weapon damage */
					int damage = 1 + DiceRoller.getSuccesses(player.getStrength(), display_rolls);
					if(allout_attack){
						/* All-Out Attack allows for 2 more dice while rolling to damage */
						damage += DiceRoller.getSuccesses(2, display_rolls);
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
				
				int successes = DiceRoller.getSuccesses(player.getDexterity() + player.getFirearms(), display_rolls);
				if(willpower_spent){
					/* Spending Willpower allows for 3 more dice while rolling to hit */
					successes += DiceRoller.getSuccesses(3, display_rolls);
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
			}else if(input.equals("D")){
				
				/* Dodging doubles your Defense until your next turn */
				
				player.setCurrentDefense(player.getCurrentDefense() * 2);
				/* Spending a point of Willpower adds +2 to Defense on top of this */
				player.setCurrentDefense(player.getCurrentDefense() + 2);
				System.out.println("You prepare to Dodge, increasing your Defense to " + player.getCurrentDefense());
			}
			
			/* Check if the enemy is dead - the enemy can't make a move if it's dead! */
			if(enemy.getCurrentHealth() <= 0){
				/* Skip to the next turn, where the game will end */
				continue;
			}
			
			/* Reset enemy's Defense back to full */
			enemy.setCurrentDefense(enemy.getDefense());
			
			/* Let the AI decide the enemy's move */
			String enemy_move = enemy_ai.getDecision(player.getCurrentHealth(), enemy.getCurrentHealth(), enemy.getCurrentWillpower());
			
			if(enemy_move.contains("M")){
				
				/* Melee attack */
				
				/* Give player a chance to spend Willpower to increase Defense */
				if(player.getCurrentWillpower() > 0){
					System.out.println("The enemy is about to attempt a melee attack");
					System.out.println("Would you like to spend a Willpower point to increase your Defense? (Y/N)");
					String willpower_decision;
					do{
						willpower_decision = scan.nextLine().trim().toUpperCase();
					}while(!willpower_decision.equals("Y") && !willpower_decision.equals("N"));
					if(willpower_decision.equals("Y")){
						player.setCurrentWillpower(player.getCurrentWillpower() - 1);
						player.setCurrentDefense(player.getCurrentDefense() + 2);
					}
				}
				
				int successes = DiceRoller.getSuccesses(enemy.getDexterity() + enemy.getWeaponry() - player.getCurrentDefense(),
						display_rolls);
				if(enemy_move.contains("W") && enemy.getCurrentWillpower() > 0){
					/* Spending Willpower allows for 3 more dice while rolling to hit */
					successes += DiceRoller.getSuccesses(3, display_rolls);
					enemy.setCurrentWillpower(enemy.getCurrentWillpower() - 1);
					System.out.println("The enemy uses Willpower!");
				}
				if(successes > 0){
					System.out.println("The enemy hit!");
					int damage = 1 + DiceRoller.getSuccesses(enemy.getStrength(), display_rolls);
					if(enemy_move.contains("A")){
						/* All-Out Attack allows for 2 more dice while rolling to damage */
						damage += DiceRoller.getSuccesses(2, display_rolls);
						/* Now apply defense penalty */
						enemy.setCurrentDefense(0);
						System.out.println("The enemy goes for an all-out attack!");
					}
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
			}else if(enemy_move.contains("R")){
				/* Ranged attack */
				int successes = DiceRoller.getSuccesses(enemy.getDexterity() + enemy.getFirearms(), display_rolls);
				if(enemy_move.contains("W") && enemy.getCurrentWillpower() > 0){
					/* Spending Willpower allows for 3 more dice while rolling to hit */
					successes += DiceRoller.getSuccesses(3, display_rolls);
					enemy.setCurrentWillpower(enemy.getCurrentWillpower() - 1);
					System.out.println("The enemy uses Willpower!");
				}
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
