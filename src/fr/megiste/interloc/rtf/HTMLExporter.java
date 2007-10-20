package fr.megiste.interloc.rtf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.imageio.ImageIO;

import fr.megiste.interloc.data.ModeleLiens;
import fr.megiste.interloc.util.InterlocException;

public class HTMLExporter extends DocumentExporter {

    private Writer fw;

    private File file;

    private boolean wasImage = false;

    public HTMLExporter(ModeleLiens modele) {
        super(modele);
    }

    void addSimpleText(String txt) {
        writeLine(txt + "<br/>");
    }

    void addTitleText(String txt) {
        writeLine("<h1>" + txt + "</h1>");
    }

    void closeDocument() {
        writeLine("</body></html>");
        try {

            fw.close();
        } catch (IOException e) {
            throw new InterlocException(e);
        }

    }

    private void writeLine(String s) {
        try {
            if (wasImage && !isImageTxt(s)) {
                fw.write("</table>\n");
            }
            wasImage = isImageTxt(s);

            fw.write(s + "\n");
        } catch (IOException e) {
            throw new InterlocException(e);
        }

    }

    /**
     * @param s
     */
    private boolean isImageTxt(String s) {
        return (s.indexOf("<img") > 0);
    }

    void initDocument(File outputFile) {
        try {
            file = outputFile;
            FileOutputStream fos = new FileOutputStream(outputFile);
            fw = new OutputStreamWriter(fos,"UTF-8");
            writeLine("<html><head><meta http-equiv=\"Content-Type\" Content=\"text/html; charset=UTF-8\"></head><body>");
        } catch (IOException e) {
            throw new InterlocException(e);
        }

    }

    void insertImage(BufferedImage subImage, String id) {
        try {
            if(!wasImage){
                writeLine("<table border=0 cellpadding=0 cellspacing=0>");
            }
            
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

}
