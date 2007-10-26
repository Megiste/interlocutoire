package fr.megiste.interloc.rtf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import fr.megiste.interloc.data.ModeleLiens;
import fr.megiste.interloc.util.InterlocException;

public class VelocityExporter extends DocumentExporter {

	private VelocityContext context;


	private File file;
	

	

	public VelocityExporter(ModeleLiens modele) {
		super(modele);
	}


	void initDocument(File outputFile) {
		file = outputFile;
		context = new VelocityContext();
		context.put("file", file);
		context.put("exporter", this);
		
		
	}


	public void writeImagePacks() {
		context.put("packs", (ImagePack[]) packs.toArray(new ImagePack[packs.size()]));
		
		File output=file;
		
		String templateFile = "template.vm";
		
		handleTemplate(output, templateFile);
		
	}


	/**
	 * @param output
	 * @param templateFile
	 */
	public void handleTemplate(File output, String templateFile) {
		Template template = null;
		try {
			template = Velocity.getTemplate(templateFile);
		} catch (ResourceNotFoundException e) {
			System.out.println("Example : error : cannot find template " + templateFile );
		} catch (ParseErrorException e) {
			System.out.println("Example : Syntax error in template " + templateFile + ":" + e );
		} catch (Exception e) {
			throw new InterlocException(e);
		}
		try {
			FileWriter fw = new FileWriter(output);
			if(template!=null){
				template.merge(context, fw);
			}
			fw.close();
		} catch (ResourceNotFoundException e) {
			throw new InterlocException(e);
		} catch (ParseErrorException e) {
			throw new InterlocException(e);
		} catch (MethodInvocationException e) {
			throw new InterlocException(e);
		} catch (IOException e) {
			throw new InterlocException(e);
		}
	}

	public String writeImage(String repName,BufferedImage image,int count){
		
		
        String imgPath;
		try {
			
			File rep = new File(file.getParentFile(),repName);
			if(!rep.exists()){
				rep.mkdirs();
			}
			File fichierImage = new File(rep, "imageTmp_" + count + ".jpg");
			if(fichierImage.exists()){
				fichierImage.delete();
			}
			FileOutputStream fos = new FileOutputStream(fichierImage,false);
			ImageIO.write(image, "jpg", fos);
			imgPath = repName + "/" + fichierImage.getName();
		} catch (IOException e) {
			throw new InterlocException(e);
		}
        
        return imgPath;
	}

}
