package fr.megiste.interloc.ihm;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileFilter;

import fr.megiste.interloc.InterlocMain;
import fr.megiste.interloc.data.AccesseurFichierHOM;
import fr.megiste.interloc.data.CreerNouveauLienException;
import fr.megiste.interloc.data.ErreurEcritureException;
import fr.megiste.interloc.data.LectureHOMException;
import fr.megiste.interloc.data.Lien;
import fr.megiste.interloc.data.ModeleLiens;
import fr.megiste.interloc.hist.ErreurRecupHistoriqueException;
import fr.megiste.interloc.rtf.DocumentExporter;
import fr.megiste.interloc.rtf.ErreurExportException;
import fr.megiste.interloc.rtf.RTFExporter;
import fr.megiste.interloc.rtf.VelocityExporter;
import fr.megiste.interloc.rtf.DocumentExporter.AffichageHelper;
import fr.megiste.interloc.util.Messages;

public class InterlocIhm extends JFrame {

	private abstract class InterlocAction extends AbstractAction {

		private boolean activeEtat = false;

		private JComponent bouton = null;

		public InterlocAction(String key) {
			super(Messages.getInstance().getValue(key));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(final ActionEvent arg0) {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			afficherInfo("message.actionencours", new Object[] { getValue(Action.NAME) });

			new Thread() {
				public void run() {
					try {
						executer(arg0);
					} catch (Exception e) {
						e.printStackTrace();
						afficherInfo(e);
					} catch (Throwable e) {
						traitementErreur(e);

					}
				}
			}.start();

			afficherInfo(null, null);
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		public abstract void executer(ActionEvent arg0) throws Exception;

		public JComponent getBouton() {
			return bouton;
		}

		public boolean isActiveEtat() {
			return activeEtat;
		}

		public void setActiveEtat(boolean activeEtat) {
			this.activeEtat = activeEtat;
		}

		public void setBouton(JComponent bouton) {
			this.bouton = bouton;
		}

		public void setEnabled(boolean newValue) {

			super.setEnabled(newValue);
			if (bouton != null) {
				// bouton.setEnabled(newValue);
				// bouton.revalidate();
				// bouton.repaint();
				if (newValue) {
					bouton.setForeground(Color.black);
				} else {
					bouton.setForeground(Color.gray);
				}

			}
		}

	}

	private static final String BANNIERE = "Interloc by <Megiste> V "
			+ InterlocMain.readInterlocVersion();

	public static final FileFilter HOM_FILE_FILTER = new FileFilter() {

		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			else
				return f.getName().endsWith(AccesseurFichierHOM.SUFFIX_HOM);
		}

		public String getDescription() {
			return "Fichier *.hom : textes d'homélies";
		}

	};

	private static Logger logger = InterlocMain.getLogger();

	private static final int PANNEAU_BAS = 1;

	private static final int PANNEAU_HAUT = 0;

	public static final FileFilter EXPORT_FILE_FILTER = new FileFilter() {

		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			else
				return  f.getName().toLowerCase().endsWith("html");
		}

		public String getDescription() {
			return "Fichier *.html";
		}

	};
	public static final FileFilter IMAGE_FILE_FILTER = new FileFilter() {

		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			else
				return f.getName().toLowerCase().endsWith("gif")
						|| f.getName().toLowerCase().endsWith("jpg")
						|| f.getName().toLowerCase().endsWith(IMAGE_TYPE);
		}

		public String getDescription() {
			return "Fichier image";
		}

	};

	private static final String IMAGE_TYPE = "jpg";

	private InterlocAction actionLier;

	private InterlocAction actionSupprimer;

	private String charsetCourant = AccesseurFichierHOM.CHARSET;

	private JFileChooser chooser;

	private JFileChooser chooserExport;

	private int compteurCharsets;

	private DessinateurLiens dessinateur;

	private File fichierCourant = null;

	private JLabel labelInfo = new JLabel();

	private File lastExportedPicture;

	private File lastExportSelectedFile;

	private File lastExportTotalSelectedFile;

	private Vector listeCharsets;

	private boolean modeSurlignage = false;

	private JPanel panneauBoutonsBas = new JPanel();

	private JPanel panneauBoutonsHaut = new JPanel();

	private JPanel panneauDialogue = new JPanel();

	private File repertoireDefaut = new File(".");

	private JScrollPane scrollPane;

	private JLabel taillePile = new JLabel("", JLabel.RIGHT);

	private JButton testTailles;

	private AffichageHelper affichageHelper = new AffichageHelper() {

		public void afficherMessage(final String message, final Object[] values) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					afficherInfo(message, values);
				}
			});
		}

		public void afficherProgres(final int noEtape) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					progressBar.setValue(noEtape);
				}
			});

		}

		public void finish() {
			progressBar.setValue(0);
			progressBar.setVisible(false);

		}

		public void init(final int nbEtapes) {
			/*
			 * progressBar.setMinimum(0); progressBar.setMaximum(nbEtapes);
			 */
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					progressBar.setMinimum(0);
					progressBar.setVisible(true);
					progressBar.setMaximum(nbEtapes);
				}
			});

		}

	};

	private JProgressBar progressBar;

	private JFileChooser chooserExportImage;

	private int afficherChoix(String message, String[] strings) {
		int choix = -1;
		String valeurChoisie = (String) JOptionPane.showInputDialog(this, message, "Choisir...",
				JOptionPane.QUESTION_MESSAGE, null, strings, strings[0]);
		for (int i = 0; i < strings.length; i++) {
			String val = strings[i];
			if (val.equals(valeurChoisie)) {
				choix = i + 1;
				break;
			}

		}

		return choix;
	}

	protected void afficherInfo(Exception e) {
		String message = "ERREUR:" + e.getMessage();
		logger.warning(e.getMessage());
		afficherInfo(message, null);
	}

	private void afficherInfo(String clef, Object[] values) {
		if (clef == null)
			labelInfo.setText("");
		else {
			String message = null;
			if (values != null) {
				message = Messages.getInstance().getValue(clef, values);
			} else {
				message = Messages.getInstance().getValue(clef);
			}
			labelInfo.setText(message);
		}
		((JComponent) getContentPane()).revalidate();
	}

	private void afficherTaillePile() {

		int t = dessinateur.getModel().getPileDeRedo().size();
		if (t > 0) {
			taillePile.setText("       " + t);
		} else {
			taillePile.setText("");
		}

	}

	protected void changerCharset() throws LectureHOMException, ErreurRecupHistoriqueException {
		if (listeCharsets == null) {
			listeCharsets = new Vector(0);
			listeCharsets.addAll(Charset.availableCharsets().keySet());
			compteurCharsets = 0;
		}
		if (compteurCharsets >= listeCharsets.size()) {
			compteurCharsets = 0;
		}
		charsetCourant = (String) listeCharsets.get(compteurCharsets);
		compteurCharsets++;

		chargeFichier(fichierCourant);
		afficherInfo("message.rechargementcharset", new String[] { fichierCourant.getPath(),
				charsetCourant });
		dessinateur.getModel().setModif(true);
	}

	public void chargeFichier(File selectedFile) throws LectureHOMException,
			ErreurRecupHistoriqueException {
		logger.info("Chargement de :" + selectedFile);
		repertoireDefaut = selectedFile.getParentFile();
		fichierCourant = selectedFile;

		AccesseurFichierHOM accesseurFichierHOM = new AccesseurFichierHOM(charsetCourant);

		accesseurFichierHOM.chargerLiens(selectedFile);

		dessinateur.getModel().initLiens(accesseurFichierHOM.getLiensCharges(),
				accesseurFichierHOM.getSurlignages());

		// dessinateur.getModel().initHistorique(accesseurFichierHOM.getHistoriqueCharge());

		setTitle(BANNIERE + " - " + fichierCourant.getName() + " ");

	}

	/*
	 * protected void copierImage() { logger.info("Copie d'image...");
	 * 
	 * Icon icon = new ImageIcon((BufferedImage) dessinateur.creerImage()); final JLabel label = new
	 * JLabel(icon); label.setTransferHandler(new ImageTransferable()); TransferHandler handler =
	 * label.getTransferHandler(); Toolkit kit = Toolkit.getDefaultToolkit(); final Clipboard
	 * clipboard = kit.getSystemClipboard(); handler.exportToClipboard(label, clipboard,
	 * TransferHandler.COPY);
	 * 
	 * afficherInfo("Image copiée dans le presse papier", null);
	 * dessinateur.nettoyerSelectionExport(); }
	 */

	public void creerNouveauLien() throws CreerNouveauLienException {
		if (dessinateur.getDlSelectionnes().size() < 2) {
			throw new CreerNouveauLienException("Pas assez d'objets selectionn�s...");
		} else {
			dessinateur.getModel().creerNouveauLien(new Vector(dessinateur.getDlSelectionnes()));
		}
		dessinateur.getDlSelectionnes().clear();
		dessinateur.getModel().getPileDeRedo().clear();
	}

	private void enregistrerAction(String chars, InterlocAction action, boolean invisible,
			int quelPanneau) {
		JComponent comp = (JComponent) getContentPane();
		String nom = (String) action.getValue(Action.NAME);

		String texteBouton = "<html>" + nom + " <b>(" + chars + ")</b></html>";

		KeyStroke ks = null;
		if (!invisible) {
			JComponent bouton;

			if (action.isActiveEtat()) {
				bouton = new JToggleButton(action);
				((JToggleButton) bouton).setText(texteBouton);
			} else {
				bouton = new JButton(action);
				((AbstractButton) bouton).setText(texteBouton);
			}
			action.setBouton(bouton);
			JPanel panneau;
			if (quelPanneau == PANNEAU_BAS) {
				panneau = panneauBoutonsBas;
			} else {
				panneau = panneauBoutonsHaut;
			}
			panneau.add(bouton);

			String tchars = "typed " + chars;
			ks = KeyStroke.getKeyStroke(tchars);
			if (ks == null) {
				ks = KeyStroke.getKeyStroke(chars);
			}
		} else {
			ks = KeyStroke.getKeyStroke(chars);
			// ks = KeyStroke.getKeyStroke("control " + c);
			// ks = KeyStroke.getKeyStroke(new
			// Character(c),java.awt.event.InputEvent.CTRL_MASK);
		}

		if (ks == null) {
			logger.severe("Impossible de trouver un ks pour :" + chars);
			InterlocMain.quitter(1, "Impossible de trouver un ks pour :" + chars);
		}
		comp.getInputMap().put(ks, nom);
		comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, nom);
		comp.getInputMap(JComponent.WHEN_FOCUSED).put(ks, nom);
		comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ks, nom);
		comp.getActionMap().put(nom, action);

		// ((JComponent) getContentPane()).registerKeyboardAction(action, nom,
		// ks,
		// JComponent.WHEN_IN_FOCUSED_WINDOW);
		// dessinateur.registerKeyboardAction(action, nom, ks,
		// JComponent.WHEN_IN_FOCUSED_WINDOW);

	}

	protected void exporterFichierDansDocWord() throws ErreurExportException {
		if (lastExportTotalSelectedFile != null) {
			chooserExport.setSelectedFile(lastExportTotalSelectedFile);
		} else {
			chooserExport.setSelectedFile(new File(fichierCourant.getParentFile(), fichierCourant
					.getName().replaceAll("." + AccesseurFichierHOM.SUFFIX_HOM,
							"." + RTFExporter.SUFFIX_RTF)));
		}
		int returnVal = chooserExport.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			DocumentExporter exporteur = new RTFExporter(dessinateur.getModel());

			// exporteur.exportTotal(chooserExport.getSelectedFile());

			dessinateur.initMinMaxIndexes();
			exporteur.exportTotal(chooserExport.getSelectedFile(), dessinateur.getIndiceMin(),
					dessinateur.getIndiceMax());

			afficherInfo("message.exportOK", new String[] { chooserExport.getSelectedFile()
					.getPath() });
			lastExportTotalSelectedFile = chooserExport.getSelectedFile();
		}

	}

	protected void exporterFichierEnImage() throws IOException {

		RenderedImage rendImage = dessinateur.creerImage();

		File repertoireImages = new File(repertoireDefaut, "images");
		if (lastExportedPicture != null && lastExportedPicture.getParentFile().exists()) {
			repertoireImages = lastExportedPicture.getParentFile();
		}

		if (!repertoireImages.exists() || !repertoireImages.isDirectory()) {
			repertoireImages.mkdirs();
		}

		String nomBaseFichierImage = fichierCourant.getName().replaceAll(
				"." + AccesseurFichierHOM.SUFFIX_HOM, "");
		if (dessinateur.getIndiceMax() > 0) {
			nomBaseFichierImage = nomBaseFichierImage + "_" + dessinateur.getIndiceMin() + "_"
					+ dessinateur.getIndiceMax();
		}

		File fichierImage = new File(repertoireImages, nomBaseFichierImage + "." + IMAGE_TYPE);
		int compteur = 1;
		while (fichierImage.exists()) {
			fichierImage = new File(repertoireImages, nomBaseFichierImage + "_" + compteur + "."
					+ IMAGE_TYPE);
			compteur++;
		}

		chooserExportImage.setSelectedFile(fichierImage);
		int returnVal = chooserExportImage.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			ImageIO.write(rendImage, IMAGE_TYPE, chooserExportImage.getSelectedFile());
			afficherInfo("message.exportOK", new String[] { chooserExportImage.getSelectedFile()
					.getPath() });
			
			dessinateur.nettoyerSelectionExport();
			lastExportedPicture = chooserExport.getSelectedFile();
		}

	}

	private void exporterPasAPas() throws ErreurExportException {
		dessinateur.initMinMaxIndexes();
		String id = "";
		if (dessinateur.getIndiceMax() != 0) {
			id = "_" + dessinateur.getIndiceMin() + "_" + dessinateur.getIndiceMax();
		}

		String filename = "pas_a_pas_"
				+ fichierCourant.getName().replaceAll("\\." + AccesseurFichierHOM.SUFFIX_HOM,
						id + "." + DocumentExporter.SUFFIX_HTML);

		if (lastExportSelectedFile != null) {
			chooserExport
					.setSelectedFile(new File(lastExportSelectedFile.getParentFile(), filename));
		} else {
			chooserExport.setSelectedFile(new File(fichierCourant.getParentFile(), filename));
		}
		int returnVal = chooserExport.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			DocumentExporter exporteur = new VelocityExporter(dessinateur.getModel());
			exporteur.setAffichageHelper(affichageHelper);
			exporteur.exporterPasAPas(chooserExport.getSelectedFile(), dessinateur.getIndiceMin(),
					dessinateur.getIndiceMax());
			lastExportSelectedFile = chooserExport.getSelectedFile();
			afficherInfo("message.exportppOK", new String[] { chooserExport.getSelectedFile()
					.getPath() });
		}

	}

	/**
	 * @return the fichierCourant
	 */
	public File getFichierCourant() {
		return fichierCourant;
	}

	public Point getPosScrollPane() {
		return scrollPane.getViewport().getViewPosition();
	}

	private String getTexteLien(Lien lien) {
		if (lien.estFeuille())
			return lien.getTexte();

		DessinLien dl = dessinateur.getDessinLien(lien, -1);

		return lien.getTexte() + dl.getNumeroDessin();
	}

	public void init() {

		setTitle(BANNIERE);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent arg0) {
				if (dessinateur.getModel().isModif()) {
					int res = JOptionPane.showConfirmDialog(InterlocIhm.this,
							"Vous n'avez pas sauvé...\nVoulez vous quitter quand même?",
							"Confirmation", JOptionPane.YES_NO_OPTION);
					if (res == JOptionPane.YES_OPTION) {
						InterlocMain.quitter();
					}
				} else {
					InterlocMain.quitter();
				}
			}
		});

		JPanel panneauPrincipal = new JPanel();
		panneauPrincipal.setLayout(new BoxLayout(panneauPrincipal, BoxLayout.Y_AXIS));

		// panneauBoutonsHaut.setLayout(new BoxLayout(panneauBoutonsHaut,
		// BoxLayout.X_AXIS));
		// panneauBoutonsBas.setLayout(new BoxLayout(panneauBoutonsBas,
		// BoxLayout.X_AXIS));
		//		
		panneauBoutonsHaut.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panneauBoutonsBas.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panneauDialogue.setLayout(new BoxLayout(panneauDialogue, BoxLayout.X_AXIS));

		dessinateur = new DessinateurLiens(new ModeleLiens(new Vector()));

		testTailles = new JButton("click ! 800 * 800");
		testTailles.setSize(800, 800);
		testTailles.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int largeur = testTailles.getSize().width;
				int hauteur = testTailles.getSize().height;

				largeur = largeur - 100;
				hauteur = hauteur + 100;

				testTailles.setPreferredSize(new Dimension(largeur, hauteur));

				testTailles.setText("" + testTailles.getSize().width + ","
						+ testTailles.getSize().height);

			}

		});

		scrollPane = new JScrollPane(dessinateur);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panneauPrincipal.add(panneauBoutonsHaut);
		panneauPrincipal.add(panneauBoutonsBas);
		panneauPrincipal.add(panneauDialogue);
		panneauPrincipal.add(scrollPane);
		JPanel panneauBas = new JPanel(new FlowLayout(FlowLayout.LEFT));

		panneauBas.add(labelInfo);
		progressBar = new JProgressBar();
		progressBar.setVisible(false);
		panneauBas.add(progressBar);
		panneauPrincipal.add(panneauBas);

		setContentPane(panneauPrincipal);
		logger.info("Ajout objets graphiques OK");

		initCommandes();
		initCommandesInvisibles();

		initChooser();
		initChooserExport();
		initChooserExportImage();

		panneauBoutonsHaut.add(taillePile);

		logger.info("Ajout commandes OK");
		dessinateur.getModel().addListDataListener(new ListDataListener() {

			public void contentsChanged(ListDataEvent arg0) {
				majTitre();
			}

			public void intervalAdded(ListDataEvent arg0) {
				majTitre();

			}

			public void intervalRemoved(ListDataEvent arg0) {
				majTitre();
			}
		});

		scrollPane.getViewport().addComponentListener(new ComponentAdapter() {

			int ancienneLargeur = -1;

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
			 */
			public void componentResized(ComponentEvent e) {
				int nouvelleLargeur = scrollPane.getViewport().getSize().width;
				if (ancienneLargeur != nouvelleLargeur) {
					ancienneLargeur = nouvelleLargeur;
					dessinateur.setLargeurReference(nouvelleLargeur);
					dessinateur.redessiner();
					// logger.info("taille VP : " +
					// scrollPane.getViewport().getSize().width + ","
					// +scrollPane.getViewport().getSize().height);

				}

			}

		});
	}

	private void initChooser() {
		chooser = new JFileChooser(repertoireDefaut);
		chooser.setFileFilter(HOM_FILE_FILTER);
	}

	private void initChooserExport() {
		chooserExport = new JFileChooser(repertoireDefaut);
		chooserExport.setFileFilter(EXPORT_FILE_FILTER);
	}

	private void initChooserExportImage() {
		chooserExportImage = new JFileChooser(repertoireDefaut);
		chooserExportImage.setFileFilter(IMAGE_FILE_FILTER);
	}

	private void initCommandes() {

		InterlocAction action = null;

		action = new InterlocAction("label.bouton.ouvrir") {
			public void executer(ActionEvent arg0) throws Exception {
				ouvrirFichier();
			}
		};
		enregistrerAction("o", action, false, PANNEAU_HAUT);

		action = new InterlocAction("label.bouton.enregistrer") {
			public void executer(ActionEvent arg0) throws Exception {
				sauverFichier(fichierCourant);
			}
		};
		enregistrerAction("e", action, false, PANNEAU_HAUT);

		action = new InterlocAction("label.bouton.enregss") {
			public void executer(ActionEvent arg0) throws Exception {
				sauverFichierSous();
			}
		};
		enregistrerAction("F12", action, false, PANNEAU_HAUT);

		actionLier = new InterlocAction("label.bouton.lier") {
			public void executer(ActionEvent arg0) throws Exception {
				creerNouveauLien();
				afficherTaillePile();
			}

		};
		enregistrerAction("l", actionLier, false, PANNEAU_HAUT);

		actionSupprimer = new InterlocAction("label.bouton.supprimer") {
			public void executer(ActionEvent arg0) throws Exception {
				supprimerLienCourant();
				afficherTaillePile();
			}
		};
		enregistrerAction("d", actionSupprimer, false, PANNEAU_HAUT);

		action = new InterlocAction("label.bouton.surligner") {
			public void executer(ActionEvent arg0) throws Exception {
				surlignage();
			}
		};
		action.setActiveEtat(true);
		enregistrerAction("s", action, false, PANNEAU_BAS);

		// action = new InterlocAction("Copier") {
		// public void executer(ActionEvent arg0) {
		// copierImage();
		// }
		// };
		// enregistrerAction("c", action, false);

		// action = new InterlocAction("label.bouton.exporter") {
		// public void executer(ActionEvent arg0) throws Exception {
		// exporterFichierDansDocWord();
		// }
		//
		// };
		// enregistrerAction("x", action, false, PANNEAU_BAS);

		action = new InterlocAction("label.bouton.exporthtml") {
			public void executer(ActionEvent arg0) throws Exception {
				exporterFichierDansDocHTML();
			}

		};
		enregistrerAction("x", action, false, PANNEAU_BAS);

		action = new InterlocAction("label.bouton.image") {
			public void executer(ActionEvent arg0) throws Exception {
				exporterFichierEnImage();
			}
		};
		enregistrerAction("i", action, false, PANNEAU_BAS);

		action = new InterlocAction("label.bouton.pasapas") {
			public void executer(ActionEvent arg0) throws Exception {
				exporterPasAPas();
			}

		};
		enregistrerAction("p", action, false, PANNEAU_BAS);
	}

	protected void exporterFichierDansDocHTML() throws ErreurExportException {
		dessinateur.initMinMaxIndexes();
		String id = "";
		if (dessinateur.getIndiceMax() != 0) {
			id = "_" + dessinateur.getIndiceMin() + "_" + dessinateur.getIndiceMax();
		}

		String filename = fichierCourant.getName().replaceAll("." + AccesseurFichierHOM.SUFFIX_HOM,
				id + "." + DocumentExporter.SUFFIX_HTML);

		if (lastExportTotalSelectedFile != null) {
			chooserExport.setSelectedFile(new File(lastExportTotalSelectedFile.getParentFile(),
					filename));
		} else {

			chooserExport.setSelectedFile(new File(fichierCourant.getParentFile(), filename));
		}

		int returnVal = chooserExport.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			DocumentExporter exporteur = new VelocityExporter(dessinateur.getModel());

			// exporteur.exportTotal(chooserExport.getSelectedFile());

			exporteur.exportTotal(chooserExport.getSelectedFile(), dessinateur.getIndiceMin(),
					dessinateur.getIndiceMax());

			afficherInfo("message.exportOK", new String[] { chooserExport.getSelectedFile()
					.getPath() });
			lastExportTotalSelectedFile = chooserExport.getSelectedFile();
		}
	}

	private void initCommandesInvisibles() {
		InterlocAction action = null;

		action = new InterlocAction("label.commandeinvisible.recharger") {
			public void executer(ActionEvent arg0) throws Exception {
				changerCharset();
			}
		};

		enregistrerAction("shift R", action, true, PANNEAU_HAUT);

		action = new InterlocAction("label.commandeinvisible.pdown") {
			public void executer(ActionEvent arg0) throws Exception {
				scroll(scrollPane.getViewport().getSize().height - 2 * DessinLien.LIGNE);
			}
		};
		enregistrerAction("SPACE", action, true, PANNEAU_HAUT);

		action = new InterlocAction("label.commandeinvisible.scrollup") {
			public void executer(ActionEvent arg0) throws Exception {
				scroll(-200);
			}
		};
		enregistrerAction("UP", action, true, PANNEAU_HAUT);

		action = new InterlocAction("label.commandeinvisible.scrolldown") {
			public void executer(ActionEvent arg0) throws Exception {
				scroll(200);
			}
		};
		enregistrerAction("DOWN", action, true, PANNEAU_HAUT);

		action = new InterlocAction("label.commandeinvisible.scrollup2") {
			public void executer(ActionEvent arg0) throws Exception {
				scroll(-600);
			}
		};
		enregistrerAction("PAGE_UP", action, true, PANNEAU_HAUT);

		action = new InterlocAction("label.commandeinvisible.scrolldown2") {
			public void executer(ActionEvent arg0) throws Exception {
				scroll(600);
			}
		};
		enregistrerAction("PAGE_DOWN", action, true, PANNEAU_HAUT);

		action = new InterlocAction("label.commandeinvisible.redessiner") {
			public void executer(ActionEvent arg0) throws Exception {

				dessinateur.redessiner();

				logger.info("Hauteur dessinateur " + dessinateur.getSize().height);
				logger.info("Hauteur vue " + scrollPane.getViewport().getViewSize().height);
			}
		};
		enregistrerAction("F5", action, true, PANNEAU_HAUT);

		action = new InterlocAction("label.commandeinvisible.enarriere") {
			public void executer(ActionEvent arg0) throws Exception {
				dessinateur.reculer(1);
				afficherTaillePile();
			}
		};
		enregistrerAction("LEFT", action, true, PANNEAU_HAUT);

		action = new InterlocAction("label.commandeinvisible.enavant") {
			public void executer(ActionEvent arg0) throws Exception {
				dessinateur.avancer(1);
				afficherTaillePile();
			}
		};
		enregistrerAction("RIGHT", action, true, PANNEAU_HAUT);

	}

	private void majTitre() {
		// logger.info("Maj titre : " +dessinateur.getModel().isModif() );
		setTitle(BANNIERE + " - " + fichierCourant.getName() + " "
				+ (dessinateur.getModel().isModif() ? "*" : ""));

	}

	private void ouvrirFichier() throws LectureHOMException, ErreurRecupHistoriqueException {
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			chargeFichier(chooser.getSelectedFile());
			dessinateur.redessiner();
		}
	}

	protected void sauverFichier(File nomFichierSauvegarde) throws ErreurEcritureException {
		Vector liensASauver = new Vector();
		for (int i = 0; i < dessinateur.getModel().size(); i++) {
			Lien l = (Lien) dessinateur.getModel().getElementAt(i);
			liensASauver.add(l);
		}

		AccesseurFichierHOM accesseur = new AccesseurFichierHOM(charsetCourant);
		accesseur.setLiensCharges(liensASauver);
		accesseur.setHistoriqueCharge(dessinateur.getModel().getHistorique());
		accesseur.setSurlignages(dessinateur.getModel().getClefsLiensSurlignes());

		accesseur.enregistrer(nomFichierSauvegarde);

		afficherInfo("message.sauvegarde", new Object[] { nomFichierSauvegarde.getName() });
		dessinateur.getModel().setModif(false);
		majTitre();

	}

	protected void sauverFichierSous() throws ErreurEcritureException {
		chooser.setSelectedFile(fichierCourant);
		int returnVal = chooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File fileDest = chooser.getSelectedFile();
			boolean enregOK = true;
			if (fileDest.exists()) {
				int res = JOptionPane.showConfirmDialog((JComponent) this.getContentPane(), ""
						+ fileDest.getName() + " existe. Voulez-vous le remplacer?", "Confirmer",
						JOptionPane.YES_NO_OPTION);
				if (res == JOptionPane.NO_OPTION) {
					enregOK = false;
				}
			}
			if (enregOK) {
				sauverFichier(fileDest);
				fichierCourant = fileDest;
				majTitre();
			}
		}
	}

	protected void scroll(int scroll) {
		logger.info("scroll : " + scroll);
		Point pos = getPosScrollPane();
		pos.y = pos.y + scroll;
		if (pos.y < 0)
			pos.y = 0;
		if (pos.y + scrollPane.getViewport().getSize().height > dessinateur.getSize().height) {
			pos.y = dessinateur.getSize().height - scrollPane.getViewport().getSize().height + 2
					* DessinLien.LIGNE;
		}
		setPosScrollPane(pos);
	}

	public void setPosScrollPane(Point point) {
		scrollPane.getViewport().setViewPosition(point);
	}

	void supprimerLienCourant() throws SupprimerLienException {
		if (dessinateur.getDlSelectionnes().size() == 0)
			throw new SupprimerLienException("Selectionner au moins un lien");

		Lien lien = null;
		for (Iterator iter = dessinateur.getDlSelectionnes().iterator(); iter.hasNext();) {
			DessinLien dl = (DessinLien) iter.next();
			if (!dl.getLien().estFeuille()) {
				lien = dl.getLien();
				break;
			}
		}
		if (lien == null)
			throw new SupprimerLienException("Aucun lien selectionn� supprimable");
		dessinateur.getDlSelectionnes().clear();
		int reconnexion = -1;
		if (lien.getPere() != null) {
			String[] choix = new String[lien.getNombreEnfants()];
			for (int i = 0; i < choix.length; i++) {
				choix[i] = getTexteLien(lien.getFils(i));
			}

			String message = "Connecter [" + lien.getPere().getTexte() + "] avec";
			reconnexion = afficherChoix(message, choix);
			if (reconnexion != -1) {
				dessinateur.getModel().supprimerLien(lien, reconnexion);
				dessinateur.getModel().getPileDeRedo().clear();
			}

		} else {
			dessinateur.getModel().supprimerLien(lien, -1);
			dessinateur.getModel().getPileDeRedo().clear();
		}

	}

	protected void surlignage() {
		modeSurlignage = !modeSurlignage;
		actionLier.setEnabled(!modeSurlignage);
		actionSupprimer.setEnabled(!modeSurlignage);
		dessinateur.setModeSurlignage(modeSurlignage);

	}

	public void traitementErreur(Throwable e) {
		JOptionPane.showInternalMessageDialog(this.getContentPane(), "Erreur de type : "
				+ e.getClass().getName() + " [" + e.getMessage()
				+ "]\n Contacter laurent@kloetzer.fr et lui envoyer le fichier : "
				+ InterlocMain.NOM_FICHIER_JOURNAL);
		InterlocMain.erreur(e);
	}

}
