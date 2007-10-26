package fr.megiste.interloc.rtf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;

import javax.imageio.ImageIO;

import fr.megiste.interloc.data.ModeleLiens;
import fr.megiste.interloc.util.InterlocException;

public class HTMLExporter extends DocumentExporter {

    private Writer fw;

    private File file;

    public HTMLExporter(ModeleLiens modele) {
        super(modele);
    }


    void closeDocument() {
        

    }

    private void writeLine(String s) {
        try {
            fw.write(s + "\n");
        } catch (IOException e) {
            throw new InterlocException(e);
        }

    }



    void initDocument(File outputFile) {
        try {
            file = outputFile;
            FileOutputStream fos = new FileOutputStream(outputFile);
            fw = new OutputStreamWriter(fos,"UTF-8");
            
        } catch (IOException e) {
            throw new InterlocException(e);
        }

    }

    void insertImage(BufferedImage subImage, String id) {
        try {
            File imgRep = new File(file.getParentFile(), file.getName() + "_images");
            if (!imgRep.exists()) {
                imgRep.mkdirs();
            }
            File fichierImage = new File(imgRep, "imageTmp" + id + ".jpg");
            ImageIO.write(subImage, "jpg", fichierImage);
            String imgPath = imgRep.getName() + "/" + fichierImage.getName();

            writeLine("<tr><td><img src=\"" + imgPath + "\"/></td></tr>");
        } catch (IOException e) {
            throw new InterlocException(e);
        }

    }


	public void writeImagePacks() {
		writeLine("<html><head><meta http-equiv=\"Content-Type\" Content=\"text/html; charset=UTF-8\"></head><body>");
		for (Iterator iter = packs.iterator(); iter.hasNext();) {
			ImagePack pack = (ImagePack) iter.next();
			if(pack.getTitle()!=null){
				writeLine("<h1>" + pack.getTitle() + "</h1>");	
			}
			writeLine("<table border=0 cellpadding=0 cellspacing=0>");
			for(int i=0;i<pack.getImages().size();i++){
				insertImage((BufferedImage) pack.getImages().get(i),""+i);
			}
			writeLine("</table>");
			
			
			if(pack.getCommentary()!=null){
				writeLine("<h1>" + pack.getCommentary() + "</h1>");	
			}
			
			
			
		}
		writeLine("</body></html>");
        try {

            fw.close();
        } catch (IOException e) {
            throw new InterlocException(e);
        }
		
	}

}
