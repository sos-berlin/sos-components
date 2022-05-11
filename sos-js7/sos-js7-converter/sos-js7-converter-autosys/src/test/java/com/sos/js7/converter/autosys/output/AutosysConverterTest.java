package com.sos.js7.converter.autosys.output;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.js7.converter.autosys.input.XMLJobParser;
import com.sos.js7.converter.commons.JS7ConverterConfig.Platform;

public class AutosysConverterTest {

    @Ignore
    @Test
    public void test() throws IOException {
        Path inputDir = Paths.get("src/test/resources/input/xml");
        Path outputDir = Paths.get("src/test/resources/output");
        Path reportDir = Paths.get("src/test/resources/report");

        AutosysConverter.CONFIG.withGenerateConfig().withWorkflows(true).withSchedules(true).withLocks(true).withCyclicOrders(false);
        AutosysConverter.CONFIG.withMockConfig().withPlatform(Platform.UNIX).withAgentName("my_agent_name").withScript("$HOME/MockScript.sh");
        AutosysConverter.CONFIG.withScheduleConfig().withDefaultCalendarName("AnyDays").withPlanOrders(true).withSubmitOrders(true);
        AutosysConverter.CONFIG.withJobConfig().withGraceTimeout(15).withParallelism(1).withFailOnErrWritten(true).withScriptNewLine("\n");
        AutosysConverter.CONFIG.withSubFolderConfig().withMapping("aapg=2; ebzc=0; wmad=0; abcd=0").withSeparator("_");

        AutosysConverter.convert(new XMLJobParser(), inputDir, outputDir, reportDir);
    }
}
