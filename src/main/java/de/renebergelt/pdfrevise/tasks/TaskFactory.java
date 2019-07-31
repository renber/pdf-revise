package de.renebergelt.pdfrevise.tasks;

import de.renebergelt.pdfrevise.types.TaskOptions;
import org.reflections.Reflections;

import java.util.*;

public class TaskFactory {

    List<PdfTask> availableTasks = new ArrayList<>();
    HashMap<String, PdfTask> taskVerbs = new HashMap<>();

    public TaskFactory() {
        discoverTasks();
    }

    private void discoverTasks() {
        // find all tasks by searching classes which implement the PdfTask interface
        Reflections reflections = new Reflections("de.renebergelt.pdfrevise.tasks");
        Set<Class<? extends PdfTask>> taskClasses =  reflections.getSubTypesOf(PdfTask.class);

        for(Class<? extends PdfTask> taskClass: taskClasses) {
            try {
                PdfTask task = taskClass.newInstance();
                availableTasks.add(task);
            } catch (InstantiationException e) {
                // todo: log
            } catch (IllegalAccessException e) {
                // todo: log
            }
        }

        for(PdfTask task: availableTasks) {
            String verb = task.getDefaultOptions().getTaskVerb();
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
