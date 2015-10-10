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

	private static final String PLAYER_LABEL = "PLAYER";
	private static final String ENEMY_LABEL = "ENEMY";
	
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
				
				/* The player gets the opportunity to aim for a specific body part */
				System.out.println("What part of the body will you aim for?");
				System.out.println("A for any, L for limb, HE for head, HA for hand, E for eye");
				String target;
				int bonus_damage = 0;
				int debuff = 0;
				int hit_penalty = 0;
				do{
					target = scan.nextLine().trim().toUpperCase();
				}while(!target.equals("A") && !target.equals("L") && !target.equals("HE")
						&& !target.equals("HA") && !target.equals("E"));
				switch(target){
					case "L" :
						debuff = 1;
						hit_penalty = 2;
						break;
					case "HE" :
						bonus_damage = 2;
						debuff = 1;
						hit_penalty = 4;
						break;
					case "HA" :
						debuff = 2;
						hit_penalty = 4;
						break;
					case "E" :
						bonus_damage = 2;
						debuff = 2;
						hit_penalty = 6;
						break;
				}
				enemy.setRollModifier(enemy.getRollModifier() - debuff);
				
				/* 
				 * To hit with a melee weapon, you must get at least 1 success with 
				 * Dexterity + Weaponry - Enemy_Defense 
				 */
				
				/* AI decides whether to spend a point of Willpower to increase Defense */
				if(enemy_ai.getDefenseBoostDecision(player.getCurrentHealth(),
						enemy.getCurrentHealth(), 
						enemy.getCurrentWillpower())){
					enemy.setCurrentWillpower(enemy.getCurrentWillpower() - 1);
					enemy.setCurrentDefense(enemy.getCurrentDefense() + 2);
				}
				int rolls = player.getDexterity() + player.getWeaponry() 
						- enemy.getCurrentDefense() + player.getRollModifier() - hit_penalty;
				if(willpower_spent){
					/* Spending Willpower allows for 3 more dice while rolling to hit */
					rolls += 3;
				}
				int successes = DiceRoller.getSuccesses(rolls, display_rolls, PLAYER_LABEL);
				if(successes > 0){
					System.out.println("You hit!");
					/* Damage is determined by making a Strength roll, then adding the default weapon damage */
					int damage = 1 + DiceRoller.getSuccesses(player.getStrength() + player.getRollModifier(), 
							display_rolls, PLAYER_LABEL);
					if(allout_attack){
						/* All-Out Attack allows for 2 more dice while rolling to damage */
						damage += DiceRoller.getSuccesses(2 + player.getRollModifier(), display_rolls, PLAYER_LABEL);
					}
					/* Apply bonus damage from aiming for a specific body part */
					damage += bonus_damage;
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
				
				/* The player gets the opportunity to aim for a specific body part */
				System.out.println("What part of the body will you aim for?");
				System.out.println("A for any, L for limb, HE for head, HA for hand, E for eye");
				String target;
				int bonus_damage = 0;
				int debuff = 0;
				int hit_penalty = 0;
				do{
					target = scan.nextLine().trim().toUpperCase();
				}while(!target.equals("A") && !target.equals("L") && !target.equals("HE")
						&& !target.equals("HA") && !target.equals("E"));
				switch(target){
					case "L" :
						debuff = 1;
						hit_penalty = 2;
						break;
					case "HE" :
						bonus_damage = 2;
						debuff = 1;
						hit_penalty = 4;
						break;
					case "HA" :
						debuff = 2;
						hit_penalty = 4;
						break;
					case "E" :
						bonus_damage = 2;
						debuff = 2;
						hit_penalty = 6;
						break;
				}
				enemy.setRollModifier(enemy.getRollModifier() - debuff);
				
				/* To hit with a ranged weapon, you must get at least 1 success with 
				 * Dexterity + Firearms 
				 */
				
				int rolls = player.getDexterity() + player.getFirearms() 
						- enemy.getCurrentDefense() + player.getRollModifier() - hit_penalty;
				if(willpower_spent){
					/* Spending Willpower allows for 3 more dice while rolling to hit */
					rolls += 3;
				}
				int successes = DiceRoller.getSuccesses(rolls, display_rolls, PLAYER_LABEL);
				if(successes > 0){
					System.out.println("You hit!");
					/* Ranged weapons have constant damage values */
					int damage = 2;
					/* Apply bonus damage from aiming for a specific body part */
					damage += bonus_damage;
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
				
				int successes = DiceRoller.getSuccesses(enemy.getDexterity() + enemy.getWeaponry() - 
						player.getCurrentDefense() + enemy.getRollModifier(), display_rolls, ENEMY_LABEL);
				if(enemy_move.contains("W") && enemy.getCurrentWillpower() > 0){
					/* Spending Willpower allows for 3 more dice while rolling to hit */
					successes += DiceRoller.getSuccesses(3 + enemy.getRollModifier(), display_rolls, ENEMY_LABEL);
					enemy.setCurrentWillpower(enemy.getCurrentWillpower() - 1);
					System.out.println("The enemy uses Willpower!");
				}
				if(successes > 0){
					System.out.println("The enemy hit!");
					int damage = 1 + DiceRoller.getSuccesses(enemy.getStrength() + enemy.getRollModifier(), 
							display_rolls, ENEMY_LABEL);
					if(enemy_move.contains("A")){
						/* All-Out Attack allows for 2 more dice while rolling to damage */
						damage += DiceRoller.getSuccesses(2 + enemy.getRollModifier(), display_rolls, ENEMY_LABEL);
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
				int successes = DiceRoller.getSuccesses(enemy.getDexterity() + enemy.getFirearms()
						+ enemy.getRollModifier(), display_rolls, ENEMY_LABEL);
				if(enemy_move.contains("W") && enemy.getCurrentWillpower() > 0){
					/* Spending Willpower allows for 3 more dice while rolling to hit */
					successes += DiceRoller.getSuccesses(3 + enemy.getRollModifier(), display_rolls, ENEMY_LABEL);
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
			}else if(enemy_move.contains("D")){
				int dodge_boost = 0;
				if(enemy_move.contains("W") && enemy.getCurrentWillpower() > 0){
					enemy.setCurrentWillpower(enemy.getCurrentWillpower() - 1);
					dodge_boost = 2;
				}
				enemy.setCurrentDefense(enemy.getCurrentDefense() * 2);
				enemy.setCurrentDefense(enemy.getCurrentDefense() + dodge_boost);
				System.out.println("The enemy prepares to dodge... (Defense increased to "+enemy.getCurrentDefense()+")");
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
