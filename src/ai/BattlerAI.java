package ai;

/**
 * Interface for AIs that will battling.
 * 
 * @author David Adamson
 *
 */
public interface BattlerAI {

	public String getDecision(int player_hp, int battler_hp, int battler_willpower);
	public boolean getDefenseBoostDecision(int player_hp, int battler_hp, int battler_willpower);
	public String getBodyPartDecision(int player_hp, int battler_hp, 
			int battler_willpower, int battler_dicemod);
	public String getGrappledDecision(int player_hp, int battler_hp, int battler_willpower);
	public String getGrapplingDecision(int player_hp, int battler_hp, int battler_willpower);
	
}
