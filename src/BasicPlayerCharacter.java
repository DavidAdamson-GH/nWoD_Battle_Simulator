
/**
 * A basic, ready-made character mainly for testing purposes.
 * 
 * @author David Adamson
 *
 */
public class BasicPlayerCharacter implements WoDCharacter {
	
	@Override
	public int getIntelligence() {
		return 1;
	}

	@Override
	public int getWits() {
		return 2;
	}

	@Override
	public int getResolve() {
		return 1;
	}

	@Override
	public int getStrength() {
		return 1;
	}

	@Override
	public int getDexterity() {
		return 2;
	}

	@Override
	public int getStamina() {
		return 5;
	}

	@Override
	public int getPresence() {
		return 1;
	}

	@Override
	public int getManipulation() {
		return 1;
	}

	@Override
	public int getComposure() {
		return 1;
	}

	@Override
	public int getMaxHealth() {
		return getSize() + getStamina();
	}

	@Override
	public int getCurrentHealth() {
		return getMaxHealth();
	}

	@Override
	public int getMaxWillpower() {
		return getResolve() + getComposure();
	}

	@Override
	public int getCurrentWillpower() {
		return getMaxWillpower();
	}

	@Override
	public int getInvestigation() {
		return 1;
	}

	@Override
	public int getBrawl() {
		return 1;
	}

	@Override
	public int getFirearms() {
		return 2;
	}

	@Override
	public int getWeaponry() {
		return 1;
	}

	@Override
	public int getSize() {
		return 6;
	}

	@Override
	public int getDefense() {
		return Integer.min(getWits(), getDexterity());
	}

	@Override
	public int getInitiative() {
		return getDexterity() + getComposure();
	}

	@Override
	public int getMeleeArmor() {
		return 0;
	}

	@Override
	public int getRangedArmor() {
		return 0;
	}

}
