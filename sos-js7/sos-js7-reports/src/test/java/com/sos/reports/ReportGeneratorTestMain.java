package com.sos.reports;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ReportGeneratorTestMain {
 // 1. top n frequently failed workflows
 // 2. top n frequently failed jobs
 // 3. top n agents with most parallel execution
 // 4. top n periods of low and high parallism jof job executions
 // 5. top n high criticality failed jobs
 // 6. top n frequently failed workflows with cancelled orders
 // 7. top n workflows with the longest execution time
 // 8. top n jobs with the longest execution time
 // 9. top n periods during which mostly workflows executed.
 // 10. top n periods during which mostly jobs executed.

    @Test
    public void test() throws Exception {

        String report = "8";
        String hits = "3";
        String inputDirectory = "c:/temp/1/jobs";
        String outputDirectory = "c:/temp";
        String frequency = "weekly";
        String monthFrom = "2023-01";

        List<String> args = new ArrayList<>();
        args.add("-r");args.add(report);
        args.add("-n");args.add(hits);
        args.add("--inputDirectory");args.add(inputDirectory);
        args.add("-p");args.add(frequency);
        args.add("-o");args.add(outputDirectory);
        args.add("-s");args.add(monthFrom);

        ReportGenerator.main(args.stream().toArray(String[]::new));
    }
}
