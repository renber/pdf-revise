package de.renebergelt.pdfrevise.tasks;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import de.renebergelt.pdfrevise.types.PageFilter;
import de.renebergelt.pdfrevise.types.TaskFailedException;
import de.renebergelt.pdfrevise.types.TaskOptions;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * A special task which updates the page filter
 */
public class UpdatePageFilter implements PdfTask<UpdatePageFilter.PageFilterOptions> {

    @Parameters(separators = "=", commandDescription = "Specifies the pages to which subsequent tasks should be applied.")
    public static class PageFilterOptions implements TaskOptions {
        @Override
        public String getTaskVerb() {
            return "pages";
        }

        @Parameter(required=true, description = "[page sequence (e.g. 1,2-5,!3,6-10)]", order = 1)
        public String pageSelector = "";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public PageFilterOptions getDefaultOptions() {
        return new PageFilterOptions();
    }

    @Override
    public void process(PageFilterOptions options, InputStream inStream, OutputStream outStream, PageFilter filter, Consumer progressCallback) throws TaskFailedException {
        throw new UnsupportedOperationException("Cannot execute UpdatePageFilter");
    }
}
