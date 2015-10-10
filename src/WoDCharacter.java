
/**
 * Interface for a standard World of Darkness Character
 * 
 * @author David Adamson
 *
 */
public interface WoDCharacter {

	/* Get/Set general modifiers */
	public int getRollModifier();
	public void setRollModifier(int new_mod);
	
	/* Change HP, Willpower and Defense*/
	public void setCurrentHealth(int hp);
	public void setCurrentWillpower(int willpower);
	public void setCurrentDefense(int defense);
	
	/* Mental Attributes */
	public int getIntelligence();
	public int getWits();
	public int getResolve();
	
	/* Physical Attributes */
	public int getStrength();
	public int getDexterity();
	public int getStamina();
	
	/* Social Attributes */
	public int getPresence();
	public int getManipulation();
	public int getComposure();

	/* Health */
	public int getMaxHealth();
	public int getCurrentHealth();
	
	/* Willpower */
	public int getMaxWillpower();
	public int getCurrentWillpower();
	
	/* Skills */
	// Currently only contains a select few skills
	public int getInvestigation();
	
	public int getBrawl();
	public int getFirearms();
	public int getWeaponry();
	
	/* Misc Values */
	public int getSize();
	public int getDefense();
	public int getCurrentDefense();
	public int getInitiative();
	public int getMeleeArmor();
	public int getRangedArmor();
	
}
