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

        @Parameter(names = {"--out"}, description = "Name of the output file.", order = 0)
        public String outFile = defaultOutFile;

        @Parameter(names = {"--page-watermark"}, description = "Add a watermark to the background of every page", order = 1)
        public boolean addPageWatermark = false;

        @Parameter(names = {"--image-watermark"}, description = "Add a watermark over every image", order = 2)
        public boolean addImageWatermark = false;

        @Parameter(names = {"--watermark-text"}, description = "The watermark text", order = 3)
        public String watermarkText = "DRAFT";

        @Parameter(names = {"--page-watermark-layer"}, description = "Where to put the watermark text for pages (background or foreground)", order = 3)
        public StampLayer pageWatermarkLayer = StampLayer.BACKGROUND;

        @Parameter(names = {"--render"}, description = "Convert pages to images", order = 4)
        public boolean renderPages = false;

        @Parameter(names = {"--dpi"}, description = "The DPI to render the pdf pages with", order = 5)
        public int renderDpi = 120;

        @Parameter(names = {"--export"}, description = "Export pages as images to the given folder", order = 6)
        public String exportToFolder = "";

        @Parameter(names = {"--pages"}, description = "The pages to include in the output (e.g. 1,2-5,!3,6-10)", order = 7)
        public String pageSelector = "";

        @Parameter(names = {"--append"}, description = "Append the given pdf file to the input file", order = 8)
        public String appendFilename = "";

        @Parameter(names = {"--disable-copy-paste"}, description = "Disables the Copy/Paste function in pdf readers", order = 9)
        public boolean disableCopyPaste = false;

        @Parameter(names = {"--help", "-?", "--?", "/?"}, help = true, description = "Show this help screen", order = 50)
        public boolean help;

        public void validate() {
                if (outFile.isEmpty() || defaultOutFile.equals(outFile) ) {
                        outFile = FilenameUtils.removeExtension(inFile) + ".out." + FilenameUtils.getExtension(inFile);
                }
        }
}
