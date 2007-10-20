package fr.megiste.interloc.ihm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import fr.megiste.interloc.InterlocMain;
import fr.megiste.interloc.data.Lien;
import fr.megiste.interloc.data.ModeleLiens;
import fr.megiste.interloc.hist.ErreurRecupHistoriqueException;

public class DessinateurLiens extends JPanel {
	
	//private static final int IMG_MAX_W = 800;

	private ModeleLiens model = null;
	
	private static Logger logger = InterlocMain.getLogger();
	
	private HashMap mapDessinsLiens = new HashMap();
	
	private DessinLien dlSurvol = null;

	private Vector dlSelectionnes = new Vector(0);
	
	private DessinLien dessinsLiens[] = null;

	private JComboBox editor;

	private Rectangle rectangleExport = null;

	private Color couleurSelectionExport = new Color(0,0,0,128);

	public static int MARGE = 2;

	public static int HAUTEUR_LIGNE = 20;

	//private DefaultListSelectionModel selectionModel;
	
	private int largeurPartieDessin = 200;
	
	private int nbCharsMaxi = 0;

	private FontMetrics fontMetrics;

	private int largeurTexte;

	private int indiceMax;

	private int indiceMin;

	private Dimension tailleCalculee = new Dimension(200,200);

	private int largeurReference;
	
	private static String TEST = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

	private boolean modeSurlignage = false;
    
    private int largeurDessinMax = -1;
    
    private int largeurDessinLien = DessinLien.LIGNE;
    
    
    

    public DessinateurLiens(ModeleLiens modele){
		setLayout(null);
		setLargeurReference(800);
		this.model = modele;
		
		model.addListDataListener(new ListDataListener(){
			public void contentsChanged(ListDataEvent e) {
				redessiner();
			}


			public void intervalAdded(ListDataEvent e) {
				redessiner();
				
			}

			public void intervalRemoved(ListDataEvent e) {
				redessiner();
			}
			
		});
		
		addMouseMotionListener(new MouseMotionListener(){

			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseMoved(MouseEvent e) {
				DessinLien dl = trouverLienSelonPoint(e.getPoint());
				if(dl!=dlSurvol){
					if(dl!=null){
						dl.setSurvol(true);
						repeindre(dl);
						//logger.info("Rectangle survol y,h = " + dl.getRectangleSurvol().y + "," + dl.getRectangleSurvol().height);
					}
					if(dlSurvol!=null){
						dlSurvol.setSurvol(false);
						repeindre(dlSurvol);
					}
					dlSurvol = dl;
					//if(dlSurvol!=null) logger.info("dlSurvol=" + dl.getLien().getClef());
				}
			}
		});
		
		addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				int c = e.getClickCount();
				DessinLien dl = trouverLienSelonPoint(e.getPoint());
				if(!modeSurlignage){
					if(c>1 && dl!=null && !dl.getLien().estFeuille()){
						afficherSaisie(dl);
					} else {
						if(dl!=null && dlSelectionnes.contains(dl)){
							dlSelectionnes.remove(dl);
							repeindre(dl);
						} else if(dl!=null && !dlSelectionnes.contains(dl)){
							dlSelectionnes.add(dl);
							repeindre(dl);
							if((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != MouseEvent.SHIFT_DOWN_MASK){
								while(dlSelectionnes.size()>2){
									DessinLien dl2 = (DessinLien) dlSelectionnes.remove(0); //On enleve le premier � chaque fois.
									repeindre(dl2);
								}
							}
						} else if(dl==null){
							for(int i=dlSelectionnes.size()-1;i>=0;i--){
								DessinLien dls = (DessinLien) dlSelectionnes.remove(i);
								repeindre(dls);
							}
						}
						if(editor!=null) editor.setVisible(false);
						
					}
				} else {
					dlSelectionnes.clear();
					if(dl!=null) surlignage(dl);
				}
			}
		});
		
		DessinLien.BOLD_FONT = getFont().deriveFont(Font.BOLD);
		DessinLien.PLAIN_FONT = getFont().deriveFont(Font.PLAIN);
	}
	
	private void initTailles() {
		tailleCalculee.width = largeurReference;
		
		int profondeurMax = model.getProfondeurMax();
        
		largeurPartieDessin = (profondeurMax + 2) * DessinLien.LIGNE;
        if(largeurDessinMax>-1 && largeurPartieDessin > largeurDessinMax){
            largeurPartieDessin = largeurDessinMax;
            initLargeurDessinLien(largeurPartieDessin / (profondeurMax + 2));
        } else if(largeurPartieDessin< 200) {
            largeurPartieDessin = 200;
        }
		
		largeurTexte = tailleCalculee.width - largeurPartieDessin ;
		if(largeurTexte < 200) largeurTexte = 200;

		if(fontMetrics==null) {
			fontMetrics = getFontMetrics(getFont());	
		}
		int tailleTexte = fontMetrics.stringWidth(TEST);
		if(tailleTexte < largeurTexte){
			while(tailleTexte < largeurTexte + DessinLien.LIGNE){
				TEST  = TEST + "A";
				tailleTexte = fontMetrics.stringWidth(TEST);
			}
		} else if (tailleTexte > largeurTexte){
			while(tailleTexte > largeurTexte && tailleTexte > 100){
				TEST = TEST.substring(0,TEST.length()-1);
				int newtailleTexte = fontMetrics.stringWidth(TEST);
				if(newtailleTexte>tailleTexte){
					logger.severe("je ma encore gourré sur le substring");
				}
				tailleTexte = newtailleTexte;
			}
		}
		nbCharsMaxi = (int) (TEST.length() * 1.3);
		
	}

	private void initLargeurDessinLien(int l) {
        largeurDessinLien = l;
        if(dessinsLiens!=null){
            for (int i = 0; i < dessinsLiens.length; i++) {
                DessinLien dl = dessinsLiens[i];
                dl.setLargeur(l);
            }
        }
    }

    protected void afficherSaisie(DessinLien dl) {
		if(editor==null){
			//editor = new JTextField();
			editor = new JComboBox();
			editor.addItem("I");
			editor.addItem("E");
			
			
			setBorder(BorderFactory.createEmptyBorder());
			//editor.setSize(2 * HAUTEUR_LIGNE,(int) (HAUTEUR_LIGNE * 1.5));
			
			editor.setSize(2 * HAUTEUR_LIGNE,(int) (HAUTEUR_LIGNE));
			editor.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent arg0) {
					DessinLien dl = (DessinLien) ((JComponent) arg0.getSource()).getClientProperty("machin");
					if(dl!=null){
						String nouveauNom = (String) editor.getSelectedItem();
						model.modifierTexteLien(dl.getLien(),nouveauNom);
						editor.setVisible(false);
						editor.putClientProperty("machin", null);
						doLayout();
					}
				}
			});
			
			editor.addKeyListener(new KeyAdapter(){

				/* (non-Javadoc)
				 * @see java.awt.event.KeyAdapter#keyPressed(java.awt.event.KeyEvent)
				 */
				public void keyPressed(KeyEvent arg0) {
					if(arg0.getKeyCode()==KeyEvent.VK_ESCAPE){
						editor.setVisible(false);
						doLayout();
					}
				}
			});
		}
		editor.setLocation(dl.getEditorLocation());
		//editor.setText(dl.getLien().getTexte());
		editor.setSelectedItem(dl.getLien().getTexte());
		editor.putClientProperty("machin", dl);
		
		add(editor);
		editor.setVisible(true);
		doLayout();
		
	}

	public void redessiner() {
		mapDessinsLiens.clear();
		
		initTailles(); 
		
		
		initDessinsLiens();

		setPreferredSize(getTailleCalculee());
		setSize(getTailleCalculee());
		
		//logger.info("nlle tailles panneau : " + getSize().width + "," + getSize().height);
		
		revalidate();
		repaint();
		
	}

	private void repeindre(DessinLien dl) {
		dl.setSelectionne(dlSelectionnes.contains(dl));
		dl.setSurlignage(model.getClefsLiensSurlignes().contains(dl.getLien().getClef()));
		paintImmediately(dl.getRectangleEtendu());
		
	}
	

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		dessinerLiens(g, true);
		
		dessinerRectangleSelection(g);
		

	}



	private void dessinerRectangleSelection(Graphics g) {
		if(rectangleExport!=null){
			Color ancienneCol = g.getColor();
			g.setColor(couleurSelectionExport );
			g.fillRect(rectangleExport.x, rectangleExport.y, rectangleExport.width, rectangleExport.height);
			g.setColor(ancienneCol);
		}
		
	}

	private void dessinerLiens(Graphics g, boolean avecSelections) {
		for(int i=0;i<dessinsLiens.length;i++){
			DessinLien dessinLien = dessinsLiens[i];
            if(dessinLien!=null){
                dessinLien.setNumeroDessin(i+1-model.getNbFeuilles());
                dessinLien.setSelectionne(dlSelectionnes.contains(dessinLien));
                dessinLien.setSurlignage(model.getClefsLiensSurlignes().contains(dessinLien.getLien().getClef()));
                dessinLien.dessinerLien(g, avecSelections);
            }
		}
	}

	private DessinLien trouverLienSelonPoint(Point point) {
		if(dessinsLiens==null){
			initDessinsLiens();
		}
		for (int i = 0; i < dessinsLiens.length; i++) {
			DessinLien dl = dessinsLiens[i];
			if(dl.getRectangleSurvol().contains(point)){
				return dl;
			}
		}
		return null;
	}

	DessinLien getDessinLien(Lien lien, int yOrigine) {
		DessinLien dl = (DessinLien) mapDessinsLiens.get(lien.getClef());
		if(dl==null){
			dl = new DessinLien(lien,largeurPartieDessin,nbCharsMaxi,largeurTexte,largeurDessinLien);
			dl.setYOrigine(yOrigine);
			Lien[] enfants = lien.getTousFils();
			for (int i = 0; i < enfants.length; i++) {
				Lien enfant = enfants[i];
				DessinLien dlEnfant = getDessinLien(enfant,-1);
				dl.ajouterEnfant(dlEnfant);
			}
			mapDessinsLiens.put(lien.getClef(),dl);
			dl.calculerCoordonnees();
		}
		return dl;
	}

	/**
	 * @return the model
	 */
	public ModeleLiens getModel() {
		return model;
	}
	
	private void initDessinsLiens(){
		dessinsLiens = new DessinLien[model.size()];
		
		//On calcule la hauteur
		int compteur=0;
		
		int hauteur = 0;
		
		int yOrigine = 0;
		for(int i=0;i<model.size();i++){
			Lien lien = (Lien)model.getElementAt(i);
			
			if(lien.estFeuille()){
				DessinLien dl = getDessinLien(lien,yOrigine);

				hauteur = Math.max(hauteur, dl.getRectangleSurvol().y + dl.getRectangleSurvol().height ); 
				yOrigine = dl.getRectangleSurvol().y + dl.getRectangleSurvol().height;
				
				dessinsLiens[compteur++] = dl;
			}
		}

		for(int i=0;i<model.size();i++){
			Lien lien = (Lien)model.getElementAt(i);
			if(!lien.estFeuille()){
				DessinLien dl = getDessinLien(lien,-1);
				dessinsLiens[compteur++] = dl;
			}
		}

		
		for(int i=0;i<model.size();i++){
			DessinLien dl = getDessinLien((Lien)model.getElementAt(i),-1);
			dl.setNumeroDessin(i+1-model.getNbFeuilles());
		}
		hauteur = hauteur +  DessinLien.LIGNE;
		tailleCalculee.height = hauteur;
		
		//logger.info("Nouvelle tc : " + tailleCalculee.width + "," + tailleCalculee.height);
		
		
		
		
		
		//setPreferredSize(tailleCalculee);
		//setSize(tailleCalculee);
		//setMinimumSize(taille);
	}
	



	/**
	 * @return the dlSelectionnes
	 */
	public Vector getDlSelectionnes() {
		return dlSelectionnes;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
//	public Dimension getPreferredSize() {
//		int largeur =super.getPreferredSize().width;
//		int hauteur = (model.getNbFeuilles() * (HAUTEUR_LIGNE + MARGE));
//		return new Dimension(largeur,hauteur);
//	}	

	public void initMinMaxIndexes(){
		indiceMin = Integer.MAX_VALUE;
		indiceMax = 0;
        if(dlSelectionnes.size()>=2){
        	for (Iterator iter = dlSelectionnes.iterator(); iter.hasNext();) {
				DessinLien dl = (DessinLien) iter.next();
				if(dl.getLien().estFeuille()){
					indiceMin = Math.min(indiceMin, dl.getLien().getIndex());
					indiceMax = Math.max(indiceMax, dl.getLien().getIndex());
				}
			}
        }

	}
	
	public BufferedImage creerImage() {
		if(dessinsLiens==null) initDessinsLiens();
        //int width = Math.min(getSize().width,IMG_MAX_W);
		int width = getSize().width;
        int height = getSize().height;		
		
		 //On regarde si on doit extraire une partie
		
		
        int compteFeuilles = 0;
        int yMin=height, yMax = 0;
		indiceMin = Integer.MAX_VALUE;
		indiceMax = 0;
        if(dlSelectionnes.size()>=2){
        	for (Iterator iter = dlSelectionnes.iterator(); iter.hasNext();) {
				DessinLien dl = (DessinLien) iter.next();
				if(dl.getLien().estFeuille()){
					compteFeuilles++;
					yMin = Math.min(yMin,dl.getRectangleSurvol().y);
					yMax = Math.max(yMax,dl.getRectangleSurvol().y + dl.getRectangleSurvol().height);
					
					indiceMin = Math.min(indiceMin, dl.getLien().getIndex());
					indiceMax = Math.max(indiceMax, dl.getLien().getIndex());
				}
			}
        }
        
        if(compteFeuilles<2){
        	rectangleExport = new Rectangle(0,0,width,height);
        } else {
        	rectangleExport = new Rectangle(0, yMin,width, yMax-yMin);
        }
        
        paintImmediately(rectangleExport);
        		
		

    
        // Create a buffered image in which to draw
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    
        // Create a graphics contents on the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();
    
        // Draw graphics
        
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, width, height);

        dessinerLiens(g2d,false);
    
        // Graphics context no longer needed so dispose it
        g2d.dispose();
        
       
        
        if(compteFeuilles==2){
        	bufferedImage = bufferedImage.getSubimage(0, yMin,width, yMax-yMin);
        }
    
        return bufferedImage;
    }

	public void nettoyerSelectionExport(){
		
		Rectangle zone = new Rectangle(rectangleExport);
		rectangleExport = null;
		paintImmediately(zone);
		
	}

	public int[] getCoupuresPossibles() {
		int[] sortie = new int[model.getNbFeuilles()];
		for(int i=0;i<model.size();i++){
			Lien l = (Lien) model.getElementAt(i);
			if(l.estFeuille()){
				DessinLien dl = getDessinLien(l, -1);
				sortie[i]=dl.yFmax;
				
			}
		}
		return sortie;
	}

	/**
	 * @return the indiceMax
	 */
	public int getIndiceMax() {
		return indiceMax;
	}

	/**
	 * @return the indiceMin
	 */
	public int getIndiceMin() {
		return indiceMin;
	}

	public void surlignage(DessinLien dl) {
		String clef = dl.getLien().getClef();
		if(!model.getClefsLiensSurlignes().contains(clef)){
			model.getClefsLiensSurlignes().add(clef);
		} else{
			model.getClefsLiensSurlignes().remove(clef);	
		}
		
		repeindre(dl);
		model.setModif(true);
		
	}
	
	

	public void setModeSurlignage(boolean modeSurlignage) {
		this.modeSurlignage = modeSurlignage;
	}

	public Dimension getTailleCalculee() {
		return tailleCalculee;
	}

	/**
	 * @param largeurReference the largeurReference to set
	 */
	public void setLargeurReference(int largeurReference) {
		this.largeurReference = largeurReference;
	}

	public void reculer(int nbPas) {
		
		for(int i=0;i<nbPas;i++){
			if(model.getSize()>model.getNbFeuilles()){
				Lien dernierLien = (Lien) model.getElementAt(model.getSize()-1);
				if(dernierLien.getPere()!=null){
					model.supprimerLien(dernierLien, 1);
					
				} else {
					model.supprimerLien(dernierLien, -1);
				}
				model.creerElementRedo(dernierLien);
			}

		}
		redessiner();
	}

	public void avancer(int nbPas) throws ErreurRecupHistoriqueException {
		for(int i=0;i<nbPas;i++){
			model.rejouer();
		}
		
		
	}

    /**
     * @param largeurDessinMax the largeurDessinMax to set
     */
    public void setLargeurDessinMax(int largeurDessinMax) {
        this.largeurDessinMax = largeurDessinMax;
    }
	
    

}
