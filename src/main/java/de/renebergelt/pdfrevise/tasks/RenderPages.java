package de.renebergelt.pdfrevise.tasks;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfCopy;
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

public class RenderPages implements PdfTask {

    int dpi;

    @Override
    public String getDescription() {
        return "Rendering pages";
    }

    public RenderPages(int dpi) {
        this.dpi = dpi;
    }

    @Override
    public void process(InputStream srcStream, OutputStream targetStream, PageFilter filter, Consumer<Float> progressCallback) throws TaskFailedException{
        try {

            if (filter.getClass() != NullPageFilter.class) {
                throw new TaskFailedException("Currently RenderPages does not support page filtering.", new IllegalArgumentException("filter"));
            }

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

            // render pages using pdfbox and write them using itext to a new document
            PDDocument inDocument = PDDocument.load(srcStream);
            PDFRenderer renderer = new PDFRenderer(inDocument);

            Document outDocument = new Document();
            PdfWriter writer = PdfWriter.getInstance(outDocument, targetStream);
            outDocument.open();
            writer.open();

            int pageCount = inDocument.getNumberOfPages();

            for(int p = 0; p < pageCount; p++) {

                // Todo: if not included in filter -> copy original page instead of rendering it
                BufferedImage pageImg = renderer.renderImageWithDPI(p, dpi, ImageType.RGB);
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
