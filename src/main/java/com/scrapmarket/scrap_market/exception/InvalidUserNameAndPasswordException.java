package com.scrapmarket.scrap_market.exception;

public class InvalidUserNameAndPasswordException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidUserNameAndPasswordException(String message) {
        super(message);
    }

    public InvalidUserNameAndPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}
