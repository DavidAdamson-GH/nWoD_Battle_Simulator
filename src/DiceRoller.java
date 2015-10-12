import java.util.Random;

/**
 * Manages dice rolls in the World of Darkness d10 system.
 * 
 * @author David Adamson
 *
 */
public class DiceRoller {

	public static int getSuccesses(int dice, int roll_modifier, boolean display_rolls, String label){
		/* By default, dice only crit on a 10 */
		return getSuccesses(dice, roll_modifier, 10, display_rolls, label);
	}
	
	public static int getSuccesses(int dice, int roll_modifier, int crit_threshold, boolean display_rolls, String label){
		boolean chance_roll = false;
		dice += roll_modifier;
		if(dice <= 0){
			/* 
			 * If less than 1 die is to be rolled, the roll becomes a Chance Roll, 
			 * which means only a 10 will count as a success 
			 */
			chance_roll = true;
			dice = 1;
		}
		int[] rolls = new int[dice];
		int successes = 0;
		int crits;
		Random rng = new Random();
		do{
			crits = 0;
			for(int i = 0; i < rolls.length; i++){
				rolls[i] = rng.nextInt(10) + 1;
				if(rolls[i] >= 8 && !chance_roll) {
					/* Anything 8 or higher is a success */
					successes++;
					if(rolls[i] >= crit_threshold){
						/* Check to see if it's a crit */
						crits++;
					}
				}else if(rolls[i] == 10 && chance_roll){
					/* Chance Roll is a success - but no crit is possible with a Chance Roll */
					successes++;
				}
			}
			if(display_rolls && rolls.length > 0){
				System.out.println("("+label+")" + (chance_roll ? "[chance roll] " : "") + rollsToString(rolls));
			}
			/* Re-roll the crits */
			rolls = new int[crits];
			if(display_rolls && rolls.length > 0){
				System.out.println(rollsToString(rolls));
			}
		}while(crits > 0);
			
		return successes;
	}
	
	private static String rollsToString(int[] rolls){
		String output = "[";
		for(int i = 0; i < rolls.length; i++){
			output = output.concat(Integer.toString(rolls[i]));
			output = output.concat("  ");
		}
		return output.trim() + "]";
	}
	
}
