package de.renebergelt.pdfrevise;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import de.renebergelt.pdfrevise.tasks.*;
import org.apache.pdfbox.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PdfRevise {

    public static final String VERSION = "1.1.0";
    public static final String COPYRIGHT = "Copyright 2019 Ren\u00E9 Bergelt";

    public static void main(String[] args) {

        try {
            Options opt = parseCommandLine(args);

            if (!opt.help) {
                run(opt);
            }
        } catch (ParameterException e) {
            System.out.println("Wrong command line options: " + e.getMessage());
        }
    }

    static Options parseCommandLine(String[] args) throws ParameterException {
        Options opt = new Options();

        JCommander cmd = JCommander.newBuilder()
                .addObject(opt)
                .build();
        cmd.setProgramName("java -jar pdf-revise.jar");

        // when no parameters are given, show usage help
        if (args.length == 0) {
            opt.help = true;
        } else {
            // parse command line options

            try {
                cmd.parse(args);
            } catch (ParameterException e) {
                throw e;
            }
        }

        opt.validate();

        if (opt.help) {
            showHelp(cmd);
        }

        return opt;
    }

    static void showHelp(JCommander cmd) {
        System.out.println("pdfdraft " + VERSION);
        System.out.println(COPYRIGHT);
        cmd.usage();
    }

    static void run(Options opt) {

        List<PdfTask> tasks = new ArrayList<>();

        if (!opt.pageSelector.isEmpty()) tasks.add(new SelectPages(opt.pageSelector));

        if (opt.addPageWatermark) tasks.add(new PageWatermarkStamper(opt.watermarkText, opt.pageWatermarkLayer));
        if (opt.addImageWatermark) tasks.add(new ImageWatermarkStamper(opt.watermarkText));

        if (!opt.appendFilename.isEmpty()) tasks.add(new AppendPdf(opt.appendFilename));

        if (!opt.exportToFolder.isEmpty()) tasks.add(new ExportPagesAsImages(opt.renderDpi, opt.exportToFolder));
        if (opt.renderPages) tasks.add(new RenderPages(opt.renderDpi));

        if (opt.disableCopyPaste) tasks.add(new DisableCopyPaste());

        InputStream inStream = null;

        try {
            try {
                inStream = new FileInputStream(opt.inFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("input file '" + opt.inFile + "' does not exist", e);
            }

            if (tasks.size() == 0) {
                System.out.println("No tasks have been selected: output file will just be a copy of input file");
            }

            for (PdfTask task : tasks) {
                System.out.print(task.getDescription());

                try (ByteArrayOutputStream bufStream = new ByteArrayOutputStream()) {
                    task.process(inStream, bufStream, (p) -> System.out.print("."));

                    // the process result becomes the input stream for the next task
                    InputStream newStream = new ByteArrayInputStream(bufStream.toByteArray());
                    IOUtils.closeQuietly(inStream);
                    inStream = newStream;
                } catch (TaskFailedException | IOException e) {
                    throw new RuntimeException(e);
                }

                System.out.println();
            }

            // write the result file
            try (FileOutputStream outStream = new FileOutputStream(opt.outFile)) {
                IOUtils.copy(inStream, outStream);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } finally {
            if (inStream != null)
                IOUtils.closeQuietly(inStream);
        }
    }
}
