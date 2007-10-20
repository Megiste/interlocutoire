package fr.megiste.interloc.rtf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfWriter2;

import fr.megiste.interloc.data.ModeleLiens;
import fr.megiste.interloc.util.InterlocException;

public class RTFExporter extends DocumentExporter {

	public static final String SUFFIX_RTF = "rtf";
	static final double PROPORTION_L_H = 1.4d;
	
    Font policeRtfTitre = new Font(Font.TIMES_ROMAN, 16, Font.BOLD);
    Font policeRtfTexte = new Font(Font.TIMES_ROMAN, 12, Font.NORMAL);
    private Document document;
    private FileOutputStream fos;
	static Logger logger = Logger.getLogger(RTFExporter.class.getName());

	public RTFExporter(ModeleLiens modele) {
		super(modele);
	}

    void initDocument(File outputFile) {
        
        try {
            document = new Document();
            fos = new FileOutputStream(outputFile);
            RtfWriter2.getInstance(document, fos);
            
            document.open();
        } catch (FileNotFoundException e) {
            throw new InterlocException(e);
        }        
    }

    void closeDocument() {
        try {
            document.close();
            fos.close();
        } catch (IOException e) {
            throw new InterlocException(e);
        }
        
    }

    void addTitleText(String txt) {
        try {
            document.add(new Paragraph(txt,policeRtfTitre));
        } catch (DocumentException e) {
            throw new InterlocException(e);
        }
        
    }

    void addSimpleText(String txt) {
        try {
            document.add(new Paragraph(txt,policeRtfTexte));
        } catch (DocumentException e) {
            throw new InterlocException(e);
        }
        
    }

    void insertImage(BufferedImage subImage, String id) {
        try {
            File fichierImage = new File(workingDir ,"./imageTmp" + id + ".jpg");
            ImageIO.write(subImage, "jpg", fichierImage);
            
            Image jpg = Image.getInstance(fichierImage.getPath());
            jpg.setDeflated(true);
            jpg.setSpacingAfter((float) 0.0);
            jpg.setSpacingBefore((float) 0.0);
            jpg.setAlignment(Image.TEXTWRAP);
            //Chunk c = new Chunk(jpg,0,0);
            document.add(jpg);
        } catch (BadElementException e) {
            throw new InterlocException(e);
        } catch (MalformedURLException e) {
            throw new InterlocException(e);
        } catch (IOException e) {
            throw new InterlocException(e);
        } catch (DocumentException e) {
            throw new InterlocException(e);
        }
        
    }

}
