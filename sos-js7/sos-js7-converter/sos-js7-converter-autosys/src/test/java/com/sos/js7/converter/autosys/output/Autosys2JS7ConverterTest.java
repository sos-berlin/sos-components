package com.sos.js7.converter.autosys.output;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.js7.converter.autosys.input.XMLJobParser;
import com.sos.js7.converter.commons.JS7ConverterConfig.Platform;

public class Autosys2JS7ConverterTest {

    @Ignore
    @Test
    public void test() throws IOException {
        Path input = Paths.get("src/test/resources/input/xml");
        Path outputDir = Paths.get("src/test/resources/output");
        Path reportDir = Paths.get("src/test/resources/output/report");

        Autosys2JS7Converter.CONFIG.getGenerateConfig().withWorkflows(true).withSchedules(true).withLocks(true).withCyclicOrders(false);
        Autosys2JS7Converter.CONFIG.getAgentConfig().withForcedPlatform(Platform.UNIX).withMapping("abcd=agent;xyz=agent_cluster");// .withForcedName("my_agent_name");
        Autosys2JS7Converter.CONFIG.getMockConfig().withScript("$HOME/MockScript.sh");
        Autosys2JS7Converter.CONFIG.getScheduleConfig().withDefaultCalendarName("AnyDays").withPlanOrders(true).withSubmitOrders(true);
        Autosys2JS7Converter.CONFIG.getJobConfig().withForcedGraceTimeout(15).withForcedParallelism(1).withForcedFailOnErrWritten(true)
                .withScriptNewLine("\n");
        Autosys2JS7Converter.CONFIG.getSubFolderConfig().withMapping("aapg=2;ebzc=0;wmad=0;abcd=0").withSeparator("_");

        Autosys2JS7Converter.convert(new XMLJobParser(), input, outputDir, reportDir);
    }
}
