package de.renebergelt.pdfrevise.tasks;

/**
 * Thrown when the execution of a PdfTask fails
 */
public class TaskFailedException extends Exception {

    public TaskFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
