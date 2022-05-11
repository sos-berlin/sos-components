package com.sos.js7.converter.commons.report;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

public class ReportWriterTest {

    public enum MyHEADER {
        FOO, BAR;
    }

    @Ignore
    @Test
    public void test() {
        Path outputFile = Paths.get("src/test/resources/test.csv");

        CSVRecords r = new CSVRecords(MyHEADER.class);
        r.addRecord(Arrays.asList("foo1", "bar1"));
        r.addRecord(Arrays.asList("foo2", "bar2"));

        ReportWriter.write(outputFile, r);
    }

}
