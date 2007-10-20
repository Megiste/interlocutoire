package fr.megiste.interloc.hist;

public class HistSupp implements Historique {

	
	private int reco;
	private String clef;

	public HistSupp(String clef, int reconnexion) {
		this.clef = clef;
		this.reco = reconnexion;
	}

	public String getPrefixe() {
		// TODO Auto-generated method stub
		return PREFIXE_SUPP;
	}

	public String versChaine() {
		StringBuffer sb = new StringBuffer();
		sb.append(HIST).append(SEP);
		sb.append(PREFIXE_SUPP).append(SEP);
		sb.append(clef).append(SEP);
		sb.append(reco).append(SEP);
		return sb.toString();
	}

	/**
	 * @return the clef
	 */
	public String getClef() {
		return clef;
	}

	/**
	 * @return the reco
	 */
	public int getReco() {
		return reco;
	}

}
