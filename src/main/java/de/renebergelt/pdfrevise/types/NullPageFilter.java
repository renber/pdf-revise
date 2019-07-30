package de.renebergelt.pdfrevise.types;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Default page filter which does not filter
 */
public class NullPageFilter implements PageFilter {

    @Override
    public boolean isPageInFilter(int pageNumber, int pageCount) {
        return true;
    }

    @Override
    public List<Integer> getPages(int pageCount) {
        return IntStream.rangeClosed(1, pageCount).boxed().collect(Collectors.toList());
    }
}
