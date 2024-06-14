package com.sos.reports;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class ReportGeneratorTestMain {
 // 1. top n frequently failed workflows
 // 2. top n frequently failed jobs
 // 3. top n agents with most parallel execution
 // 4. top n high criticality failed jobs
 // 5. top n frequently failed workflows with cancelled orders
 // 6. top n workflows with the longest execution time
 // 7. top n jobs with the longest execution time
 // 8. top n periods during which mostly workflows executed.
 // 9. top n periods during which mostly jobs executed.

     @Test
    public void test() throws Exception {

        String report = "10";
        String hits = "8";
        String inputDirectory = "C:/Program Files/sos-berlin.com/js7/joc/jetty_base/reporting/data";
        String outputDirectory = "c:/tmp";
        String frequency = "monthly";
        String monthFrom = "2024-04";
        String monthTo = "2024-05";
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
