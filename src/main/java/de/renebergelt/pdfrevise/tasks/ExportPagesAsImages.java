package de.renebergelt.pdfrevise.tasks;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.function.Consumer;

public class ExportPagesAsImages implements PdfTask {

    int dpi;
    String targetFolder;

    @Override
    public String getDescription() {
        return "Extracting pages";
    }

    public ExportPagesAsImages(int dpi, String targetFolder) {
        this.dpi = dpi;
        this.targetFolder = targetFolder;
    }

    @Override
    public void process(InputStream srcStream, OutputStream targetStream, PageFilter filter, Consumer<Float> progressCallback) {
        try {
            // todo: create target folder if it does not exist

            // read page sizes from pdf
            // in a pdf each page can have a different size
            PdfReader reader = new PdfReader(srcStream);
            java.util.List<Rectangle> pageSizes = new ArrayList<>();
            for(int i = 1; i <= reader.getNumberOfPages(); i++) {
                pageSizes.add(reader.getPageSize(i));
            }
            reader.close();

            // rewind the stream
            srcStream.reset();

            // render pages using pdfbox and write as image file
            PDDocument inDocument = PDDocument.load(srcStream);
            PDFRenderer renderer = new PDFRenderer(inDocument);

            int pageCount = inDocument.getNumberOfPages();

            for(int p = 0; p < pageCount; p++) {
                BufferedImage pageImg = renderer.renderImageWithDPI(p, dpi, ImageType.RGB);

                if (filter.isPageInFilter(p+1)) {
                    try (FileOutputStream fStream = new FileOutputStream(targetFolder + "/" + String.valueOf(p + 1) + ".png")) {
                        ImageIO.write(pageImg, "png", fStream);
                    }
                }

                progressCallback.accept( (p+1)/ (float)pageCount );
            }

            inDocument.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
