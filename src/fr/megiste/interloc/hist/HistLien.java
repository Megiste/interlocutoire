package fr.megiste.interloc.hist;

import java.util.Vector;

public class HistLien implements Historique {

	private String clef;
	
	private String[] clefsEnfants = new String[0];

	private String texte;

	public HistLien(String clef, Vector enfants, String texte) {
		this.clef = clef;
		clefsEnfants = (String[]) enfants.toArray(new String[enfants.size()]);
		this.texte = texte;
	}

	public String versChaine() {
		StringBuffer sb = new StringBuffer();
		sb.append(HIST).append(SEP);
		sb.append(PREFIXE_LIEN).append(SEP);
		sb.append(clef).append(SEP);
		for (int i = 0; i < clefsEnfants.length; i++) {
			String enfant = clefsEnfants[i];
			sb.append(enfant).append(SEP);
		}
		return sb.toString();
	}

	public String getPrefixe() {
		return PREFIXE_LIEN ;
	}

	/**
	 * @return the clef
	 */
	public String getClef() {
		return clef;
	}

	/**
	 * @return the clefsEnfants
	 */
	public String[] getClefsEnfants() {
		return clefsEnfants;
	}

	public String getTexte() {
		return texte;
	}
	
	

}
