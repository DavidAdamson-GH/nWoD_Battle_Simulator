
/**
 * A basic, ready-made character mainly for testing purposes.
 * 
 * @author David Adamson
 *
 */
public class BasicPlayerCharacter implements WoDCharacter {
	
	private int current_hp;
	private int current_willpower;
	private int current_defense;
	
	public BasicPlayerCharacter(){
		current_hp = getMaxHealth();
		current_willpower = getMaxWillpower();
		current_defense = getDefense();
	}
	
	@Override
	public void setCurrentHealth(int hp) {
		current_hp = hp;
	}

	@Override
	public void setCurrentWillpower(int willpower) {
		current_willpower = willpower;
	}
	
	@Override
	public void setCurrentDefense(int defense){
		current_defense = defense;
	}
	
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
		return current_hp;
	}

	@Override
	public int getMaxWillpower() {
		return getResolve() + getComposure();
	}

	@Override
	public int getCurrentWillpower() {
		return current_willpower;
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
	public int getCurrentDefense() {
		return current_defense;
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
