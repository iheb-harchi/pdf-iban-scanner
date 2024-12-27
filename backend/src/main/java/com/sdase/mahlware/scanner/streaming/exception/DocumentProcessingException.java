package com.sdase.mahlware.scanner.streaming.exception;

/**
 * Eine benutzerdefinierte Ausnahme, die verwendet wird, um Fehler während der Dokumentenverarbeitung zu kennzeichnen.
 */
public class DocumentProcessingException extends RuntimeException {

    public DocumentProcessingException(String message) {
        super(message);
    }

    public DocumentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
