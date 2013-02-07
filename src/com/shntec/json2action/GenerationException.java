package com.shntec.json2action;

public class GenerationException extends RuntimeException {
	public GenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public GenerationException(String message) {
        super(message);
    }

    public GenerationException(Throwable cause) {
        super(cause);
    }

    public GenerationException(String message, ClassNotFoundException cause) {
        super(message, cause);
    }
}
