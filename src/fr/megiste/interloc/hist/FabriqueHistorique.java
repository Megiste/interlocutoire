package fr.megiste.interloc.hist;

import java.util.StringTokenizer;
import java.util.Vector;

public class FabriqueHistorique {
	public static Historique fabrique(String chaine){
		if(chaine == null || chaine.length()==0) return null;
		
		StringTokenizer st = new StringTokenizer(chaine,Historique.SEP);
		if(!Historique.HIST.equals(st.nextToken())) return null;
		
		if(Historique.PREFIXE_LIEN.equals(st.nextToken())){
			return fabriqueHistLien(st);
		}else if(Historique.PREFIXE_SUPP.equals(st.nextToken())){
			return fabriqueHistSupp(st);
		}
		return null;
	}

	private static Historique fabriqueHistSupp(StringTokenizer st) {
		String clef = st.nextToken();
		int reco = Integer.parseInt(st.nextToken());
		return new HistSupp(clef,reco);
	}

	private static Historique fabriqueHistLien(StringTokenizer st) {
		String clef = st.nextToken();
		String texte = st.nextToken();
		Vector enfants = new Vector();
		
		while(st.hasMoreTokens()){
			enfants.add(st.nextToken());
		}
		
		return new HistLien(clef,enfants, texte);
	}
}
