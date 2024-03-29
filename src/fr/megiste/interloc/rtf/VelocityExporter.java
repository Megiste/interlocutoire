package fr.megiste.interloc.rtf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import fr.megiste.interloc.data.ModeleLiens;
import fr.megiste.interloc.util.InterlocException;
import fr.megiste.interloc.util.Utilities;

public class VelocityExporter extends DocumentExporter {

	private static final int MAX_TAILLE_PAGE = 200;

	private VelocityContext context;

	private File file;

	private File outputDir;

	private static String TEMPLATE_BASE_FILE = "template" + File.separator + "template.vm";;

	public VelocityExporter(ModeleLiens modele) {
		super(modele);
		initTemplateFile();
	}

	private void initTemplateFile() {
		File f = new File("." + File.separator + TEMPLATE_BASE_FILE);
		if (f.exists() && f.length() == 0) {
			f.delete();
		}
		if (!f.exists()) {
			if (!f.getParentFile().exists()) {
				f.getParentFile().mkdirs();
			}

			try {
				ClassLoader cl = this.getClass().getClassLoader();
				InputStream in = cl.getResourceAsStream(f.getName());
				FileOutputStream out = new FileOutputStream(f);
				// Transfer bytes from in to out
				byte[] buf = new byte[1];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();

			} catch (Exception e) {
				throw new InterlocException(e);
			}

		}

	}

	void initDocument(File outputFile) {
		file = outputFile;
		context = new VelocityContext();
		
		context.put("exporter", this);
	}

	/**
	 * @param outputFile
	 * @param page
	 * @return
	 */
	private File generatePartialOutputFile(File outputFile, int page) {
		int idx = outputFile.getName().lastIndexOf(".");
		String fileNameWithoutSuffix = outputFile.getName();
		String suffix = "";
		if (idx > -1) {
			suffix = fileNameWithoutSuffix.substring(idx);
			fileNameWithoutSuffix = fileNameWithoutSuffix.substring(0, idx);
		}
		String outFileName = outputFile.getName();
		if (page > 0) {
			outFileName = fileNameWithoutSuffix + "_" + page + suffix;
		}

		File partialOuputFile = new File(outputFile.getParentFile(), outFileName);
		return partialOuputFile;
	}

	public void writeImagePacks() {
		List page = new ArrayList();
		int taillePage = 0;
		int numPage = 0;
		outputDir = file.getParentFile();
		File template = new File(TEMPLATE_BASE_FILE);
		String templateName = template.getName();

		for (int i = 0; i < packs.size(); i++) {
			if (taillePage >= MAX_TAILLE_PAGE) {
				handlePage(numPage, templateName, false, page);
				numPage++;
				taillePage = 0;
				page.clear();

			}
			ImagePack pack = (ImagePack) packs.get(i);
			page.add(pack);
			taillePage = taillePage + pack.getSize();

		}
		handlePage(numPage, templateName, true, page);

	}

	/**
	 * @param numPage
	 * @param templateName
	 * @param last
	 * @param packs
	 */
	private void handlePage(int numPage, String templateName, boolean last, List packs) {
		context.put("packs", packs);
		
		File pageFile = generatePartialOutputFile(file, numPage);
		context.put("file", pageFile);
		

		if (numPage > 0) {
			context.put("pagePrecedente", generatePartialOutputFile(file, numPage - 1).getName());
		} else {
			context.remove("pagePrecedente");
		}
		if (!last) {
			context.put("pageSuivante", generatePartialOutputFile(file, numPage + 1).getName());
		} else {
			context.remove("pageSuivante");
		}
		handleTemplate(pageFile.getName(), templateName);
	}

	/**
	 * @param fileName
	 * @param TEMPLATE_BASE_FILE
	 */
	public boolean handleTemplate(String fileName, String templateName) {

		String templateFile = new File(TEMPLATE_BASE_FILE).getParentFile().getName() + File.separator
				+ templateName;
		Template template = null;
		File outputFile = new File(outputDir, fileName);
		if (!outputFile.getParentFile().exists()) {
			outputFile.getParentFile().mkdirs();
		}
		System.out.println("Processing :" + templateName + " to " + outputFile);
		try {
			template = Velocity.getTemplate(templateFile);
		} catch (ResourceNotFoundException e) {
			System.out.println("Example : error : cannot find template " + templateFile);
		} catch (ParseErrorException e) {
			System.out.println("Example : Syntax error in template " + templateFile + ":" + e);
		} catch (Exception e) {
			throw new InterlocException(e);
		}
		try {
			FileWriter fw = new FileWriter(outputFile);
			if (template != null) {
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
		System.out.println("Processing :" + templateName + "  OK");
		return true;
	}

	public String writeImage(String repName, File image, int count) {
		String imageName = "imageTmp_" + count + ".jpg";
		return writeImage(repName, image, imageName);
	}

	public String writeImage(String repName, File imageSource, String imageName) {

		String imgPath;
		try {

			File rep = new File(file.getParentFile(), repName);
			if (!rep.exists()) {
				rep.mkdirs();
			}
			File fichierImage = new File(rep, imageName);
			if (fichierImage.exists()) {
				fichierImage.delete();
			}
			FileOutputStream fos = new FileOutputStream(fichierImage, false);
			FileInputStream fis = new FileInputStream(imageSource);
			Utilities.copy(fis, fos);
			imgPath = repName + "/" + fichierImage.getName();
			imageSource.delete();
		} catch (IOException e) {
			throw new InterlocException(e);
		}

		return imgPath;
	}

}
