package com.sos.js7.converter.autosys;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class Autosys2JS7ConverterMainTest {

    @Ignore
    @Test
    public void test() {
        String config = "src/test/resources/js7_convert_autosys.config";
        String input = "src/test/resources/input";
        String outputDir = "src/test/resources/output";

        // input = "D:/_Workspace/_tools/js7.x-converter/autosys/bistum-mainz.de/PRD_AllJobsSicherung_20240126175500.txt";
        input = "D:/_Workspace/_tools/js7.x-converter/autosys/bistum-mainz.de/PRD_AllJobsSicherung.xml";
        input = "D:/_Workspace/_tools/js7.x-converter/autosys/bistum-mainz.de/PRD_AllJobsSicherung.ap.xml";
        //input = "D:/_Workspace/_tools/js7.x-converter/autosys/bistum-mainz.de/klein.xml";
        
        List<String> args = new ArrayList<>();
        args.add("--config=" + config);
        args.add("--input=" + input);
        args.add("--output-dir=" + outputDir + "/live");
        args.add("--report-dir=" + outputDir + "/report");
        args.add("--archive=" + outputDir + "/js7_converted.zip");

        Autosys2JS7ConverterMain.main(args.stream().toArray(String[]::new));
    }
}
