package fr.megiste.interloc.data;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import fr.megiste.interloc.InterlocMain;

public class Lien {

	private static final String CLEF_PHRASE = "P";

	private static final String CLEF_LIEN = "I";

	private static final Comparator COMPARATOR_LIENS_SELON_POSITION = new Comparator(){

		public int compare(Object o1, Object o2) {
			Lien l1 = (Lien) o1;
			Lien l2 = (Lien) o2;
			return (int) (l1.getPosition() - l2.getPosition());
		}
		
	};

	private Lien pere = null;

	private String texte = null;

	private int index = -1;

	public String clef = null;

	private int profondeur = -1;

	private double position = -1;
	
	private Vector enfants = new Vector();
	

	private static int PROCHAINE_CLEF=0;

	public Lien(String texte, int indice) {
		this.texte = texte;
		this.index = indice;
		clef = CLEF_PHRASE + indice;
		
		if(indice>=PROCHAINE_CLEF){
			PROCHAINE_CLEF = indice + 1;
		}
	}

	public Lien(Vector liensSelectionnes) {
		clef = CLEF_LIEN + (PROCHAINE_CLEF);
		PROCHAINE_CLEF++;
		texte = CLEF_LIEN;
		
		TreeSet liensTries = new TreeSet(COMPARATOR_LIENS_SELON_POSITION);
		liensTries.addAll(liensSelectionnes);
		enfants.addAll(liensTries);

		for(int i=0;i<enfants.size();i++){
			getFils(i).pere = this;
		}
	}	
	
	private static void traiterClef(String clef){
		String chaineNombre;
		int nb;
		if(clef.startsWith(CLEF_PHRASE) || clef.startsWith(CLEF_LIEN)){
			chaineNombre = clef.substring(1);
			try {
				nb = Integer.parseInt(chaineNombre);
				if(nb>=PROCHAINE_CLEF){
					PROCHAINE_CLEF = nb+1;
				}
			} catch (NumberFormatException e) {
				InterlocMain.erreur(e);
			}
		}
	}

	public Lien(String clef, String desc) {
		traiterClef(clef);
		this.clef = clef;
		this.texte = desc;
	}

	public Lien(String clef2, Vector enfants) {
		this(enfants);
		traiterClef(clef2);
		this.clef = clef2;
	}

	public void reinitCaracs() {
		position = -1;
		profondeur = -1;
		if(pere!=null) pere.reinitCaracs();
		
	}

	/**
	 * @return the texte
	 */
	public String getTexte() {
			return texte;	
	}
	
	/**
	 * @param texte
	 *            the texte to set
	 */
	public void setTexte(String texte) {
		this.texte = texte;
	}

	public String getClef() {
		return clef;
	}

	/**
	 * @return the pere
	 */
	public Lien getPere() {
		return pere;
	}

	public int getProfondeur() {
		if (profondeur == -1) {
			if (estFeuille()) {
				profondeur = 1;
			} else {
				int profMax=0;
				for(int i=0;i<enfants.size();i++){
					profMax = Math.max(getFils(i).getProfondeur(),profMax);
				}
				
				profondeur = profMax + 1;
			}
		}
		return profondeur;
	}

	public double getPosition() {
		if (position == -1) {
			if (estFeuille()) {
				position = index;
			} else {
				double totalPos = 0;
				for(int i=0;i<enfants.size();i++){
					totalPos = totalPos + getFils(i).getPosition();
				}
				position = totalPos / enfants.size();
			}
		}
		return position;

	}

	public boolean equals(Object o1, Object o2) {
		return o1 == o2;
	}

	public Lien getPatriarche() {
		if (pere == null)
			return this;
		else
			return pere.getPatriarche();
	}

	public int getNumeroFils(Lien lien) {
		return enfants.indexOf(lien);
	}

	public void setFils(int numeroFils, Lien newLink) {
		if(numeroFils<enfants.size()){
			enfants.setElementAt(newLink, numeroFils);
		}
		else if(numeroFils==enfants.size()){
			enfants.add(newLink);
		} else {
			InterlocMain.quitter(1,"tentative d'ajouter un fils no " + numeroFils + " quand enfants.size()=" + enfants.size());
		}
			
			
		newLink.pere = this;
		reinitCaracs();
		
	}
	
	public Lien getFils(int numero){
		return (Lien) enfants.get(numero);
	}
	
	public Lien[] getTousFils(){
		Lien[] sortie = new Lien[getNombreEnfants()];
		for (int i = 0; i < sortie.length; i++) {
			sortie[i] = getFils(i);
			
		}
		return sortie;
	}

	public void nettoyerConnexions() {
		for(int i=0;i<enfants.size();i++){
			getFils(i).pere = null;
		}
	}

	public boolean estFeuille(){
		return enfants.size() == 0;
	}

	public int getNombreEnfants() {
		return enfants.size();
	}

	public int getIndex() {
		return index;
	}

	public Vector getClefsEnfants() {
		Vector sortie = new Vector(0);
		for (Iterator iter = enfants.iterator(); iter.hasNext();) {
			Lien enfant = (Lien) iter.next();
			sortie.add(enfant.getClef());
		}
		return sortie;
	}

}
