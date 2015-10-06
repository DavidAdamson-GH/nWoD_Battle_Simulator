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
	public String getDecision(int player_hp, int enemy_hp) {
		int r = new Random().nextInt(2);
		if(r == 0){
			return "M";
		}else{
			return "R";
		}
	}

}
