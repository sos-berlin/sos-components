package com.sos.js7.converter.js1;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class JS12JS7ConverterMainTest {

    @Ignore
    @Test
    public void test() {
        String config = "src/test/resources/js7_convert_js1.config";
        String input = "src/test/resources/input";
        String outputDir = "src/test/resources/output";

        List<String> args = new ArrayList<>();
        args.add("--config=" + config);
        args.add("--input=" + input);
        args.add("--output-dir=" + outputDir + "/live");
        args.add("--report-dir=" + outputDir + "/report");
        args.add("--archive=" + outputDir + "/js7_converted.tar.gz");

        JS12JS7ConverterMain.main(args.stream().toArray(String[]::new));
    }
}
