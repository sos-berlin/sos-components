package com.sos.js7.converter.autosys.output.js7;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.js7.converter.autosys.input.XMLJobParser;
import com.sos.js7.converter.commons.JS7ConverterConfig.Platform;

public class JS7ConverterTest {

    @Ignore
    @Test
    public void test() throws IOException {
        Path input = Paths.get("src/test/resources/input/xml");
        Path outputDir = Paths.get("src/test/resources/output");
        Path reportDir = Paths.get("src/test/resources/output/report");

        JS7Converter.CONFIG.getGenerateConfig().withWorkflows(true).withSchedules(true).withLocks(true).withCyclicOrders(false);
        JS7Converter.CONFIG.getAgentConfig().withForcedPlatform(Platform.UNIX).withMapping("abcd=agent;xyz=agent_cluster");// .withForcedName("my_agent_name");
        JS7Converter.CONFIG.getMockConfig().withScript("$HOME/MockScript.sh");
        JS7Converter.CONFIG.getScheduleConfig().withDefaultWorkingCalendarName("AnyDays").withDefaultNonWorkingCalendarName(null).withPlanOrders(true)
                .withSubmitOrders(true);
        JS7Converter.CONFIG.getJobConfig().withForcedGraceTimeout(15).withForcedParallelism(1).withForcedFailOnErrWritten(true).withScriptNewLine(
                "\n");
        JS7Converter.CONFIG.getSubFolderConfig().withMapping("aapg=2;ebzc=0;wmad=0;abcd=0").withSeparator("_");

        JS7Converter.convert(new XMLJobParser(), input, outputDir, reportDir);
    }
}
