import java.util.Random;

/**
 * Manages dice rolls in the World of Darkness d10 system.
 * 
 * @author David Adamson
 *
 */
public class DiceRoller {

	public static int getSuccesses(int dice, boolean display_rolls){
		/* By default, dice only crit on a 10 */
		return getSuccesses(dice, 10, display_rolls);
	}
	
	public static int getSuccesses(int dice, int crit_threshold, boolean display_rolls){
		int[] rolls = new int[dice];
		int successes = 0;
		int crits;
		Random rng = new Random();
		do{
			crits = 0;
			for(int i = 0; i < rolls.length; i++){
				rolls[i] = rng.nextInt(10) + 1;
				if(rolls[i] >= 8) {
					/* Anything 8 or higher is a success */
					successes++;
					if(rolls[i] >= crit_threshold){
						/* Check to see if it's a crit */
						crits++;
					}
				}
			}
			if(display_rolls && rolls.length > 0){
				System.out.println(rollsToString(rolls));
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
