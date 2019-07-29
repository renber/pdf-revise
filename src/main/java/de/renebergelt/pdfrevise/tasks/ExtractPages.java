package de.renebergelt.pdfrevise.tasks;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * Extracts specific pages
 */
public class ExtractPages implements PdfTask {

    @Override
    public String getDescription() {
        return "Extracting pages";
    }

    public ExtractPages() {

    }

    @Override
    public void process(InputStream inStream, OutputStream outStream, PageFilter filter, Consumer<Float> progressCallback) throws TaskFailedException {
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
