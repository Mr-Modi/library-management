package com.library.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public static DuplicateResourceException isbn(String isbn) {
        return new DuplicateResourceException("A book with ISBN '" + isbn + "' already exists");
    }
}
