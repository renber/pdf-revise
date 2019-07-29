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
public class SelectPages implements PdfTask {

    String pageRange;

    @Override
    public String getDescription() {
        return "Selecting pages";
    }

    public SelectPages(String pageRange) {
        this.pageRange = pageRange;
    }

    @Override
    public void process(InputStream inStream, OutputStream outStream, Consumer<Float> progressCallback) throws TaskFailedException {
        try {
            PdfReader reader = new PdfReader(inStream);
            PdfStamper stamper = new PdfStamper(reader, outStream);
            reader.selectPages(pageRange);
            stamper.close();
            progressCallback.accept(1.0f);
        } catch (IOException | DocumentException e) {
            throw new TaskFailedException("Page selection failed: " + e.getMessage(), e);
        }
    }
}
