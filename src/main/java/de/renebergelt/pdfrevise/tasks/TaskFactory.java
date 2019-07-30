package de.renebergelt.pdfrevise.tasks;

import de.renebergelt.pdfrevise.types.TaskOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

public class TaskFactory {

    List<PdfTask> availableTasks = new ArrayList<>();
    HashMap<String, PdfTask> taskVerbs = new HashMap<>();

    public TaskFactory() {
        discoverTasks();
    }

    private void discoverTasks() {
        availableTasks.add(new ImageWatermarkStamper());
        availableTasks.add(new PageWatermarkStamper());
        availableTasks.add(new AddPageNumbers());

        availableTasks.add(new RenderPages());

        availableTasks.add(new ExtractPages());
        availableTasks.add(new AppendPdf());
        availableTasks.add(new ExportPagesAsImages());

        availableTasks.add(new DisableCopyPaste());

        availableTasks.add(new UpdatePageFilter());

        for(PdfTask task: availableTasks) {
            String verb = task.getDefaultOptions().getCommandName();
            taskVerbs.put(verb, task);
        }
    }

    public TaskOptions createOptions(String taskVerb) throws NoSuchElementException  {
        PdfTask task = getTask(taskVerb);
        return task.getDefaultOptions();
    }

    public boolean isTaskVerb(String s) {
        return taskVerbs.containsKey(s);
    }

    public PdfTask getTask(String taskVerb) {
        PdfTask task = taskVerbs.get(taskVerb);
        if (task == null)
            throw new NoSuchElementException(taskVerb);

        return task;
    }

    public List<PdfTask> getAvailableTasks() {
        return availableTasks;
    }

}
