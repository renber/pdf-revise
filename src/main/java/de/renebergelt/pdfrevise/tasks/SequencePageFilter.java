package de.renebergelt.pdfrevise.tasks;

import com.itextpdf.text.pdf.SequenceList;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * PageFilter which uses itext's SequenceList to parse page ranges
 */
public class SequencePageFilter implements PageFilter {

    List<Integer> pages;
    String filterTerm;

    public SequencePageFilter(String filterTerm) {
        this.filterTerm = filterTerm;
        pages = SequenceList.expand(filterTerm, Integer.MAX_VALUE);
    }

    @Override
    public boolean isPageInFilter(int pageNumber) {
        return pages.contains(Integer.valueOf(pageNumber));
    }

    @Override
    public List<Integer> getPages(int pageCount) {
        return SequenceList.expand(filterTerm, pageCount);
    }

}
