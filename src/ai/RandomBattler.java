package ai;

import java.util.Random;

/**
 * Battler AI that makes moves randomly.
 * 
 * @author David Adamson
 *
 */
public class RandomBattler implements BattlerAI {

	@Override
	public String getDecision(int player_hp, int battler_hp, int battler_willpower) {
		Random rng = new Random();
		int r = rng.nextInt(2);
		if(r == 0){
			r = rng.nextInt(4);
			switch(r){
				case 0 : return "MA";
				case 1 : return battler_willpower > 0 ? "MW" : "M";
				case 2 : return battler_willpower > 0 ? "MAW" : "MA";
				default: return "M";
			}
		}else{
			r = rng.nextInt(2);
			if(r == 0){
				return "R";
			}else{
				return battler_willpower > 0 ? "RW" : "R";
			}
		}
	}

}
