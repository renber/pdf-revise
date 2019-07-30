package de.renebergelt.pdfrevise.tasks;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import de.renebergelt.pdfrevise.types.PageFilter;
import de.renebergelt.pdfrevise.types.TaskFailedException;
import de.renebergelt.pdfrevise.types.TaskOptions;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

public class PageWatermarkStamper implements PdfTask<PageWatermarkStamper.PageWatermarkOptions> {

    @Override
    public String getDescription() {
        return "Adding watermark to pages";
    }

    @Parameters(separators = "=", commandDescription = "Add a watermark to every page")
    public static class PageWatermarkOptions implements TaskOptions {

        @Override
        public String getCommandName() {
            return "add-page-watermark";
        }

        @Parameter(required = true, description = "The watermark text")
        public String watermarkText = "DRAFT";

        @Parameter(names = {"--layer"}, description = "Where to put the watermark text for pages (background or foreground)")
        public StampLayer watermarkLayer = StampLayer.BACKGROUND;
    }

    @Override
    public PageWatermarkOptions getDefaultOptions() {
        return new PageWatermarkOptions();
    }

    @Override
    public void process(PageWatermarkOptions options, InputStream inStream, OutputStream outStream, PageFilter filter, Consumer<Float> progressCallback) throws TaskFailedException {
        try {
            PdfReader reader = new PdfReader(inStream);
            int pageCount = reader.getNumberOfPages();
            PdfStamper stamper = new PdfStamper(reader, outStream);
            stamper.setRotateContents(false);

            // transparency
            PdfGState gs1 = new PdfGState();
            gs1.setFillOpacity(0.5f);

            // properties
            PdfContentByte contentLayer;
            Rectangle pagesize;
            // loop over every page
            for (int p = 1; p <= pageCount; p++) {
                pagesize = reader.getPageSize(p);

                if (filter.isPageInFilter(p, pageCount)) {
                    // determine the font size based on the available space and the length of the text
                    float fontsize = (pagesize.getWidth() + pagesize.getHeight()) / (options.watermarkText.length() + 2);
                    Font f = new Font(Font.FontFamily.HELVETICA, fontsize);
                    f.setColor(BaseColor.LIGHT_GRAY);
                    Phrase phrase = new Phrase(options.watermarkText, f);

                    float x = pagesize.getWidth() / 2 + 50;
                    float y = pagesize.getHeight() / 2 - 50;

                    // determine rotation based on the bounds (lay it on the diagonal from bottom left to top right)
                    // for this we need the length of the diagonal which is the hypotenuse of the two vertices making up the bounds rect
                    double hyp = Math.sqrt(pagesize.getWidth() * pagesize.getWidth() + pagesize.getHeight() * pagesize.getHeight());
                    float rot = (float) Math.toDegrees(Math.asin(pagesize.getHeight() / hyp));

                    contentLayer = options.watermarkLayer.isBackground() ? stamper.getUnderContent(p) : stamper.getOverContent(p);
                    contentLayer.saveState();
                    contentLayer.setGState(gs1);
                    ColumnText.showTextAligned(contentLayer, Element.ALIGN_CENTER, phrase, x, y, rot);
                    contentLayer.restoreState();
                }
                progressCallback.accept( (p+1)/ (float)pageCount );
            }
            stamper.close();
            reader.close();
        } catch (Exception e) {
            throw new TaskFailedException("Task AddWatermarkText failed: " + e.getMessage(), e);
        }

    }

    public enum StampLayer {
        FOREGROUND(false),
        FG(false),
        BACKGROUND(true),
        BG(true);

        private boolean background;

        StampLayer(boolean isBackground) {
            background = isBackground;
        }

        public boolean isBackground() {
            return background;
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
