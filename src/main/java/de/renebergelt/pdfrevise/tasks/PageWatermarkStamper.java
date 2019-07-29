package de.renebergelt.pdfrevise.tasks;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import de.renebergelt.pdfrevise.StampLayer;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

public class PageWatermarkStamper implements PdfTask {

    String watermarkText;
    StampLayer targetLayer;

    @Override
    public String getDescription() {
        return "Adding watermark to pages";
    }

    public PageWatermarkStamper(String watermarkText, StampLayer targetLayer) {
        if (watermarkText == null)
            throw new IllegalArgumentException("Parameter watermarkText must not be null");
        if (targetLayer == null)
            throw new IllegalArgumentException("Parameter targetLayer must not be null");

        this.watermarkText = watermarkText;
        this.targetLayer = targetLayer;
    }

    @Override
    public void process(InputStream inStream, OutputStream outStream, PageFilter filter, Consumer<Float> progressCallback) throws TaskFailedException {
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

                if (filter.isPageInFilter(p)) {
                    // determine the font size based on the available space and the length of the text
                    float fontsize = (pagesize.getWidth() + pagesize.getHeight()) / (watermarkText.length() + 2);
                    Font f = new Font(Font.FontFamily.HELVETICA, fontsize);
                    f.setColor(BaseColor.LIGHT_GRAY);
                    Phrase phrase = new Phrase(watermarkText, f);

                    float x = pagesize.getWidth() / 2 + 50;
                    float y = pagesize.getHeight() / 2 - 50;

                    // determine rotation based on the bounds (lay it on the diagonal from bottom left to top right)
                    // for this we need the length of the diagonal which is the hypotenuse of the two vertices making up the bounds rect
                    double hyp = Math.sqrt(pagesize.getWidth() * pagesize.getWidth() + pagesize.getHeight() * pagesize.getHeight());
                    float rot = (float) Math.toDegrees(Math.asin(pagesize.getHeight() / hyp));

                    contentLayer = targetLayer.isBackground() ? stamper.getUnderContent(p) : stamper.getOverContent(p);
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
}
