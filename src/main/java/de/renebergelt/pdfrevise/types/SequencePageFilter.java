package de.renebergelt.pdfrevise.types;

import com.itextpdf.text.pdf.SequenceList;

import java.util.ArrayList;
import java.util.List;

/**
 * PageFilter which uses itext's SequenceList to parse page ranges
 */
public class SequencePageFilter implements PageFilter {

    String filterTerm;

    List<Integer> cachedPages;
    int cachedForPageCount;


    public SequencePageFilter(String filterTerm) {
        this.filterTerm = filterTerm;
    }

    @Override
    public boolean isPageInFilter(int pageNumber, int pageCount) {
        return getCachedPages(pageCount).contains(Integer.valueOf(pageNumber));
    }

    @Override
    public List<Integer> getPages(int pageCount) {
        return new ArrayList<>(getCachedPages(pageCount));
    }

    private List<Integer> getCachedPages(int pageCount) {
        if (cachedPages == null || cachedForPageCount != pageCount) {
            cachedPages = SequenceList.expand(filterTerm, pageCount);
            cachedForPageCount = pageCount;
        }

        return cachedPages;
    }

}
