package fr.megiste.interloc.util;

public class InterlocException extends RuntimeException {
	public InterlocException(String message){
		super(message);
	}

    public InterlocException(Exception e) {
        super(e);
    }
}
