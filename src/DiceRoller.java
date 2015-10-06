import java.util.Random;

/**
 * Manages dice rolls in the World of Darkness d10 system.
 * 
 * @author David
 *
 */
public class DiceRoller {

	public static int getSuccesses(int dice){
		/* By default, dice only crit on a 10 */
		return getSuccesses(dice, 10);
	}
	
	public static int getSuccesses(int dice, int crit_threshold){
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
			/* Re-roll the crits */
			rolls = new int[crits];
		}while(crits > 0);
		
		return successes;
	}
	
}
