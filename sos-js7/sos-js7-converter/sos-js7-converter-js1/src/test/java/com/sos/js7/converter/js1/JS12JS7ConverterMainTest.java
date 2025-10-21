package com.sos.js7.converter.js1;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSShell;

public class JS12JS7ConverterMainTest {

    @Ignore
    @Test
    public void testDependencies() throws Exception {

        SOSPath.deleteIfExists(Paths.get("target/dependency"));

        // cmd>mvn dependency:tree

        System.out.println(SOSShell.executeCommand("mvn dependency:tree"));
        System.out.println("###############");
        System.out.println(SOSShell.executeCommand("mvn dependency:list"));
        System.out.println("###############");

        // copy to target/dependency/
        System.out.println(SOSShell.executeCommand("mvn dependency:copy-dependencies -DincludeScope=runtime -Doverwrite=true"));
        // System.out.println(SOSShell.executeCommand("mvn dependency:copy-dependencies -Doverwrite=true"));
    }

    @Ignore
    @Test
    public void test() throws Exception {
        String config = "src/test/resources/js7_convert_js1.config";
        String input = "src/test/resources/input";
        String outputDir = "src/test/resources/output";

        SOSPath.deleteIfExists(Paths.get(outputDir).resolve("live"));
        SOSPath.deleteIfExists(Paths.get(outputDir).resolve("report"));

        List<String> args = new ArrayList<>();
        args.add("--config=" + config);
        args.add("--input=" + input);
        args.add("--output-dir=" + outputDir + "/live");
        args.add("--report-dir=" + outputDir + "/report");
        args.add("--archive=" + outputDir + "/js7_converted.zip");

        JS12JS7ConverterMain.main(args.stream().toArray(String[]::new));
    }

}
