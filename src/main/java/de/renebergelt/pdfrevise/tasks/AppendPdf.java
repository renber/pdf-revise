package de.renebergelt.pdfrevise.tasks;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import de.renebergelt.pdfrevise.types.PageFilter;
import de.renebergelt.pdfrevise.types.TaskFailedException;
import de.renebergelt.pdfrevise.types.TaskOptions;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

public class AppendPdf implements PdfTask<AppendPdf.AppendOptions> {

    @Parameters(separators = "=", commandDescription = "Appends the specified pdf file")
    public static class AppendOptions implements TaskOptions {
        @Override
        public String getTaskVerb() {
            return "append";
        }

        @Parameter(required=true, description = "The pdf file to append")
        public String fileToAppend;
    }

    @Override
    public AppendOptions getDefaultOptions() {
        return new AppendOptions();
    }

    @Override
    public String getDescription() {
        return "Appending pdf file";
    }

    @Override
    public void process(AppendOptions options, InputStream inStream, OutputStream outStream, PageFilter filter, Consumer<Float> progressCallback) throws TaskFailedException {
        try {
            PDFMergerUtility merger = new PDFMergerUtility();
            merger.addSource(inStream);
            merger.addSource(new java.io.File(options.fileToAppend));

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
