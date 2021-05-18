package de.renebergelt.pdfrevise;

import de.renebergelt.pdfrevise.tasks.PdfTask;
import de.renebergelt.pdfrevise.tasks.TaskFactory;
import de.renebergelt.pdfrevise.tasks.UpdatePageFilter;
import de.renebergelt.pdfrevise.types.*;
import org.apache.pdfbox.io.IOUtils;

import java.io.*;
import java.util.List;

/**
 * PdfRevise's main logic class which runs tasks on a pdf
 */
public class PdfRevise {

    /**
     * Run PdfRevise with specified options
     */
    public void run(Options opt, TaskFactory taskFactory) throws TaskFailedException {
        try (InputStream inStream = new FileInputStream(opt.inFile);
             ByteArrayOutputStream memStream = new ByteArrayOutputStream();
             FileOutputStream outStream = new FileOutputStream(opt.outFile)) {

            // copy file stream into memory, to ensure random read/write
            IOUtils.copy(inStream, memStream);
            ByteArrayInputStream workStream = new ByteArrayInputStream((memStream.toByteArray()));

            run(workStream, outStream, opt.getTasks(), taskFactory);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("input file '" + opt.inFile + "' does not exist", e);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Run the given tasks on inStream and write the result to outStream
     */
    public void run(InputStream inStream, OutputStream outStream, List<TaskOptions> tasks, TaskFactory taskFactory) throws TaskFailedException {
        PageFilter pageFilter = new NullPageFilter();

        if (tasks.size() == 0) {
            System.out.println("No tasks have been selected: output file will just be a copy of input file");
        }

        int taskNumber = 1;
        int taskCount = tasks.size();
        for (TaskOptions taskOptions : tasks) {

            if (taskOptions.getClass() == UpdatePageFilter.PageFilterOptions.class) {
                // this is not a real task -> we update the currently active page filter
                pageFilter = new SequencePageFilter(((UpdatePageFilter.PageFilterOptions) taskOptions).pageSelector);
            } else {
                PdfTask task = taskFactory.getTask(taskOptions.getTaskVerb());

                System.out.print("Running Task " + taskNumber + ": " + task.getDescription());

                try (ByteArrayOutputStream bufStream = new ByteArrayOutputStream()) {
                    task.process(taskOptions, inStream, bufStream, pageFilter, (p) -> System.out.print("."));

                    // the process result becomes the input stream for the next task
                    InputStream newStream = new ByteArrayInputStream(bufStream.toByteArray());
                    IOUtils.closeQuietly(inStream);
                    inStream = newStream;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    System.out.println();
                }
                
                taskNumber++;
            }
        }

        // write the result to out stream
        try {
            IOUtils.copy(inStream, outStream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
