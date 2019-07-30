package de.renebergelt.pdfrevise.types;

import java.util.List;

public interface PageFilter {

    /**
     * Checks if the given page number is part of the filter
     * @param pageNumber Number of the page (first page has number 1)
     */
    boolean isPageInFilter(int pageNumber, int pageCount);

    List<Integer> getPages(int pageCount);

}
