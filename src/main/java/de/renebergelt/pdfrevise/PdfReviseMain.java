package de.renebergelt.pdfrevise;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import de.renebergelt.pdfrevise.tasks.*;
import de.renebergelt.pdfrevise.types.*;

import java.util.*;

/**
 * Command line interface to PdfRevise
 */
public class PdfReviseMain {

    public static final String VERSION = "1.2.0";
    public static final String COPYRIGHT = "Copyright 2019 Ren\u00E9 Bergelt";

    public static void main(String[] args) {

        try {
            TaskFactory taskfactory = new TaskFactory();

            Options opt = parseCommandLine(args, taskfactory);

            if (opt.help) {
                showHelp(taskfactory);
            } else {
                PdfRevise pdfrevise = new PdfRevise();
                pdfrevise.run(opt, taskfactory);
            }
        } catch (ParameterException e) {
            System.out.println("Wrong command line options: " + e.getMessage());
        } catch (TaskFailedException e) {
            System.out.println("Error while executing task: " + e.getMessage());
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

        List<TaskOptions> availableTaskOptions = new ArrayList();

        for(PdfTask task: taskFactory.getAvailableTasks()) {
            TaskOptions taskOptions = task.getDefaultOptions();
            availableTaskOptions.add(taskOptions);
        }

        // sort task alphabetically
        Collections.sort(availableTaskOptions, new Comparator<TaskOptions>() {
            @Override
            public int compare(TaskOptions o1, TaskOptions o2) {
                return o1.getTaskVerb().compareTo(o2.getTaskVerb());
            }
        });

        for(TaskOptions taskOption: availableTaskOptions) {
            builder.addCommand(taskOption.getTaskVerb(), taskOption);
        }

        JCommander cmd = builder.build();
        cmd.setProgramName("java -jar pdf-revise.jar [input pdf file]");

        System.out.println("pdfdraft " + VERSION);
        System.out.println(COPYRIGHT);
        StringBuilder sb = new StringBuilder();
        cmd.usage(sb);
        String helpText = sb.toString();

        // change the help text (there is no easy way to override cmd.usage() atm)
        helpText = helpText.replace("Commands", "Tasks");
        helpText = helpText.replaceFirst("Options:", "Options: defined as --option_name=value\n  Tasks are executed in the order they appear on the command line");
        helpText = helpText.replaceFirst("\\[command\\]\\s+\\[command\\s+options\\]", "[task1] [task1 options] [task2] [task2 options] ...");

        System.out.println(helpText);
    }


}
