package de.renebergelt.pdfrevise.types;

/**
 * Thrown when the execution of a PdfTask fails
 */
public class TaskFailedException extends Exception {

    public TaskFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
