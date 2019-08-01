package de.renebergelt.pdfrevise.tasks;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpLocation;
import com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpProcessor;
import de.renebergelt.pdfrevise.textextraction.LocationTextExtractionStrategyEx;
import de.renebergelt.pdfrevise.types.*;

import java.awt.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BlackenText implements PdfTask<BlackenText.BlackenOptions> {

    @Override
    public String getDescription() {
        return "Blacken text occurences";
    }

    @Parameters(separators = "=", commandDescription = "Blackens the specified term in the pdf (original term is completely removed)")
    public static class BlackenOptions implements TaskOptions {
        @Override
        public String getTaskVerb() {
            return "blacken";
        }

        @Parameter(required = true, description = "[text to be blackened]")
        public String what = "";

        @Parameter(names = {"-color"}, description = "The color to use for blackening (name or in hex format (e.g. #FFFFFF))", converter = FontOptions.ColorConverter.class)
        public Color color = Color.BLACK;
    }

    @Override
    public BlackenText.BlackenOptions getDefaultOptions() {
        return new BlackenOptions();
    }

    @Override
    public void process(BlackenOptions options, InputStream inStream, OutputStream outStream, PageFilter filter, Consumer<Float> progressCallback) throws TaskFailedException {
        try {
            PdfReader reader = new PdfReader(inStream);
            PdfStamper stamper = new PdfStamper(reader, outStream);

            BaseColor color = new BaseColor(options.color.getRed(), options.color.getGreen(), options.color.getBlue());

            int pageCount = reader.getNumberOfPages();

            for (int p = 1; p <= pageCount; p++) {
                if (filter.isPageInFilter(p, reader.getNumberOfPages())) {
                    List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();

                    LocationTextExtractionStrategyEx strategy = new LocationTextExtractionStrategyEx(options.what);
                    PdfTextExtractor.getTextFromPage(reader, p, strategy);

                    for (LocationTextExtractionStrategyEx.SearchResult result : strategy.getSearchResults()) {
                        cleanUpLocations.add(new PdfCleanUpLocation(p, result.getBounds(), color));
                    }

                    // remove original occurences of the word
                    if (cleanUpLocations.size() > 0) {
                        PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(cleanUpLocations, stamper);
                        cleaner.cleanUp();
                    }
                }

                progressCallback.accept(p / (float) pageCount);
            }

            stamper.close();
            reader.close();
        } catch (Exception e) {
            throw new TaskFailedException("Superseding text failed: " + e, e);
        }
    }
}
