package de.renebergelt.pdfrevise.tasks;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

public interface PdfTask {

    String getDescription();

    void process(InputStream inStream, OutputStream outStream, PageFilter filter, Consumer<Float> progressCallback) throws TaskFailedException;

}
