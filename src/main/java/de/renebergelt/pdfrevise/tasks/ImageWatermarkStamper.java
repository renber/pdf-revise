package de.renebergelt.pdfrevise.tasks;

import com.itextpdf.text.*;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Grayscales all images of a pdf
 */
public class ImageWatermarkStamper implements PdfTask {

    String watermarkText;

    BufferedImage overlayImg;

    @Override
    public String getDescription() {
        return "Adding watermark to images";
    }

    public ImageWatermarkStamper(String watermarkText) {
        if (watermarkText == null)
            throw new IllegalArgumentException("Parameter watermarkText must not be null");

        this.watermarkText = watermarkText;
    }

    @Override
    public void process(InputStream src, OutputStream dest, PageFilter filter, Consumer<Float> progressCallback) throws TaskFailedException {
        try {
            PdfReader reader = new PdfReader(src);
            PdfStamper stamper = new PdfStamper(reader, dest);

            int pageCount = reader.getNumberOfPages();

            PdfReaderContentParser parser = new PdfReaderContentParser(reader);

            for (int p = 1; p <= pageCount; p++) {

                if (filter.isPageInFilter(p)) {
                    PdfImageCollector imageCollector = new PdfImageCollector();
                    parser.processContent(p, imageCollector);

                    if (imageCollector.getImages().size() > 0) {
                        PdfContentByte over;
                        over = stamper.getOverContent(p);
                        for (Rectangle rect : imageCollector.getImages()) {
                            addWatermark(over, rect, watermarkText);
                        }
                    }
                }

                progressCallback.accept( (p+1)/ (float)pageCount );
            }

            stamper.close();
            reader.close();
        } catch (Exception e) {
            throw new TaskFailedException("Task ImageOverlay failed: " + e.getMessage(), e);
        }
    }

    private static void addWatermark(PdfContentByte targetContent, Rectangle bounds, String text) {
        // text watermark
        // determine the font size based on the available space and the length of the text
        float fontsize = (bounds.width + bounds.height) / (text.length() + 1);

        // determine rotation based on the bounds (lay it on the diagonal from bottom left to top right)
        // for this we need the length of the diagonal which is the hypotenuse of the two vertices making up the bounds rect
        double hyp = Math.sqrt(bounds.width*bounds.width + bounds.height*bounds.height);
        float rot = (float)Math.toDegrees(Math.asin(bounds.height / hyp));

        com.itextpdf.text.Font f = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, fontsize);
        f.setColor(BaseColor.LIGHT_GRAY);
        Phrase p = new Phrase(text, f);

        // transparency
        PdfGState gs1 = new PdfGState();
        gs1.setFillOpacity(0.5f);

        targetContent.saveState();
        targetContent.setGState(gs1);
        ColumnText.showTextAligned(targetContent, Element.ALIGN_CENTER, p, bounds.x + bounds.width / 2, bounds.y + bounds.height / 2, rot);
        targetContent.restoreState();
    }

    private Image addOverlay(PdfImageObject image) throws IOException, DocumentException {
        BufferedImage oldImg = image.getBufferedImage();
        BufferedImage newImg = new BufferedImage(oldImg.getWidth(), oldImg.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D g = (Graphics2D)newImg.getGraphics();
        g.setBackground(Color.WHITE);
        g.fillRect(0, 0, oldImg.getWidth(), oldImg.getHeight());
        g.drawImage(oldImg, 0, 0, null);
        g.drawImage(overlayImg, 0, 0, oldImg.getWidth(), oldImg.getHeight(), null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(newImg, "png", baos);
        Image resultImg = Image.getInstance(baos.toByteArray());
        resultImg.setImageMask(createImageMask(newImg));
        return resultImg;
    }

    private Image createImageMask(BufferedImage img) throws IOException, DocumentException {
        BufferedImage newBi = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_USHORT_GRAY);
        newBi.getGraphics().drawImage(img, 0, 0, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(newBi, "png", baos);
        Image mask = Image.getInstance(baos.toByteArray());
        mask.makeMask();
        return mask;
    }

    private static void replaceStream(PRStream orig, PdfStream stream) throws IOException {
        orig.setLength(0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        stream.writeContent(baos);
        orig.setData(baos.toByteArray(), false);
        for (PdfName name : stream.getKeys()) {
            orig.put(name, stream.get(name));
        }
    }

    static class PdfImageCollector implements ExtRenderListener {

        private java.util.List<Rectangle> images = new ArrayList<>();

        public java.util.List<Rectangle> getImages() {
            return images;
        }

        @Override
        public void modifyPath(PathConstructionRenderInfo pathConstructionRenderInfo) {

        }

        @Override
        public Path renderPath(PathPaintingRenderInfo pathPaintingRenderInfo) {
            return null;
        }

        @Override
        public void clipPath(int i) {

        }

        @Override
        public void beginTextBlock() {

        }

        @Override
        public void renderText(TextRenderInfo textRenderInfo) {

        }

        @Override
        public void endTextBlock() {

        }

        @Override
        public void renderImage(ImageRenderInfo imageRenderInfo) {
            // get the image bounds
            int x = (int) imageRenderInfo.getStartPoint().get(0);
            int y = (int) imageRenderInfo.getStartPoint().get(1);
            int w = (int) imageRenderInfo.getImageCTM().get(Matrix.I11);
            int h = (int) imageRenderInfo.getImageCTM().get(Matrix.I22);

            images.add(new Rectangle(x, y, w, h));
        }
    }

}
