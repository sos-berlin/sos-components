package com.sos.reports;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ReportGeneratorTestMain {

    @Test
    public void test() throws Exception {

        String report = "1";
        String inputDirectory = "c:/temp/1";
        String outputDirectory = "c:/temp";
        String frequency = "weekly";
        String monthFrom = "2023-01";

        List<String> args = new ArrayList<>();
        args.add("-r");args.add(report);
        args.add("--inputDirectory");args.add(inputDirectory);
        args.add("-p");args.add(frequency);
        args.add("-o");args.add(outputDirectory);
        args.add("-s");args.add(monthFrom);

        ReportGenerator.main(args.stream().toArray(String[]::new));
    }
}
