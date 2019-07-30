package de.renebergelt.pdfrevise;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import de.renebergelt.pdfrevise.tasks.*;
import de.renebergelt.pdfrevise.types.*;
import org.apache.pdfbox.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PdfRevise {

    public static final String VERSION = "1.1.0";
    public static final String COPYRIGHT = "Copyright 2019 Ren\u00E9 Bergelt";

    public static void main(String[] args) {

        try {
            TaskFactory taskfactory = new TaskFactory();

            Options opt = parseCommandLine(args, taskfactory);

            if (opt.help) {
                showHelp(taskfactory);
            } else {
                run(opt, taskfactory);
            }
        } catch (ParameterException e) {
            System.out.println("Wrong command line options: " + e.getMessage());
        }
    }

    static Options parseCommandLine(String[] args, TaskFactory taskfactory) throws ParameterException {
        Options opt = new Options();

        CommandLineParser parser = new CommandLineParser(taskfactory);
        parser.parse(Arrays.asList(args), opt);

        return opt;
    }

    static void showHelp(TaskFactory taskFactory) {
        Options mainOptions = new Options();

        JCommander.Builder builder = JCommander
                .newBuilder()
                .addObject(mainOptions);

        for(PdfTask task: taskFactory.getAvailableTasks()) {
            TaskOptions taskOptions = task.getDefaultOptions();
            builder.addCommand(taskOptions.getCommandName(), taskOptions);
        }

        JCommander cmd = builder.build();
        cmd.setProgramName("java -jar pdf-revise.jar [input pdf file]");

        System.out.println("pdfdraft " + VERSION);
        System.out.println(COPYRIGHT);
        StringBuilder sb = new StringBuilder();
        cmd.usage(sb);
        String helpText = sb.toString();
        // indicate that multiple commands be used
        helpText = helpText.replace("[command]", "[command1] [command1 options] [command2] [command2 options] ...");
        System.out.println(helpText);
    }

    static void run(Options opt, TaskFactory taskFactory) {

        PageFilter pageFilter = new NullPageFilter();

        InputStream inStream = null;

        try {
            try {
                inStream = new FileInputStream(opt.inFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("input file '" + opt.inFile + "' does not exist", e);
            }

            if (opt.getTasks().size() == 0) {
                System.out.println("No tasks have been selected: output file will just be a copy of input file");
            }

            int taskNumber = 1;
            int taskCount = opt.getTasks().size();
            for (TaskOptions taskOptions : opt.getTasks()) {

                if (taskOptions.getClass() == UpdatePageFilter.PageFilterOptions.class) {
                    // this is not a real task -> we update the currently active page filter
                    pageFilter = new SequencePageFilter( ((UpdatePageFilter.PageFilterOptions)taskOptions).pageSelector );
                } else {
                    PdfTask task = taskFactory.getTask(taskOptions.getCommandName());

                    System.out.print("Running Task " + taskNumber + ": " + task.getDescription());

                    try (ByteArrayOutputStream bufStream = new ByteArrayOutputStream()) {
                        task.process(taskOptions, inStream, bufStream, pageFilter, (p) -> System.out.print("."));

                        // the process result becomes the input stream for the next task
                        InputStream newStream = new ByteArrayInputStream(bufStream.toByteArray());
                        IOUtils.closeQuietly(inStream);
                        inStream = newStream;
                    } catch (TaskFailedException | IOException e) {
                        throw new RuntimeException(e);
                    }

                    System.out.println();
                }
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
