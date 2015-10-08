package ai;

/**
 * Interface for AIs that will battling.
 * 
 * @author David Adamson
 *
 */
public interface BattlerAI {

	public String getDecision(int player_hp, int battler_hp, int battler_willpower);
	
}
