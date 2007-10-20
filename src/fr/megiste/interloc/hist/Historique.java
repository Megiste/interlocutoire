package fr.megiste.interloc.hist;

/**
 * Element historique
 * @author LKLOETZER
 *
 */
public interface Historique{
	
	public static String HIST = "hist";
	public static String SEP = ":";
	public static final String PREFIXE_LIEN = "lien";
	public static final String PREFIXE_SUPP = "supp";
	public static final String PREFIXE_REN = "mod";
	
	

	public String getPrefixe();
	
	public String versChaine();

}
