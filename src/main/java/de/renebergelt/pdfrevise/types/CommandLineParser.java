package de.renebergelt.pdfrevise.types;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import de.renebergelt.pdfrevise.Options;
import de.renebergelt.pdfrevise.tasks.TaskFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class CommandLineParser {

    // JCommander does not support parsing multiple commands
    // therefore, we split the command line into main options
    // and commands and parse each command section individually

    private TaskFactory taskFactory;
    private JCommander cmd;

    public JCommander getCommander() {
        return cmd;
    }

    public CommandLineParser(TaskFactory taskFactory) {
        if (taskFactory == null)
            throw new IllegalArgumentException("Parameter taskfactory must not be null");

        this.taskFactory = taskFactory;
    }

    public void parse(List<String> args, Options target) throws ParameterException {

        cmd = JCommander.newBuilder()
                .addObject(target)
                .acceptUnknownOptions(true)
                .build();

        // when no parameters are given, show usage help
        if (args.size() == 0) {
            target.help = true;
        } else {
            // parse command line options
            try {
                // the first parameter has to be the input file!
                if ("--help".equals(args.get(0))) {
                    cmd.parse(args.toArray(new String[0]));
                } else {
                    target.inFile = args.get(0);
                    cmd.parse(args.subList(1, args.size()).toArray(new String[0]));
                }

                if (!target.help) {
                    // get the unknown options -> these are the task commands
                    List<String> subCommands = cmd.getUnknownOptions();
                    target.getTasks().addAll(parseTasks(subCommands));
                }
            } catch (ParameterException e) {
                throw e;
            }
        }

        target.validate();

    }

    private List<TaskOptions> parseTasks(List<String> args) throws ParameterException {
        List<TaskOptions> tasks = new ArrayList<>();

        int i = 0;
        while(i < args.size()) {
            String verb = args.get(i);
            // find end (all command arguments start with a hyphen)
            List<String> taskArgs;
            if (i + 1 < args.size()) {
                int argCount = getTaskArgCount(args.subList(i+1, args.size()));
                taskArgs = args.subList(i + 1, i + 1 + argCount);
                i += 1 + argCount;
            }
            else {
                taskArgs = new ArrayList<String>();
                i++;
            }


            tasks.add(parseTask(verb, taskArgs));
        }

        return tasks;
    }

    private int getTaskArgCount(List<String> args) {
        int i = 0;
        while(i < args.size() && !taskFactory.isTaskVerb(args.get(i))) {
            i++;
        }

        return i;
    }

    private TaskOptions parseTask(String verb, List<String> arguments) throws ParameterException {
        try {
            TaskOptions options = taskFactory.createOptions(verb);

            JCommander cmd = JCommander.newBuilder()
                    .addObject(options)
                    .build();

            try {
                cmd.parse(arguments.toArray(new String[arguments.size()]));
            } catch (ParameterException e) {
                throw new ParameterException("Error while parsing options for task '" + verb + "': " + e.getMessage(), e);
            }

            return options;

        } catch (NoSuchElementException e) {
            throw new ParameterException("The task " + verb + " is not valid");
        }
    }

}
