package de.renebergelt.pdfrevise.tasks;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import de.renebergelt.pdfrevise.types.FontOptions;
import de.renebergelt.pdfrevise.types.PageFilter;
import de.renebergelt.pdfrevise.types.PdfFontFactory;
import de.renebergelt.pdfrevise.types.TaskFailedException;
import org.bouncycastle.jcajce.provider.symmetric.ARC4;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

public class AddPageNumbers implements PdfTask<AddPageNumbers.PageNumberOptions> {

    @Parameters(separators = "=", commandDescription = "Adds page numbers")
    public static class PageNumberOptions extends FontOptions {
        @Override
        public String getTaskVerb() {
            return "add-page-numbers";
        }

        @Parameter(names = {"--style"}, description = "The style of the page numbers")
        public PageNumberStyle pageNumberStyle = PageNumberStyle.ARABIC;

        @Parameter(names = {"--start-at"}, description = "Numbering starts with this number")
        public int startAt = 1;

        @Parameter(names = {"-v", "--vertical"}, description = "Vertical alignment on the page")
        public TextAlignment verticalAlignment = TextAlignment.FAR;

        @Parameter(names = {"--vertical-margin", "--v-margin"}, description = "Vertical margin")
        public float verticalMargin = 20;

        @Parameter(names = {"-h", "--horizontal"}, description = "Horizontal alignment on the page")
        public TextAlignment horizontalAlignment = TextAlignment.CENTER;

        @Parameter(names = {"--horizontal-margin", "--h-margin"}, description = "Horizontal margin")
        public float horizontalMargin = 20;
    }

    public String getDescription() {
        return "Adding page numbers";
    }

    /**
     * Options to appear after this task's command line verb
     */
    public PageNumberOptions getDefaultOptions() {
        return new PageNumberOptions();
    }

    public void process(PageNumberOptions options, InputStream inStream, OutputStream outStream, PageFilter filter, Consumer<Float> progressCallback) throws TaskFailedException {

        try {
            PdfReader reader = new PdfReader(inStream);
            int pageCount = reader.getNumberOfPages();
            PdfStamper stamper = new PdfStamper(reader, outStream);
            stamper.setRotateContents(false);

            // properties
            PdfContentByte contentLayer;
            Rectangle pagesize;
            // loop over every page
            int currentPageNumber = options.startAt;

            for (int p = 1; p <= pageCount; p++) {
                pagesize = reader.getPageSize(p);

                if (filter.isPageInFilter(p, pageCount)) {
                    String pageNumberText = getPageNumberText(options.pageNumberStyle, currentPageNumber);

                    java.awt.Font font = new java.awt.Font(options.fontName, java.awt.Font.PLAIN, options.fontSize);
                    Font pdfFont = PdfFontFactory.convertFont(font, options.fontColor);
                    Phrase phrase = new Phrase(pageNumberText, pdfFont);

                    float x = options.horizontalAlignment.getValue(pagesize.getWidth(), options.horizontalMargin, false);
                    float y = options.verticalAlignment.getValue(pagesize.getHeight(), options.verticalMargin, true);

                    contentLayer = stamper.getOverContent(p);
                    contentLayer.saveState();
                    ColumnText.showTextAligned(contentLayer, Element.ALIGN_CENTER, phrase, x, y, 0);
                    contentLayer.restoreState();
                }

                currentPageNumber++;
                progressCallback.accept( (p+1)/ (float)pageCount );
            }
            stamper.close();
            reader.close();
        } catch (Exception e) {
            throw new TaskFailedException("Task AddWatermarkText failed: " + e.getMessage(), e);
        }
    }

    public String getPageNumberText(PageNumberStyle style, int pageNumber) {
        switch (style) {
            case ARABIC: return String.valueOf(pageNumber);
            case ROMAN: return convertIntegerToRomanNumeral(pageNumber);
            default:
                throw new IllegalArgumentException("Unsupported page number style: " + style.toString());
        }
    }

    /**
     * Convert input to a roman numeral
     * Adapted from https://stackoverflow.com/questions/19813960/how-to-change-an-integer-value-to-a-roman-numeral
     */
    private static String convertIntegerToRomanNumeral(int input) {
        if (input < 1 || input > 3999)
            throw new IllegalArgumentException("Roman numbers only supported up to 3999");

        StringBuilder sb = new StringBuilder();

        while (input >= 1000) {
            sb.append("M");
            input -= 1000;
        }
        while (input >= 900) {
            sb.append("CM");
            input -= 900;
        }
        while (input >= 500) {
            sb.append("D");
            input -= 500;
        }
        while (input >= 400) {
            sb.append("CD");
            input -= 400;
        }
        while (input >= 100) {
            sb.append("C");
            input -= 100;
        }
        while (input >= 90) {
            sb.append("XC");
            input -= 90;
        }
        while (input >= 50) {
            sb.append("L");
            input -= 50;
        }
        while (input >= 40) {
            sb.append("XL");
            input -= 40;
        }
        while (input >= 10) {
            sb.append("X");
            input -= 10;
        }
        while (input >= 9) {
            sb.append("IX");
            input -= 9;
        }
        while (input >= 5) {
            sb.append("V");
            input -= 5;
        }
        while (input >= 4) {
            sb.append("IV");
            input -= 4;
        }
        while (input >= 1) {
            sb.append("I");
            input -= 1;
        }

        return sb.toString();
    }

    public enum PageNumberStyle {
        ARABIC,
        ROMAN;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public enum TextAlignment {
        NEAR,
        CENTER,
        FAR;

        public float getValue(float max, float margin, boolean isVertical) {

            // for pdfs th eorigin is at the bottom left of the page

            switch(this) {
                case NEAR: return isVertical ? max - margin : margin;
                case CENTER: return max / 2;
                case FAR: return isVertical ? margin : max - margin;
                default: throw new IllegalArgumentException("Unsupported text alignment: " + this.toString());
            }
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
