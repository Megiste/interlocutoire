package fr.megiste.interloc.rtf;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;

import fr.megiste.interloc.InterlocMain;
import fr.megiste.interloc.data.ModeleLiens;
import fr.megiste.interloc.ihm.DessinateurLiens;
import fr.megiste.interloc.util.Messages;

public abstract class DocumentExporter {

    public interface AffichageHelper {
        public void afficherMessage(String message, Object[] objects);

        public void afficherProgres(int noEtape);
        
        public void init(int nbEtapes);
        
        public void finish();
    }

    
	protected ArrayList packs = new ArrayList();

	private ImagePack currentImagePack;

    protected static final int LARGEUR_IMAGE_EXPORT = 600;

    static Logger logger = Logger.getLogger(DocumentExporter.class.getName());

    public static final String SUFFIX_HTML = "html";

    public static BufferedImage scale(BufferedImage bi, double scaleValue) {
        AffineTransform tx = new AffineTransform();
        tx.scale(scaleValue, scaleValue);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        BufferedImage biNew = new BufferedImage((int) (bi.getWidth() * scaleValue),
                (int) (bi.getHeight() * scaleValue), bi.getType());
        return op.filter(bi, biNew);

    }

    private AffichageHelper affichageHelper;

    protected DessinateurLiens dessinateur;

    protected ModeleLiens modele;

    protected ModeleLiens modeleTmp;

    protected File workingDir = new File("./img.tmp");

    public DocumentExporter(ModeleLiens modele) {
        this.modele = modele;
        // feuilles = modele.getFeuilles();
        if (!workingDir.exists()) {
            workingDir.mkdirs();
        }
        packs=new ArrayList();
    }

    private void dessinerEtape(int noEtape) {
        logger.info("Pas à pas : " + noEtape);
        String message = "label.pasapas";
        affichageHelper.afficherMessage(message,new Object[]{""+noEtape});
        affichageHelper.afficherProgres(noEtape);
        
        newImagePack();
        currentImagePack.setTitle("Etape " + noEtape + " :");

        BufferedImage rendImage = dessinateur.creerImage();

        splitAndInsertImages(rendImage, noEtape);

        currentImagePack.setCommentary(Messages.getInstance().getValue("texte.commentaireetape",new Object[]{""+noEtape}));
    }

    public void exporterPasAPas(File outputFile, int phraseMin, int phraseMax) throws ErreurExportException {
        if (phraseMax == 0){
        	phraseMax = Integer.MAX_VALUE;
        }
        if (phraseMin == Integer.MAX_VALUE) {
        	phraseMin = 0;
        }
        logger.info("Export pas à pas...");
        if (outputFile.exists()){
        	outputFile.delete();
        }
            

        try {
            initDocument(outputFile);

            modele.setIgnoreModifs(true);
            int nbEtapes = modele.size() - modele.getNbFeuilles();
            affichageHelper.init(nbEtapes);
            modeleTmp = modele;
            dessinateur = new DessinateurLiens(modeleTmp);
            dessinateur.setLargeurDessinMax(300);
            dessinateur.setLargeurReference(600);
            dessinateur.redessiner();
            dessinateur.reculer(nbEtapes);

            for (int i = 0; i < nbEtapes; i++) {
                // modeleTmp.addElement(modele.getElementAt(i +
                // feuilles.size()));
                dessinateur.avancer(1);
                if (modele.getMaxIndexFeuilleAvecParent() > phraseMin
                        && modele.getMaxIndexFeuilleAvecParent() <= phraseMax) {
                    dessinerEtape(i + 1);
                }
            }
            writeImagePacks();
            //closeDocument();
        } catch (Exception e) {
            InterlocMain.erreur(e);
            throw new ErreurExportException(e.getMessage());
        }
        modele.setIgnoreModifs(false);
        logger.info("Export pp OK...");
        affichageHelper.finish();
    }

    public abstract void writeImagePacks(); 

	public void exportTotal(File outputFile, int phraseMin, int phraseMax) throws ErreurExportException {
        if (phraseMax == 0){
        	phraseMax = Integer.MAX_VALUE;
        }
        if (phraseMin == Integer.MAX_VALUE){
        	phraseMin = 0;
        }
        logger.info("Export total...");
        if (outputFile.exists()){
        	outputFile.delete();
        }
            

        try {
            initDocument(outputFile);
            newImagePack();
            currentImagePack.setTitle("Export total");
            modele.setIgnoreModifs(true);
            //int nbEtapes = modele.size() - modele.getNbFeuilles();
            modeleTmp = modele;
            dessinateur = new DessinateurLiens(modeleTmp);
            dessinateur.setLargeurDessinMax(300);
            dessinateur.setLargeurReference(600);
            dessinateur.reculer(1);
            dessinateur.avancer(1);
            BufferedImage rendImage = dessinateur.creerImage();
            int[] cuts = dessinateur.getCoupuresPossibles();
            int oldpos = 0;
            for (int i = 0; i < cuts.length; i++) {
                if (i < phraseMin || i > phraseMax) {
                    continue;
                }
                int pos = cuts[i];
                int width = rendImage.getWidth();
                int y = oldpos;
                int h = pos - oldpos;
                if (y + h > rendImage.getHeight()) {
                    h = rendImage.getHeight() - y;
                }
                if (h < 0)
                    continue;
                BufferedImage subImage = rendImage.getSubimage(0, y, width, h);
                /**File fichierImage = new File(workingDir, "./imageTmp" + "_" + i + ".jpg");
                ImageIO.write(subImage, "jpg", fichierImage);*/
                
                currentImagePack.getImages().add(subImage);

//                insertImage(subImage, "" + i);

                oldpos = pos;
            }
            writeImagePacks();
            //closeDocument();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ErreurExportException(e.getMessage());
        }
        modele.setIgnoreModifs(true);
        logger.info("Export total OK...");
    }

    
//    public File exportTotal(File outputFile) throws ErreurExportException {
//        logger.info("Export total...");
//        // File fichierRTF = new
//        // File(fichierCourant.getParentFile(),fichierCourant.getName().replaceAll("."
//        // + AccesseurFichierHOM.SUFFIX_HOM, "_total." + SUFFIX_RTF));
//        if (outputFile.exists())
//            outputFile.delete();
//
//        try {
//            initDocument(outputFile);
//
//            modeleTmp = new ModeleLiens(new Vector(0));
//            dessinateur = new DessinateurLiens(modeleTmp);
//            dessinateur.setLargeurReference(LARGEUR_IMAGE_EXPORT);
//
//            modeleTmp.initLiens(modele.getTousLiens(), modele.getClefsLiensSurlignes());
//            dessinateur.redessiner();
//
//            BufferedImage rendImage = dessinateur.creerImage();
//            // Eventuellement, on découpe en plusieurs images
//            int hauteurSousImage = (int) (PROPORTION_L_H * LARGEUR_IMAGE_EXPORT);
//            int hauteurImage = rendImage.getHeight();
//            if (hauteurImage > hauteurSousImage) {
//                int[] cuts = dessinateur.getCoupuresPossibles();
//                int y = 0;
//                while (y < hauteurImage) {
//                    int hauteur = hauteurSousImage;
//                    if (y + hauteurSousImage > hauteurImage) {
//                        hauteur = hauteurImage - y;
//                    } else {
//                        // On tente de trouver la coupure appropriee
//                        int coupure = 0;
//                        for (int i = 0; i < cuts.length - 1; i++) {
//                            if (cuts[i] < y + hauteur && cuts[i + 1] > y + hauteur) {
//                                coupure = cuts[i];
//                            }
//                        }
//                        hauteur = coupure - y;
//                    }
//                    BufferedImage sousImage = rendImage.getSubimage(0, y, LARGEUR_IMAGE_EXPORT, hauteur);
//                    insererImageDansDocument(sousImage, document);
//                    // document.newPage();
//                    y = y + hauteur;
//                }
//            } else {
//                insererImageDansDocument(rendImage, document);
//            }
//
//            // Step 5: Close the document. It will be written to the target
//            // output stream.
//            document.close();
//            return outputFile;
//        } catch (Exception e) {
//            InterlocMain.erreur(e);
//            throw new ErreurExportException(e.getMessage());
//        }
//
//    }

    private void newImagePack() {
		currentImagePack = new ImagePack();
		packs.add(currentImagePack);
		
	}

	abstract void initDocument(File outputFile);

//    private void insererImageDansDocument(BufferedImage image, Document document) throws IOException, DocumentException {
//
//        // On retaille l'image de manière à ce qu'elle aille à la taille max
//        // dans le doc.
//        // double scale = (double) (750d / (double) LARGEUR_IMAGE_EXPORT);
//
//        // image = scale(image,scale);
//
//        File fichierImage = File.createTempFile("imageInterloc", ".jpg");
//
//        // File fichierImage = new File("./imageTmp" + noEtape + ".jpg");
//
//        logger.info("insertion de " + fichierImage);
//        ImageIO.write(image, "jpg", fichierImage);
//
//        Image jpg = Image.getInstance(fichierImage.getPath());
//        jpg.scaleToFit(750f, 700f);
//
//        // jpg.setWidthPercentage((float) 0.5);
//        // jpg.scalePercent(0.5f);
//        // jpg.scaleAbsolute(200, 200);
//        document.add(jpg);
//
//        fichierImage.delete();
//    }
    
    /**
     * @param affichageHelper the affichageHelper to set
     */
    public void setAffichageHelper(AffichageHelper affichageHelper) {
        this.affichageHelper = affichageHelper;
    }
    
    private void splitAndInsertImages(BufferedImage rendImage, int noEtape) {
        int[] cuts = dessinateur.getCoupuresPossibles();
        int oldpos = 0;
        for (int i = 0; i < cuts.length; i++) {
            if (i >= modeleTmp.getMaxIndexFeuilleAvecParent()) {
                continue;
            }
            int pos = cuts[i];
            int width = rendImage.getWidth();
            BufferedImage subImage = rendImage.getSubimage(0, oldpos, width, pos - oldpos);
            currentImagePack.getImages().add(subImage);
            //insertImage(subImage, "" + noEtape + "_" + i);
            oldpos = pos;
        }

    }


}
