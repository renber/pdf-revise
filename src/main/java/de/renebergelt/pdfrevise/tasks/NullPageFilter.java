package de.renebergelt.pdfrevise.tasks;

import com.itextpdf.text.pdf.PdfReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Default page filter which does not filter
 */
public class NullPageFilter implements PageFilter {

    @Override
    public boolean isPageInFilter(int pageNumber) {
        return true;
    }

    @Override
    public List<Integer> getPages(int pageCount) {
        return IntStream.rangeClosed(1, pageCount).boxed().collect(Collectors.toList());
    }
}
