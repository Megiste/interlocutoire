package fr.megiste.interloc.ihm;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.Vector;

import fr.megiste.interloc.data.Lien;

public class DessinLien {

	public static Font BOLD_FONT;

	public static int LIGNE = DessinateurLiens.HAUTEUR_LIGNE
			+ DessinateurLiens.MARGE;

	public static int MARGE = DessinateurLiens.MARGE;

	public static Font PLAIN_FONT;

	private Stroke basicStroke = new BasicStroke(4);

	private Color couleurSurlignage = new Color(155, 155, 155, 155);

	private Point editorLocation;

	private Vector enfants = new Vector(0);

	private Lien l;;

	private int largeur = LIGNE;

	private int largeurTexte;

	private Vector lignesTexte = new Vector(0);

	private int maxChars;

	private int numeroDessin;

	private Rectangle rectangleEtendu = new Rectangle(-1, -1, 0, 0);

	private Rectangle rectangleSurvol = new Rectangle(-1, -1, 0, 0);

	private boolean selectionne = false;

	private boolean surlignage;

	private boolean survol = false;

	private int x0, x1;

	private int[] xF;

	private int xMax;

	private int y1;

    private int[] yF;

	int yFmax;

	private int yFmin;

	public DessinLien(Lien lien, int xMax, int nbCharsMaxi, int largeurTexte, int largeurDessin) {
		l = lien;
		this.xMax = xMax;
		this.maxChars = nbCharsMaxi;
		this.largeurTexte = largeurTexte;
        this.largeur = largeurDessin; 
	}

	public void ajouterEnfant(DessinLien dlEnfant) {
		enfants.add(dlEnfant);

	}

	public void calculerCoordonnees() {
		// if(l.getFils1()== null && l.getFils2()==null && l.getPere() == null)
		// return;

		x0 = xMax - (l.getProfondeur() * largeur);
		x1 = x0 + largeur;

		if (!l.estFeuille()) {
			xF = new int[enfants.size()];
			yF = new int[enfants.size()];
			yFmin = 99999999;
			yFmax = 0;
			for (int i = 0; i < enfants.size(); i++) {
				DessinLien enfant = (DessinLien) enfants.get(i);
				xF[i] = xMax - (enfant.getLien().getProfondeur() * largeur);
				yF[i] = enfant.y1;
				yFmin = Math.min(yFmin, yF[i]);
				yFmax = Math.max(yFmax, yF[i]);
				rectangleSurvol = new Rectangle(x0 + MARGE, yFmin, largeur, Math
						.abs(yFmax - yFmin));

				y1 = (int) ((yFmin + yFmax) / 2);
				editorLocation = new Point(x0, y1 - LIGNE);
			}
		} else {

			// On partitionne le texte
			diviserTexte();
			// La hauteur dépend du nombre de lignes
			int hauteur = (int) (Math.max(2, lignesTexte.size()) * LIGNE) / 2;
			yFmax = yFmin + hauteur;
			y1 = (int) ((yFmin + yFmax) / 2);
			rectangleSurvol = new Rectangle(x0 + MARGE, yFmin, largeurTexte,
					yFmax - yFmin);
		}

		rectangleEtendu = new Rectangle(rectangleSurvol.x - 3 * largeur,
				rectangleSurvol.y - 3 * LIGNE, rectangleSurvol.width + 6
						* largeur, rectangleSurvol.height + 6 * LIGNE);

	}

	public void changementTexteLien() {
		lignesTexte.clear();
		diviserTexte();
	}

	public void dessinerLien(Graphics g, boolean avecSelections) {
		// if(l.getFils1()== null && l.getFils2()==null && l.getPere() == null)
		// return;
		Graphics2D g2 = (Graphics2D) g;
		Color ancienneColor = g2.getColor();
		Stroke oldStroke = g2.getStroke();

		if (surlignage) {
			g2.setColor(couleurSurlignage);
			g2.fillRect(rectangleSurvol.x, rectangleSurvol.y,
					rectangleSurvol.width, rectangleSurvol.height);
		}

		if (selectionne && avecSelections) {
			g2.setStroke(basicStroke);
		}

		g2.setColor(Color.black);

		if (l.getPere() != null || !l.estFeuille()) {
			g2.drawLine(x0, y1, x1, y1);
		}

		if (selectionne && avecSelections) {
			g2.setFont(BOLD_FONT);
		} else {
			g2.setFont(PLAIN_FONT);
		}

		if (!l.estFeuille()) {
			// Ligne verticale
			g2.drawLine(x1, yFmin, x1, yFmax);

			// Lignes horizontales
			for (int i = 0; i < xF.length; i++) {
				int x = xF[i];
				int y = yF[i];
				g2.drawLine(x1, y, x, y);
			}

			g2.drawString(l.getTexte() + numeroDessin, x0 + MARGE, y1 - MARGE);

		} else {
			// ecrireTexte(g2, l.getTexte(), x1, y1 + LIGNE/4);
			int y = yFmin + LIGNE / 2;
			if (lignesTexte.size() == 1) {
				y = yFmin + LIGNE / 2 + LIGNE / 4;
			}
			for (int i = 0; i < lignesTexte.size(); i++) {
				String texte = (String) lignesTexte.get(i);
				g2.drawString(texte, x1, y);
				y = y + (LIGNE / 2);
			}

			if (selectionne && avecSelections) {
				g2.drawRect(rectangleSurvol.x, rectangleSurvol.y,
						rectangleSurvol.width, rectangleSurvol.height);
			}
		}

		g2.setStroke(oldStroke);

		if (survol && avecSelections) {
			g2.setColor(Color.red.brighter().brighter());
			g2.drawRect(rectangleSurvol.x, rectangleSurvol.y,
					rectangleSurvol.width, rectangleSurvol.height);
		}

		g2.setColor(ancienneColor);
	}

	// private void ecrireTexte(Graphics2D g2, String texte, int x, int y) {
	// String texte1 = texte;
	// String texte2 = null;
	// if(texte.length()>MAX_TXT_LEN) {
	// int idx = texte.indexOf(" ", MAX_TXT_LEN);
	// if(idx!=-1){
	// texte1 = texte.substring(0,idx);
	// texte2 = texte.substring(idx);
	// }
	// }
	// g2.drawString(texte1, x, y);
	// if(texte2!=null){
	// g2.drawString(texte2, x, y + LIGNE/2);
	// }
	//		
	//		
	// }

	private void diviserTexte() {

		String grandeLigne = l.getTexte().trim();
		while (grandeLigne.length() > maxChars) {
			int index = grandeLigne.lastIndexOf(" ", maxChars);
			if (index == -1) {
				break;
			} else {
				lignesTexte.add(grandeLigne.substring(0, index));
				grandeLigne = grandeLigne.substring(index);
			}
		}
		lignesTexte.add(grandeLigne);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg0) {
		Lien l1 = getLien();
		Lien l2 = ((DessinLien) arg0).getLien();
		return l1.equals(l2);
	}

	public Point getEditorLocation() {
		return editorLocation;
	}

	public Lien getLien() {
		return l;
	}

	public int getNumeroDessin() {
		return numeroDessin;
	}

	/**
	 * @return the rectangleEtendu
	 */
	public Rectangle getRectangleEtendu() {
		return rectangleEtendu;
	}

	public Rectangle getRectangleSurvol() {
		return rectangleSurvol;
	}

	/**
	 * @return the selectionne
	 */
	public boolean isSelectionne() {
		return selectionne;
	}

	/**
	 * @return the survol
	 */
	public boolean isSurvol() {
		return survol;
	}

	public void setLargeur(int l){
        boolean recalc = false;
        if(l!=largeur){
            recalc = true;
        }
        largeur = l;
        if(recalc){
            calculerCoordonnees();
        }
    }

	public void setNumeroDessin(int numero) {
		numeroDessin = numero;
	}

	/**
	 * @param selectionne
	 *            the selectionne to set
	 */
	public void setSelectionne(boolean selectionne) {
		this.selectionne = selectionne;
	}

	/**
	 * @param surlignage
	 *            the surlignage to set
	 */
	public void setSurlignage(boolean surlignage) {
		this.surlignage = surlignage;
	}

	/**
	 * @param survol
	 *            the survol to set
	 */
	public void setSurvol(boolean estSurvol) {
		this.survol = estSurvol;
	}
    
    public void setYOrigine(int y) {
		yFmin = y;
	}

}
