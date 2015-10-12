
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
	
	private int roll_modifier;
	
	private boolean is_grappled = false;
	private boolean is_grappling = false;
	
	public BasicPlayerCharacter(){
		current_hp = getMaxHealth();
		current_willpower = getMaxWillpower();
		current_defense = getDefense();
		roll_modifier = 0;
	}
	
	@Override
	public int getRollModifier(){
		return roll_modifier;
	}
	
	@Override
	public void setRollModifier(int new_mod){
		roll_modifier = new_mod;
	}
	
	@Override
	public boolean isGrappled(){
		return is_grappled;
	}
	
	@Override
	public void setGrappled(boolean grappled){
		if(grappled){
			is_grappling = false;
		}
		is_grappled = grappled;
	}
	
	@Override
	public boolean isGrappling(){
		return is_grappling;
	}
	
	@Override
	public void setGrappling(boolean grappling){
		if(grappling){
			is_grappled = false;
		}
		is_grappling = grappling;
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
		return 2;
	}

	@Override
	public int getStrength() {
		return 1;
	}

	@Override
	public int getDexterity() {
		return 3;
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
		return 2;
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
		return 3;
	}

	@Override
	public int getFirearms() {
		return 3;
	}

	@Override
	public int getWeaponry() {
		return 3;
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

	@Override
	public int getMeleeWeaponDamage() {
		return 2;
	}

	@Override
	public int getRangedWeaponDamage() {
		return 2;
	}

}
