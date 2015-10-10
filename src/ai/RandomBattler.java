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
		int r = rng.nextInt(3);
		if(r == 0){
			/* Melee */
			r = rng.nextInt(4);
			switch(r){
				case 0 : return "MA";
				case 1 : return battler_willpower > 0 ? "MW" : "M";
				case 2 : return battler_willpower > 0 ? "MAW" : "MA";
				default: return "M";
			}
		}else if(r == 1){
			/* Ranged */
			r = rng.nextInt(2);
			if(r == 0){
				/* 50% chance of attempting to use Willpower */
				return battler_willpower > 0 ? "RW" : "R";
			}else{
				return "R";
			}
		}else{
			/* Dodge */
			r = rng.nextInt(4);
			if(r == 0){
				/* 25% chance of attempting to use Willpower */
				return battler_willpower > 0 ? "DW" : "D";
			}else{
				return "D";
			}
		}
	}

	@Override
	public boolean getDefenseBoostDecision(int player_hp, int battler_hp, int battler_willpower) {
		Random rng = new Random();
		int r = rng.nextInt(2);
		if(r == 0){
			return false;
		}else{
			if(battler_willpower > 0){
				return true;
			}
			return false;
		}
	}

	@Override
	public String getBodyPartDecision(int player_hp, int battler_hp, 
			int battler_willpower, int battler_dicemod) {
		Random rng = new Random();
		int r = rng.nextInt(100);
		if((r >= 0) && (r < 60)){
			return "A";
		}
		if((r >= 60) && (r < 80)){
			return "L";
		}
		if((r >= 80) && (r < 88)){
			return "HE";
		}
		if((r >= 88) && (r < 96)){
			return "HA";
		}
		return "E";
	}
	
}
