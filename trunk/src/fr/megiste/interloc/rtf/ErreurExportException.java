package fr.megiste.interloc.rtf;


public class ErreurExportException extends Exception {

	public ErreurExportException(String message) {
		super(message);
	}

    public ErreurExportException(Exception e) {
        super(e);
    }

}
