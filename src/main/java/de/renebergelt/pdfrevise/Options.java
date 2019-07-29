package de.renebergelt.pdfrevise;

import com.beust.jcommander.Parameter;
import org.apache.commons.io.FilenameUtils;

/**
 * Command line options for pdf-draft
 */
public class Options {

        public static final String defaultOutFile = "[input pdf file].out.pdf";

        @Parameter(required = true)
        public String inFile = "";

        @Parameter(names = {"-o", "--out"}, description = "Name of the output file.", order = 0)
        public String outFile = defaultOutFile;

        @Parameter(names = {"--pages"}, description = "The pages to which operations should be applied to (e.g. 1,2-5,!3,6-10). All pages are kept in the pdf and only the selected will be modified.", order = 1)
        public String pageSelector = "";

        @Parameter(names = {"--page-watermark"}, description = "Add a watermark to the background of every page", order = 2)
        public boolean addPageWatermark = false;

        @Parameter(names = {"--image-watermark"}, description = "Add a watermark over every image", order = 3)
        public boolean addImageWatermark = false;

        @Parameter(names = {"--watermark-text"}, description = "The watermark text", order = 4)
        public String watermarkText = "DRAFT";

        @Parameter(names = {"--page-watermark-layer"}, description = "Where to put the watermark text for pages (background or foreground)", order = 5)
        public StampLayer pageWatermarkLayer = StampLayer.BACKGROUND;

        @Parameter(names = {"--render"}, description = "Convert pages to images", order = 6)
        public boolean renderPages = false;

        @Parameter(names = {"--dpi"}, description = "The DPI to render the pdf pages with", order = 7)
        public int renderDpi = 120;

        @Parameter(names = {"--render-to-folder"}, description = "Render pages as images to the given folder", order = 8)
        public String renderToFolder = "";

        @Parameter(names = {"--extract"}, description = "Extract only the filtered pages from the pdf and copy them to the target", order = 9)
        public boolean extractPages = false;

        @Parameter(names = {"--append"}, description = "Append the given pdf file to the input file", order = 10)
        public String appendFilename = "";

        @Parameter(names = {"--disable-copy-paste"}, description = "Disables the Copy/Paste function in pdf readers", order = 11)
        public boolean disableCopyPaste = false;

        @Parameter(names = {"--help", "-?", "--?", "/?"}, help = true, description = "Show this help screen", order = 50)
        public boolean help;

        public void validate() {
                if (outFile.isEmpty() || defaultOutFile.equals(outFile) ) {
                        outFile = FilenameUtils.removeExtension(inFile) + ".out." + FilenameUtils.getExtension(inFile);
                }
        }
}
