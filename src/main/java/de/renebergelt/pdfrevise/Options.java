package de.renebergelt.pdfrevise;

import com.beust.jcommander.Parameter;
import de.renebergelt.pdfrevise.types.TaskOptions;
import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Command line options for pdf-draft
 */
public class Options {

        public static final String defaultOutFile = "[input pdf file].out.pdf";

        // parsed manually, since main parameter seems not to work with all unknown options in JCommander
        public String inFile;

        @Parameter(names = {"-o", "--out"}, description = "Name of the output file.", order = 0)
        public String outFile = defaultOutFile;

        @Parameter(names = {"--help", "-?", "--?", "/?"}, help = true, description = "Show this help screen", order = 50)
        public boolean help;

        private List<TaskOptions> tasks = new ArrayList<TaskOptions>();

        public List<TaskOptions> getTasks() {
                return tasks;
        }

        public void validate() {
                if (outFile.isEmpty() || defaultOutFile.equals(outFile) ) {
                        outFile = FilenameUtils.removeExtension(inFile) + ".out." + FilenameUtils.getExtension(inFile);
                }
        }
}
