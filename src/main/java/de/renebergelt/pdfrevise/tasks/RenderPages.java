package de.renebergelt.pdfrevise.tasks;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import de.renebergelt.pdfrevise.types.NullPageFilter;
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

public class RenderPages implements PdfTask<RenderPages.RenderPagesOptions> {

    @Override
    public String getDescription() {
        return "Rendering pages";
    }

    @Parameters(separators = "=", commandDescription = "Add a watermark to every page")
    public static class RenderPagesOptions implements TaskOptions {

        @Override
        public String getTaskVerb() {
            return "render-pages";
        }

        @Parameter(names={"--dpi"}, description = "The DPI to render the pdf pages with")
        public int renderDpi;
    }

    @Override
    public RenderPagesOptions getDefaultOptions() {
        return new RenderPagesOptions();
    }

    @Override
    public void process(RenderPagesOptions options, InputStream inStream, OutputStream outStream, PageFilter filter, Consumer<Float> progressCallback) throws TaskFailedException {
        try {

            if (filter.getClass() != NullPageFilter.class) {
                throw new TaskFailedException("Currently RenderPages does not support page filtering.", new IllegalArgumentException("filter"));
            }

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

            // render pages using pdfbox and write them using itext to a new document
            PDDocument inDocument = PDDocument.load(inStream);
            PDFRenderer renderer = new PDFRenderer(inDocument);

            Document outDocument = new Document();
            PdfWriter writer = PdfWriter.getInstance(outDocument, outStream);
            outDocument.open();
            writer.open();

            int pageCount = inDocument.getNumberOfPages();

            for(int p = 0; p < pageCount; p++) {

                // Todo: if not included in filter -> copy original page instead of rendering it
                BufferedImage pageImg = renderer.renderImageWithDPI(p, options.renderDpi, ImageType.RGB);
                outDocument.setPageSize(pageSizes.get(p));
                outDocument.newPage();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(pageImg, "jpg", baos);

                Image img = Image.getInstance(baos.toByteArray());
                img.setAbsolutePosition(0, 0);
                img.scaleToFit(pageSizes.get(p).getWidth(), pageSizes.get(p).getHeight());
                outDocument.add(img);

                progressCallback.accept( (p+1)/ (float)pageCount );
            }

            inDocument.close();
            outDocument.close();
            writer.close();
        } catch (IOException | DocumentException e) {
            throw new TaskFailedException("Failed to render pages: " + e.getMessage(), e);
        }
    }

}
