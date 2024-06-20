package com.sos.reports;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ReportGeneratorTestMain {
 // 1. top n workflows with highest/lowest number of failed executions
 // 2. top n jobs with highest/lowest number of failed executions
 // 3. top n agents with  highest/lowest number of parallel job execution
 // 4. top n high criticality jobs with highest/lowest number of failed executions
 // 5. top n workflows with highest/lowest number of failed executions for cancelled orders.
 // 6. top n workflows with highest/lowest need for execution time
 // 7. top n jobs with highest/lowest need for execution time
 // 8. top n periods with highest/lowest number of workflows executions
 // 9. top n periods with highest/lowest number of workflows executions
 //10. top n jobs with highest/lowest number of successful executions
 //11. top n workflows with highest/lowest number of successful executions

     @Test
    public void test() throws Exception {

        String report = "ReportSuccessfulJobs";
        String hits = "8";
        String inputDirectory = "C:/Program Files/sos-berlin.com/js7/joc/jetty_base/reporting/data";
        String outputDirectory = "c:/tmp";
        String frequency = "monthly";
        String monthFrom = "2021-01";
        String monthTo = "2021-12";
       // String controller = "controller";
        String sort = "HIGHEST";
        
        List<String> args = new ArrayList<>();
        args.add("-r");args.add(report);
        args.add("-n");args.add(hits);
        args.add("--inputDirectory");args.add(inputDirectory);
        args.add("-p");args.add(frequency);
        args.add("-o");args.add(outputDirectory);
        args.add("-s");args.add(monthFrom);
       // args.add("-c");args.add(controller);
        args.add("-e");args.add(monthTo);
        args.add("-f");args.add(sort);

        ReportGenerator.main(args.stream().toArray(String[]::new));
    }
}
