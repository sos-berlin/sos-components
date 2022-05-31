package com.sos.js7.converter.js1.output;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.js7.converter.commons.JS7ConverterConfig.Platform;

public class JS12JS7ConverterTest {

    @Ignore
    @Test
    public void test() throws IOException {
        Path input = Paths.get("src/test/resources/input");
        Path outputDir = Paths.get("src/test/resources/output");
        Path reportDir = Paths.get("src/test/resources/report");

        JS12JS7Converter.CONFIG.getGenerateConfig().withWorkflows(true).withSchedules(true).withLocks(true).withCyclicOrders(false);
        JS12JS7Converter.CONFIG.getParserConfig().withExcludedDirectoryNames(".sos-templates , .svn");
        JS12JS7Converter.CONFIG.getAgentConfig().withForcedPlatform(Platform.WINDOWS).withDefaultName("my_agent_name").withMapping(
                "abcd=agent;xyz=agent_cluster");
        JS12JS7Converter.CONFIG.getMockConfig().withScript("$HOME/MockScript.sh");
        JS12JS7Converter.CONFIG.getScheduleConfig().withDefaultCalendarName("AnyDays").withPlanOrders(true).withSubmitOrders(true);
        JS12JS7Converter.CONFIG.getJobConfig().withForcedFailOnErrWritten(true).withScriptNewLine("\n");
        JS12JS7Converter.CONFIG.getSubFolderConfig().withMapping("aapg=2; ebzc=0; wmad=0; abcd=0").withSeparator("_");

        JS12JS7Converter.convert(input, outputDir, reportDir);
    }
}
