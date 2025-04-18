package com.hamit.obs.exception;

public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceException(String message) {
		super(message);
	}

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException() {
        super();
    }
}