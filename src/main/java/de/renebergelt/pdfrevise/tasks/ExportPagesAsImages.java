package de.renebergelt.pdfrevise.tasks;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import de.renebergelt.pdfrevise.types.PageFilter;
import de.renebergelt.pdfrevise.types.TaskFailedException;
import de.renebergelt.pdfrevise.types.TaskOptions;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.function.Consumer;

public class ExportPagesAsImages implements PdfTask<ExportPagesAsImages.ExportPagesOptions> {

    @Override
    public String getDescription() {
        return "Exporting pages";
    }

    @Parameters(separators = "=", commandDescription = "Render pages as images to the given folder")
    public static class ExportPagesOptions implements TaskOptions {

        @Override
        public String getTaskVerb() {
            return "render-to-folder";
        }

        @Parameter(required=true, description = "The target folder to render pages to")
        public String targetFolder;

        @Parameter(names={"--dpi"}, description = "The DPI to render the pdf pages with")
        public int renderDpi;
    }

    @Override
    public ExportPagesOptions getDefaultOptions() {
        return new ExportPagesOptions();
    }

    @Override
    public void process(ExportPagesOptions options, InputStream inStream, OutputStream outStream, PageFilter filter, Consumer<Float> progressCallback) throws TaskFailedException {
        try {
            // todo: create target folder if it does not exist

            // read page sizes from pdf
            // in a pdf each page can have a different size
            PdfReader reader = new PdfReader(inStream);
            java.util.List<Rectangle> pageSizes = new ArrayList<>();
            for(int i = 1; i <= reader.getNumberOfPages(); i++) {
                pageSizes.add(reader.getPageSize(i));
            }
            reader.close();

            // rewind the stream
            inStream.reset();

            // render pages using pdfbox and write as image file
            PDDocument inDocument = PDDocument.load(inStream);
            PDFRenderer renderer = new PDFRenderer(inDocument);

            int pageCount = inDocument.getNumberOfPages();

            for(int p = 0; p < pageCount; p++) {
                BufferedImage pageImg = renderer.renderImageWithDPI(p, options.renderDpi, ImageType.RGB);

                if (filter.isPageInFilter(p+1, pageCount)) {
                    try (FileOutputStream fStream = new FileOutputStream(options.targetFolder + "/" + String.valueOf(p + 1) + ".png")) {
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
