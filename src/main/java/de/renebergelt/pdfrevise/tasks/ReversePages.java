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
import java.util.*;
import java.util.function.Consumer;

public class ReversePages implements PdfTask<ReversePages.ReversePagesOptions>  {

    @Override
    public String getDescription() {
        return "Reversing page order";
    }

    @Parameters(separators = "=", commandDescription = "Reverses the order of pages")
    public static class ReversePagesOptions implements TaskOptions {
        @Override
        public String getTaskVerb() {
            return "reverse-pages";
        }
    }

    @Override
    public ReversePagesOptions getDefaultOptions() {
        return new ReversePagesOptions();
    }

    @Override
    public void process(ReversePagesOptions options, InputStream inStream, OutputStream outStream, PageFilter filter, Consumer<Float> progressCallback) throws TaskFailedException {
        try {
            PdfReader reader = new PdfReader(inStream);

            // create a look-up table which page has to be replaced with which
            HashMap<Integer, Integer> pageReplace = new HashMap<>();
            List<Integer> selectedPages = filter.getPages(reader.getNumberOfPages());
            List<Integer> selectedPagesReversed = new ArrayList<>(selectedPages);
            Collections.reverse(selectedPagesReversed);
            for(int i = 0; i < selectedPages.size(); i++) {
                pageReplace.put(selectedPages.get(i), selectedPagesReversed.get(i));
            }

            // create the list of pages to output
            List<Integer> resultPages = new ArrayList<>();
            for(int p = 1; p <= reader.getNumberOfPages(); p++) {
                Integer newPage = pageReplace.get(Integer.valueOf(p));
                if (newPage == null)
                    resultPages.add(p);
                else
                    resultPages.add(newPage);
            }

            PdfStamper stamper = new PdfStamper(reader, outStream);
            reader.selectPages(resultPages);
            stamper.close();
            progressCallback.accept(1.0f);
        } catch (IOException | DocumentException e) {
            throw new TaskFailedException("Page selection failed: " + e.getMessage(), e);
        }
    }



}
