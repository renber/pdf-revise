package de.renebergelt.pdfrevise.tasks;

import de.renebergelt.pdfrevise.types.PageFilter;
import de.renebergelt.pdfrevise.types.TaskFailedException;
import de.renebergelt.pdfrevise.types.TaskOptions;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

public interface PdfTask<TOptions extends TaskOptions> {

    String getDescription();

    /**
     * Options to appear after this task's command line verb
     */
    TOptions getDefaultOptions();

    void process(TOptions options, InputStream inStream, OutputStream outStream, PageFilter filter, Consumer<Float> progressCallback) throws TaskFailedException;

}
