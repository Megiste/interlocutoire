package fr.megiste.interloc.rtf;

import java.util.ArrayList;

public class ImagePack {
	private String title;
	private ArrayList images = new ArrayList();
	private String commentary;
	/**
	 * Gets the commentary.
	 * @return the commentary
	 */
	public String getCommentary() {
		return commentary;
	}
	/**
	 * Gets the images.
	 * @return the images
	 */
	public ArrayList getImages() {
		return images;
	}
	/**
	 * Gets the title.
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * Sets the commentary. 
	 * @param commentary the commentary to set
	 */
	public void setCommentary(String commentary) {
		this.commentary = commentary;
	}
	/**
	 * Sets the images. 
	 * @param images the images to set
	 */
	public void setImages(ArrayList images) {
		this.images = images;
	}
	/**
	 * Sets the title. 
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
}
