package fr.megiste.interloc.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;

import fr.megiste.interloc.InterlocMain;
import fr.megiste.interloc.hist.ErreurRecupHistoriqueException;
import fr.megiste.interloc.hist.HistLien;
import fr.megiste.interloc.hist.HistSupp;
import fr.megiste.interloc.hist.Historique;
import fr.megiste.interloc.ihm.DessinLien;

public class ModeleLiens extends DefaultListModel {

    private static Logger logger = InterlocMain.getLogger();

    private boolean modif = false;

    private Vector historique = new Vector(0);

    private Stack pileDeRedo = new Stack();

    private HashMap mapClefsLiens = new HashMap();

    private Vector clefsLiensSurlignes = new Vector(0);

    private boolean ignoreModifs = false;

    /**
     * @return the clefsLiensSurlignes
     */
    public Vector getClefsLiensSurlignes() {
        return clefsLiensSurlignes;
    }

    public ModeleLiens(Vector liens) {
        for (Iterator iter = liens.iterator(); iter.hasNext();) {
            Lien lien = (Lien) iter.next();
            addElement(lien);
        }

    }

    public void creerNouveauLien(Vector selects) throws CreerNouveauLienException {
        // On cherche � voir si les deux liens ne sont pas d�j� li�s : ie s'ils
        // n'ont pas un anc�tre commun.
        Vector patriarches = new Vector(0);
        for (int i = 0; i < selects.size(); i++) {
            DessinLien dl = (DessinLien) selects.get(i);
            Lien patriarche = dl.getLien().getPatriarche();
            boolean aEnlever = false;
            for (int j = 0; j < i; j++) {
                Lien p = (Lien) patriarches.get(j);
                if (p.equals(patriarche)) {
                    aEnlever = true;
                    break;
                }
            }
            if (aEnlever) {
                selects.remove(dl);
                i--;
            } else {
                patriarches.add(patriarche);
            }
        }
        if (selects.size() < 2) {
            throw new CreerNouveauLienException("Les objets � lier sont d�j� li�s...");
        }

        Vector liensSelectionnes = new Vector();
        for (int i = 0; i < selects.size(); i++) {
            Lien l = ((DessinLien) selects.get(i)).getLien();
            liensSelectionnes.add(l);
        }

        Lien newLink = fabriquerNouveauLien(null, liensSelectionnes);

        Vector vEnfants = new Vector();
        for (int i = 0; i < newLink.getNombreEnfants(); i++) {
            vEnfants.add(newLink.getFils(i).getClef());
        }
        // On cr�� l'�l�ment d'historique
        HistLien hl = new HistLien(newLink.getClef(), vEnfants, newLink.getTexte());
        historique.add(hl);

    }

    private Lien fabriquerNouveauLien(String clef, Vector liensSelectionnes) throws CreerNouveauLienException {
        boolean tousUnPere = true;
        Lien[] peres = new Lien[liensSelectionnes.size()];
        int[] numerosFils = new int[liensSelectionnes.size()];
        int compteur = 0;
        for (Iterator iter = liensSelectionnes.iterator(); iter.hasNext();) {
            Lien lien = (Lien) iter.next();
            if (lien.getPere() == null) {
                tousUnPere = false;
                numerosFils[compteur] = -1;
            } else {
                peres[compteur] = lien.getPere();
                numerosFils[compteur] = lien.getPere().getNumeroFils(lien);

            }
            compteur++;
        }

        if (tousUnPere) {
            throw new CreerNouveauLienException("Tous les �lements s�lectionn�s ont d�j� un p�re...");
        }
        // Cr�ation du nouveau lien.

        Lien newLink;
        if (clef == null) {
            newLink = new Lien(liensSelectionnes);
        } else {
            newLink = new Lien(clef, liensSelectionnes);
        }

        for (int i = 0; i < numerosFils.length; i++) {
            int num = numerosFils[i];
            if (num != -1) {
                peres[i].setFils(num, newLink);
            }

        }

        setModif(true);
        addElement(newLink);

        logger.fine("Nouveau lien cr��");
        return newLink;
    }

    public void traiterHistorique(Historique hist) throws ErreurRecupHistoriqueException {
        if (hist.getPrefixe().equals(Historique.PREFIXE_LIEN)) {
            HistLien histLien = (HistLien) hist;
            traiterInsertionLien(histLien);
        } else if (hist.getPrefixe().equals(Historique.PREFIXE_SUPP)) {
            HistSupp hSupp = (HistSupp) hist;
            traiterSuppressionLien(hSupp);
        }
    }

    private void traiterSuppressionLien(HistSupp supp) {
        Lien l1 = (Lien) mapClefsLiens.get(supp.getClef());
        supprimerLien(l1, supp.getReco());

    }

    private void traiterInsertionLien(HistLien histLien) throws ErreurRecupHistoriqueException {
        Vector liens = new Vector(0);
        for (int i = 0; i < histLien.getClefsEnfants().length; i++) {
            Lien l = (Lien) mapClefsLiens.get(histLien.getClefsEnfants()[i]);
            liens.add(l);
        }

        try {
            Lien nouveauLien = fabriquerNouveauLien(histLien.getClef(), liens);
            nouveauLien.setTexte(histLien.getTexte());
        } catch (CreerNouveauLienException e) {
            throw new ErreurRecupHistoriqueException(e.getMessage());
        }

    }

    public void supprimerLien(Lien lien, int reconnexion) {
        supprimerLien2(lien, reconnexion);
        HistSupp hSupp = new HistSupp(lien.getClef(), reconnexion);
        historique.add(hSupp);
    }

    public void supprimerLien2(Lien lien, int reconnexion) {
        lien.nettoyerConnexions();
        if (reconnexion != -1) {
            Lien pere = lien.getPere();
            int numeroLienChezPere = pere.getNumeroFils(lien);
            pere.setFils(numeroLienChezPere, lien.getFils(reconnexion - 1));
        }
        setModif(true);
        removeElement(lien);

    }

    public int getNbFeuilles() {
        int nbFeuilles = 0;
        for (int i = 0; i < size(); i++) {
            Lien lien = (Lien) getElementAt(i);
            if (lien.estFeuille()) {
                nbFeuilles++;
            }
        }
        return nbFeuilles;

    }

    public void initLiens(Vector liensCharges, Vector surlignages) {
        clear();
        for (Iterator iter = liensCharges.iterator(); iter.hasNext();) {
            Lien lien = (Lien) iter.next();
            addElement(lien);
        }
        clefsLiensSurlignes = surlignages;
    }

    public void modifierTexteLien(Lien lien, String text) {
        lien.setTexte(text);
        setModif(true);
        fireContentsChanged(this, 0, size() - 1);

    }

    /**
     * @return the modif
     */
    public boolean isModif() {
        return modif;
    }

    /**
     * @param modif
     *            the modif to set
     */
    public void setModif(boolean modif) {
        if (!ignoreModifs) {
            this.modif = modif;
        }
        fireContentsChanged(this, 0, size() - 1);
    }

    public Vector getFeuilles() {
        Vector feuilles = new Vector(0);
        for (int i = 0; i < size(); i++) {
            Lien lien = (Lien) getElementAt(i);
            if (lien.estFeuille()) {
                feuilles.add(lien);
            }
        }
        return feuilles;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.DefaultListModel#addElement(java.lang.Object)
     */
    public void addElement(Object obj) {
        super.addElement(obj);
        mapClefsLiens.put(((Lien) obj).getClef(), obj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.DefaultListModel#clear()
     */
    public void clear() {
        super.clear();
        mapClefsLiens.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.DefaultListModel#remove(int)
     */
    public Object remove(int index) {
        Lien sortie = (Lien) super.remove(index);
        mapClefsLiens.remove(sortie.getClef());
        return sortie;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.DefaultListModel#removeAllElements()
     */
    public void removeAllElements() {
        super.removeAllElements();
        mapClefsLiens.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.DefaultListModel#removeElement(java.lang.Object)
     */
    public boolean removeElement(Object obj) {
        Lien lien = (Lien) obj;
        mapClefsLiens.remove(lien.getClef());
        return super.removeElement(obj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.DefaultListModel#removeElementAt(int)
     */
    public void removeElementAt(int index) {
        Lien lien = (Lien) getElementAt(index);
        mapClefsLiens.remove(lien.getClef());
        super.removeElementAt(index);
    }

    public void initHistorique(Vector historiqueCharge) throws ErreurRecupHistoriqueException {
        historique = historiqueCharge;
        for (Iterator iter = historiqueCharge.iterator(); iter.hasNext();) {
            Historique h = (Historique) iter.next();
            traiterHistorique(h);

        }
    }

    public Vector getHistorique() {
        return historique;
    }

    public Vector getTousLiens() {
        Vector liens = new Vector(0);
        for (int i = 0; i < size(); i++) {
            Lien lien = (Lien) getElementAt(i);
            liens.add(lien);
        }
        return liens;
    }

    public void creerElementRedo(Lien dernierLien) {
        HistLien hLien = new HistLien(dernierLien.getClef(), dernierLien.getClefsEnfants(), dernierLien.getTexte());
        pileDeRedo.push(hLien);
    }

    public void rejouer() throws ErreurRecupHistoriqueException {
        if (pileDeRedo.size() != 0) {
            HistLien hLien = (HistLien) pileDeRedo.pop();
            traiterHistorique(hLien);
        }
    }

    public Vector getPileDeRedo() {
        return pileDeRedo;
    }

    public int getMaxIndexFeuilleAvecParent() {
        int indexMax = -1;
        for (int i = 0; i < size(); i++) {
            Lien lien = (Lien) getElementAt(i);
            if (lien.estFeuille()) {
                if (lien.getPere() != null) {
                    indexMax = Math.max(indexMax, lien.getIndex());
                }
            }
        }
        // System.out.println("Out :" + indexMax);
        return indexMax;
    }

    public void setIgnoreModifs(boolean b) {
        ignoreModifs = b;
    }

    public int getProfondeurMax() {
        int profondeurMax = 0;
        for (int i = 0; i < size(); i++) {
            Lien lien = (Lien) getElementAt(i);

            if (lien.getProfondeur() > profondeurMax) {
                profondeurMax = lien.getProfondeur();
            }
        }
        return profondeurMax;
    }

}
