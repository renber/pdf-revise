package de.renebergelt.pdfrevise.tasks;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.*;
import com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpLocation;
import com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpProcessor;
import de.renebergelt.pdfrevise.textextraction.LocationTextExtractionStrategyEx;
import de.renebergelt.pdfrevise.types.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SupersedeText implements PdfTask<SupersedeText.SupersedeTextOptions> {

    @Override
    public String getDescription() {
        return "Superseding text";
    }

    @Parameters(separators = "=", commandDescription = "Replaces text in a pdf by removing the old occurrence and adding new text instead (experimental!)")
    public static class SupersedeTextOptions extends FontOptions {
        @Override
        public String getTaskVerb() {
            return "supersede-text";
        }

        @Parameter(required = true, description = "[text to be replaced]")
        public String what = "";

        @Parameter(names = {"--with"}, description = "text to insert")
        public String with = "";

        @Parameter(names = {"--reuse-font"}, description = "reuse the font used in the pdf for the matched word. depending on the pdf output might not be as exptected (e.g. missing letters)")
        public boolean reuseFont = false;
    }

    @Override
    public SupersedeTextOptions getDefaultOptions() {
        return new SupersedeTextOptions();
    }

    @Override
    public void process(SupersedeTextOptions options, InputStream inStream, OutputStream outStream, PageFilter filter, Consumer<Float> progressCallback) throws TaskFailedException {
        try {
            PdfReader reader = new PdfReader(inStream);
            PdfStamper stamper = new PdfStamper(reader, outStream);

            Font replacementFont = PdfFontFactory.convertFont(options.createFont(), options.fontColor);

            int pageCount = reader.getNumberOfPages();

            for (int p = 1; p <= pageCount; p++) {

                if (filter.isPageInFilter(p, reader.getNumberOfPages())) {
                    List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();

                    LocationTextExtractionStrategyEx strategy = new LocationTextExtractionStrategyEx(options.what);
                    PdfTextExtractor.getTextFromPage(reader, p, strategy);

                    for (LocationTextExtractionStrategyEx.SearchResult result : strategy.getSearchResults()) {
                        System.out.println("Found at: " + result.getLeft() + ", " + result.getBottom() + " " + result.getWidth() + "x" + result.getHeight());

                        cleanUpLocations.add(new PdfCleanUpLocation(p, result.getBaselineBounds(), BaseColor.WHITE));
                    }

                    // remove original occurences of the word
                    if (cleanUpLocations.size() > 0) {
                        PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(cleanUpLocations, stamper);
                        cleaner.cleanUp();
                    }

                    // insert new text at the cleaned-up positions
                    PdfContentByte layer = stamper.getOverContent(p);

                    for(LocationTextExtractionStrategyEx.SearchResult result : strategy.getSearchResults()) {
                        // todo: extract original font name (if possible; see https://stackoverflow.com/questions/51798084/how-do-i-extract-actual-font-names-from-a-pdf-with-itextsharp)
                        Font rf = null;

                        if (options.reuseFont) {
                            String origFontName = findFontPdfName(reader, result.getFont());
                            PdfIndirectReference fontRef = findFont(reader.getPageN(p).getAsDict(PdfName.RESOURCES), origFontName);
                            if (fontRef != null) {
                                BaseFont bf = BaseFont.createFont((PRIndirectReference)fontRef);

                                int style = Font.NORMAL;
                                if (options.isBold) style |= Font.BOLD;
                                if (options.isItalic) style |= Font.ITALIC;
                                rf = new Font(bf, result.getFontSize(), style);
                            }
                        }

                        if (rf == null)
                            rf = replacementFont;

                        Phrase phrase = new Phrase(0, options.with, rf);
                        ColumnText.showTextAligned(layer, Element.ALIGN_LEFT | Element.ALIGN_BASELINE, phrase, result.getLeft(), result.getBaselineY(), 0);
                    }
                }

                progressCallback.accept(p / (float) pageCount);
            }

            stamper.close();
            reader.close();
        } catch (Exception e){
            throw new TaskFailedException("Superseding text failed: " + e, e);
        }
    }

    public String findFontPdfName(PdfReader reader, DocumentFont font) {

        PdfDictionary dict = font.getFontDictionary();
        PdfName name = dict.getAsName(PdfName.BASEFONT);
        return name == null ? null : name.toString();
    }

    /**
     * Get a reference to the font name with the given name if it is contained in resource
     */
    public static PdfIndirectReference findFont(PdfDictionary resource, String fontName) {
        if (resource == null)
            return null;
        PdfDictionary xobjects = resource.getAsDict(PdfName.XOBJECT);
        if (xobjects != null) {
            for (PdfName key : xobjects.getKeys()) {
                PdfIndirectReference fontRef = findFont(xobjects.getAsDict(key), fontName);
                if (fontRef != null)
                    return fontRef;
            }
        }
        PdfDictionary fonts = resource.getAsDict(PdfName.FONT);
        if (fonts == null)
            return null;
        PdfDictionary font;
        for (PdfName key : fonts.getKeys()) {
            font = fonts.getAsDict(key);
            String name = font.getAsName(PdfName.BASEFONT).toString();
            if (name.equals(fontName)) {
                return fonts.getAsIndirectObject(key);
            }
        }

        return null;
    }
}
