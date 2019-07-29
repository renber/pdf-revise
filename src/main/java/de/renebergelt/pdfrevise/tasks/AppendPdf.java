package de.renebergelt.pdfrevise.tasks;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

public class AppendPdf implements PdfTask {

    String fileToAppend;

    @Override
    public String getDescription() {
        return "Appending pdf file";
    }

    public AppendPdf(String fileToAppend) {
        this.fileToAppend = fileToAppend;
    }

    @Override
    public void process(InputStream inStream, OutputStream outStream, PageFilter filter, Consumer<Float> progressCallback) throws TaskFailedException {
        try {
            PDFMergerUtility merger = new PDFMergerUtility();
            merger.addSource(inStream);
            merger.addSource(new java.io.File(fileToAppend));

            merger.setDestinationStream(outStream);
            merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

            progressCallback.accept(1.0f);
        } catch (FileNotFoundException e) {
            throw new TaskFailedException("The file to be appended does not exist: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new TaskFailedException("Could not append file: " + e.getMessage(), e);
        }
    }
}
