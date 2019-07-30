package de.renebergelt.pdfrevise.types;

import com.itextpdf.text.pdf.SequenceList;

import java.util.List;

/**
 * PageFilter which uses itext's SequenceList to parse page ranges
 */
public class SequencePageFilter implements PageFilter {

    String filterTerm;

    public SequencePageFilter(String filterTerm) {
        this.filterTerm = filterTerm;
    }

    @Override
    public boolean isPageInFilter(int pageNumber, int pageCount) {
        return SequenceList.expand(filterTerm, pageCount).contains(Integer.valueOf(pageNumber));
    }

    @Override
    public List<Integer> getPages(int pageCount) {
        return SequenceList.expand(filterTerm, pageCount);
    }

}
