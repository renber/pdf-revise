package de.renebergelt.pdfrevise.tasks;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import de.renebergelt.pdfrevise.types.PageFilter;
import de.renebergelt.pdfrevise.types.TaskFailedException;
import de.renebergelt.pdfrevise.types.TaskOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * Extracts specific pages
 */
public class ExtractPages implements PdfTask<ExtractPages.ExtractPagesOptions> {

    @Override
    public String getDescription() {
        return "Extracting pages";
    }

    @Parameters(separators = "=", commandDescription = "Extract only the filtered pages from the pdf and copy them to the target")
    public static class ExtractPagesOptions implements TaskOptions {

        @Override
        public String getCommandName() {
            return "extract";
        }
    }

    @Override
    public ExtractPagesOptions getDefaultOptions() {
        return new ExtractPagesOptions();
    }

    @Override
    public void process(ExtractPagesOptions options, InputStream inStream, OutputStream outStream, PageFilter filter, Consumer<Float> progressCallback) throws TaskFailedException {
        try {
            PdfReader reader = new PdfReader(inStream);
            PdfStamper stamper = new PdfStamper(reader, outStream);
            reader.selectPages(filter.getPages(reader.getNumberOfPages()));
            stamper.close();
            progressCallback.accept(1.0f);
        } catch (IOException | DocumentException e) {
            throw new TaskFailedException("Page selection failed: " + e.getMessage(), e);
        }
    }
}
