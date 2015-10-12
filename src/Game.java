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
			
			boolean willpower_spent = false;
			/* Check if player is grappling an enemy */
			if(player.isGrappling()){
				System.out.println("You are still grappling an enemy!");
				input = getGrapplingMoveDecision();
			}else if(player.isGrappled()){
				System.out.println("You are still being grappled!");
				input = getGrappledMoveDecision();
			}else{
				input = getMoveDecision();
			}
			willpower_spent = getWillpowerDecision(player.getCurrentWillpower());
			if(willpower_spent){
				player.setCurrentWillpower(player.getCurrentWillpower() - 1);
			}
			
			if(input.equals("M")){
				meleeAttack(willpower_spent, player, enemy, enemy_ai);
			}else if(input.equals("R")){
				rangedAttack(willpower_spent, player, enemy);
			}else if(input.equals("G")){
				grapple(willpower_spent, player, enemy);
			}else if(input.equals("A")){
				grappleAttack(player, enemy);
			}else if(input.equals("X")){
				releaseGrapple(player, enemy);
			}else if(input.equals("D")){
				dodge(willpower_spent, player);
			}else if(input.equals("OV")){
				overpower(willpower_spent, player, enemy);
			}else if(input.equals("E")){
				escapeGrapple(willpower_spent, player, enemy);
			}
			
			/* Check if the enemy is dead - the enemy can't make a move if it's dead! */
			if(enemy.getCurrentHealth() <= 0){
				/* Skip to the next turn, where the game will end */
				continue;
			}
			
			/* Reset enemy's Defense back to full */
			enemy.setCurrentDefense(enemy.getDefense());
			
			/* Let the AI decide the enemy's move */
			String enemy_move = null;
			if(enemy.isGrappled()){
				/* If enemy is grappled, AI is limited to certain moves */
				enemy_move = enemy_ai.getGrappledDecision(player.getCurrentHealth(), 
						enemy.getCurrentHealth(), enemy.getCurrentWillpower());
			}else if(enemy.isGrappling()){
				/* If enemy is grappling, AI is limited to certain moves */
				enemy_move = enemy_ai.getGrapplingDecision(player.getCurrentHealth(), 
						enemy.getCurrentHealth(), enemy.getCurrentWillpower());
			}else{
				enemy_move = enemy_ai.getDecision(player.getCurrentHealth(),
						enemy.getCurrentHealth(), enemy.getCurrentWillpower());
			}
			
			assert enemy_move != null;
			if(enemy_move.contains("M")){
				/* Melee attack */
				
				/* Give player a chance to spend Willpower to increase Defense */
				if(player.getCurrentWillpower() > 0){
					System.out.println("The enemy is about to attempt a melee attack!");
					System.out.println("You can spend a Willpower point to temporarily increase your Defense.");
				}
				willpower_spent = getWillpowerDecision(player.getCurrentWillpower());
				if(willpower_spent){
					player.setCurrentWillpower(player.getCurrentWillpower() - 1);
					player.setCurrentDefense(player.getCurrentDefense() + 2);
				}
				
				/* AI can aim for specific body part */
				String target;
				int bonus_damage = 0;
				int debuff = 0;
				int hit_penalty = 0;
				target = enemy_ai.getBodyPartDecision(player.getCurrentHealth(),
						enemy.getCurrentHealth(), 
						enemy.getCurrentWillpower(), 
						enemy.getRollModifier());
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
				player.setRollModifier(player.getRollModifier() - debuff);
				
				/* Now roll to hit */
				
				int rolls = enemy.getDexterity() + enemy.getWeaponry() 
						- player.getCurrentDefense() - hit_penalty;
				if(enemy_move.contains("W") && enemy.getCurrentWillpower() > 0){
					/* Spending Willpower allows for 3 more dice while rolling to hit */
					rolls += 3;
					enemy.setCurrentWillpower(enemy.getCurrentWillpower() - 1);
					System.out.println("The enemy uses Willpower!");
				}
				int successes = DiceRoller.getSuccesses(rolls, enemy.getRollModifier(),
						display_rolls, ENEMY_LABEL);
				if(successes > 0){
					System.out.println("The enemy hit!");
					int damage_rolls = enemy.getStrength();
					if(enemy_move.contains("A")){
						/* All-Out Attack allows for 2 more dice while rolling to damage */
						damage_rolls += 2;
						/* Now apply defense penalty */
						enemy.setCurrentDefense(0);
						System.out.println("The enemy goes for an all-out attack!");
					}
					int damage = DiceRoller.getSuccesses(damage_rolls, enemy.getRollModifier(),
							display_rolls, ENEMY_LABEL);
					/* Add weapon damage */
					damage += enemy.getMeleeWeaponDamage();
					/* Add bonus damage */
					damage += bonus_damage;
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
				
				/* AI can aim for specific body part */
				String target;
				int bonus_damage = 0;
				int debuff = 0;
				int hit_penalty = 0;
				target = enemy_ai.getBodyPartDecision(player.getCurrentHealth(),
						enemy.getCurrentHealth(), 
						enemy.getCurrentWillpower(), 
						enemy.getRollModifier());
				switch(target){
					case "L" :
						System.out.println("The enemy is aiming for a limb!");
						debuff = 1;
						hit_penalty = 2;
						break;
					case "HE" :
						System.out.println("The enemy is aiming for the head!");
						bonus_damage = 2;
						debuff = 1;
						hit_penalty = 4;
						break;
					case "HA" :
						System.out.println("The enemy is aiming for a hand!");
						debuff = 2;
						hit_penalty = 4;
						break;
					case "E" :
						System.out.println("The enemy is aiming for an eye!");
						bonus_damage = 2;
						debuff = 2;
						hit_penalty = 6;
						break;
				}
				player.setRollModifier(player.getRollModifier() - debuff);
				
				/* Now roll to hit */
				
				int rolls = enemy.getDexterity() + enemy.getFirearms() - hit_penalty;
				if(enemy_move.contains("W") && enemy.getCurrentWillpower() > 0){
					/* Spending Willpower allows for 3 more dice while rolling to hit */
					rolls += 3;
					enemy.setCurrentWillpower(enemy.getCurrentWillpower() - 1);
					System.out.println("The enemy uses Willpower!");
				}
				int successes = DiceRoller.getSuccesses(rolls, enemy.getRollModifier(),
						display_rolls, ENEMY_LABEL);
				if(successes > 0){
					System.out.println("The enemy hit!");
					/* Get ranged weapon damage */
					int damage = enemy.getRangedWeaponDamage();
					/* Add bonus damage */
					damage += bonus_damage;
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
				/* Dodge */
				int dodge_boost = 0;
				if(enemy_move.contains("W") && enemy.getCurrentWillpower() > 0){
					enemy.setCurrentWillpower(enemy.getCurrentWillpower() - 1);
					dodge_boost = 2;
				}
				enemy.setCurrentDefense(enemy.getCurrentDefense() * 2);
				enemy.setCurrentDefense(enemy.getCurrentDefense() + dodge_boost);
				System.out.println("The enemy prepares to dodge... (Defense increased to "+enemy.getCurrentDefense()+")");
			}else if(enemy_move.contains("OV")){
				/* Overpower grapple */
				int rolls = enemy.getStrength() + enemy.getBrawl() - player.getStrength();
				if(enemy_move.contains("W") && enemy.getCurrentWillpower() > 0){
					enemy.setCurrentWillpower(enemy.getCurrentWillpower() - 1);
					rolls += 3;
				}
				int successes = DiceRoller.getSuccesses(rolls, enemy.getRollModifier(),
						display_rolls, ENEMY_LABEL);
				if(successes > 0){
					player.setGrappled(true);
					enemy.setGrappling(true);
					System.out.println("The enemy overpowers you and is now in control of the grapple!");
				}
			}else if(enemy_move.contains("E")){
				/* Escape from grapple */
				int ai_rolls = enemy.getStrength();
				if(enemy_move.contains("W") && enemy.getCurrentWillpower() > 0){
					enemy.setCurrentWillpower(enemy.getCurrentWillpower() - 1);
					ai_rolls += 3;
				}
				int ai_successes = DiceRoller.getSuccesses(ai_rolls, enemy.getRollModifier(),
						display_rolls, ENEMY_LABEL);
				int player_successes = DiceRoller.getSuccesses(player.getStrength(), player.getRollModifier(),
						display_rolls, PLAYER_LABEL);
				if(ai_successes > player_successes){
					player.setGrappling(false);
					enemy.setGrappling(false);
					System.out.println("The enemy escapes the grapple!");
				}
			}else if(enemy_move.contains("A")){
				/* Grapple attack */
				int rolls = enemy.getStrength() + enemy.getBrawl();
				if(enemy_move.contains("W") && enemy.getCurrentWillpower() > 0){
					enemy.setCurrentWillpower(enemy.getCurrentWillpower() - 1);
					rolls += 3;
				}
				int damage = DiceRoller.getSuccesses(rolls, enemy.getRollModifier(), 
						display_rolls, ENEMY_LABEL);
				int final_damage = damage - player.getMeleeArmor();
				if(final_damage <= (damage * -1)){
					final_damage = 0;
				}else if(final_damage < 1){
					final_damage = 1;
				}
				player.setCurrentHealth(player.getCurrentHealth() - final_damage);
				System.out.println("The enemy strikes you for "+final_damage+" damage!");
			}else if(enemy_move.contains("X")){
				/* Release player from grapple */
				player.setGrappled(false);
				enemy.setGrappling(false);
				System.out.println("The enemy releases you from the grapple!");
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
	
	/**
	 * Obtains user input corresponding to a possible move, and returns it.
	 * 
	 * @return a string corresponding to the move picked by the player
	 */
	private static String getMoveDecision(){
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter M for melee, R for ranged, G to grapple, or D to dodge");
		String input;
		do{
			/* Clean up the input, and only accept M, R or D */
			input = scan.nextLine().trim().toUpperCase();
		}while(!input.equals("M") && !input.equals("R") && !input.equals("G") && !input.equals("D"));
		return input;
	}
	
	private static String getGrapplingMoveDecision(){
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter A to attack, or X to release:");
		String input;
		do{
			/* Clean up the input, and only accept A or R */
			input = scan.nextLine().trim().toUpperCase();
		}while(!input.equals("A") && !input.equals("X"));
		return input;
	}
	
	private static String getGrappledMoveDecision(){
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter OV to overpower, or E to escape:");
		String input;
		do{
			/* Clean up the input, and only accept OV or E */
			input = scan.nextLine().trim().toUpperCase();
		}while(!input.equals("OV") && !input.equals("E"));
		return input;
	}
	
	/**
	 * 
	 * @return a boolean corresponding to the decision of whether or not to spend Willpower
	 */
	private static boolean getWillpowerDecision(int player_willpower){
		Scanner scan = new Scanner(System.in);
		if(player_willpower > 0){
			System.out.println("Would you like to spend a Willpower point? (Y/N)");
			String willpower_decision;
			do{
				willpower_decision = scan.nextLine().trim().toUpperCase();
			}while(!willpower_decision.equals("Y") && !willpower_decision.equals("N"));
			if(willpower_decision.equals("Y")){
				return true;
			}
		}
		return false;
	}
	
	private static void meleeAttack(boolean willpower_spent, 
			WoDCharacter player, WoDCharacter enemy, BattlerAI enemy_ai){
		
		Scanner scan = new Scanner(System.in);
		System.out.println("Would you like to make an All-Out Attack?");
		boolean allout_attack = false;
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
		int successes = DiceRoller.getSuccesses(rolls, player.getRollModifier(),
				display_rolls, PLAYER_LABEL);
		if(successes > 0){
			System.out.println("You hit!");
			/* Damage is determined by making a Strength roll, then adding the default weapon damage */
			int damage_rolls = player.getStrength();
			if(allout_attack){
				/* All-Out Attack allows for 2 more dice while rolling to damage */
				damage_rolls += 2;
			}
			int damage = DiceRoller.getSuccesses(damage_rolls, player.getRollModifier(),
					display_rolls, PLAYER_LABEL);
			/* Add weapon damage */
			damage += player.getMeleeWeaponDamage();
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
	}
	
	private static void rangedAttack(boolean willpower_spent, WoDCharacter player, WoDCharacter enemy){
		Scanner scan = new Scanner(System.in);
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
				 + player.getRollModifier() - hit_penalty;
		if(willpower_spent){
			/* Spending Willpower allows for 3 more dice while rolling to hit */
			rolls += 3;
		}
		int successes = DiceRoller.getSuccesses(rolls, player.getRollModifier(),
				display_rolls, PLAYER_LABEL);
		if(successes > 0){
			System.out.println("You hit!");
			/* Get ranged weapon damage */
			int damage = player.getRangedWeaponDamage();
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
	}
	
	private static void grapple(boolean willpower_spent, WoDCharacter player, WoDCharacter enemy){
		int rolls = player.getStrength() + player.getBrawl() 
				+ player.getRollModifier() - enemy.getCurrentDefense();
		
		if(willpower_spent){
			rolls += 3;
		}
		int successes = DiceRoller.getSuccesses(rolls, player.getRollModifier(),
				display_rolls, PLAYER_LABEL);
		if(successes > 0){
			/* Successful grapple */
			player.setGrappling(true);
			enemy.setGrappled(true);
			System.out.println("You grappled the enemy!");
		}else{
			System.out.println("You failed to grapple the enemy...");
		}
	}
	
	private static void grappleAttack(WoDCharacter player, WoDCharacter enemy){
		int successes = DiceRoller.getSuccesses(player.getStrength() + player.getBrawl(),
				player.getRollModifier(), display_rolls, PLAYER_LABEL);
		
		int damage = successes - enemy.getMeleeArmor();
		if(damage <= (successes * -1)){
			damage = 0;
		}else if(damage < 1){
			damage = 1;
		}
		
		enemy.setCurrentHealth(enemy.getCurrentHealth() - damage);
		System.out.println("You struck the enemy for "+damage+" damage!");
	}
	
	public static void releaseGrapple(WoDCharacter player, WoDCharacter enemy){
		player.setGrappling(false);
		player.setGrappled(false);
		enemy.setGrappling(false);
		enemy.setGrappled(false);
	}
	
	private static void dodge(boolean willpower_spent, WoDCharacter player){
		/* Dodging doubles your Defense until your next turn */
		
		player.setCurrentDefense(player.getCurrentDefense() * 2);
		if(willpower_spent){
			/* Spending a point of Willpower adds +3 to Defense on top of this */
			player.setCurrentDefense(player.getCurrentDefense() + 3);
		}
		System.out.println("You prepare to Dodge, increasing your Defense to " + player.getCurrentDefense());
	}
	
	private static void overpower(boolean willpower_spent, WoDCharacter player, WoDCharacter enemy){
		int rolls = player.getStrength() + player.getBrawl() - enemy.getStrength();
		if(willpower_spent){
			player.setCurrentWillpower(player.getCurrentWillpower() - 1);
			rolls += 3;
		}
		int successes = DiceRoller.getSuccesses(rolls, player.getRollModifier(),
				display_rolls, PLAYER_LABEL);
		if(successes > 0){
			System.out.println("You successfully overpower the enemy!");
			player.setGrappled(false);
			player.setGrappling(true);
			enemy.setGrappled(true);
			enemy.setGrappling(false);
		}else{
			System.out.println("You fail to overpower the enemy...");
		}
	}
	
	private static void escapeGrapple(boolean willpower_spent, WoDCharacter player, WoDCharacter enemy){
		int player_rolls = player.getStrength();
		if(willpower_spent){
			player.setCurrentWillpower(player.getCurrentWillpower() - 1);
			player_rolls += 3;
		}
		int player_successes = DiceRoller.getSuccesses(player_rolls, player.getRollModifier(),
				display_rolls, PLAYER_LABEL);
		int enemy_successes = DiceRoller.getSuccesses(enemy.getStrength(), enemy.getRollModifier(),
				display_rolls, PLAYER_LABEL);
		if(player_successes > enemy_successes){
			System.out.println("You successfully escape the grapple!");
			player.setGrappled(false);
			player.setGrappling(false);
			enemy.setGrappled(false);
			enemy.setGrappling(false);
		}else{
			System.out.println("You fail to escape the grapple...");
		}
	}
}
