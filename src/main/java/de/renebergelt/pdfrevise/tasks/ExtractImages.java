package de.renebergelt.pdfrevise.tasks;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import de.renebergelt.pdfrevise.types.PageFilter;
import de.renebergelt.pdfrevise.types.TaskFailedException;
import de.renebergelt.pdfrevise.types.TaskOptions;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

public class ExtractImages implements PdfTask<ExtractImages.ExtractImagesOptions> {

    @Parameters(separators = "=", commandDescription = "Render pages as images to the given folder (in png format)")
    public static class ExtractImagesOptions implements TaskOptions {

        @Override
        public String getTaskVerb() {
            return "extract-images";
        }

        @Parameter(required=true, description = "[target folder]")
        public String targetFolder;
    }

    @Override
    public String getDescription() {
        return "Extracting images";
    }

    @Override
    public ExtractImagesOptions getDefaultOptions() {
        return new ExtractImagesOptions();
    }

    @Override
    public void process(ExtractImagesOptions options, InputStream inStream, OutputStream outStream, PageFilter filter, Consumer<Float> progressCallback) throws TaskFailedException {
        try {
            PDDocument inDocument = PDDocument.load(inStream);

            PDPageTree list = inDocument.getPages();
            int pageNo = 1;
            for (PDPage page : list) {
                PDResources pdResources = page.getResources();
                int i = 1;
                for (COSName name : pdResources.getXObjectNames()) {
                    PDXObject o = pdResources.getXObject(name);
                    if (o instanceof PDImageXObject) {
                        PDImageXObject image = (PDImageXObject)o;
                        String filename = options.targetFolder + "/" + "extracted-image_p" + pageNo + "_" + i + ".png";
                        ImageIO.write(image.getImage(), "png", new File(filename));
                        i++;
                    }
                }

                pageNo++;
            }

            inDocument.close();
        } catch (IOException e) {
            throw new TaskFailedException("Image extraction failed: " + e.getMessage(), e);
        }
    }

}
