package com.sos.js7.converter.js1.output.js7;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.js7.converter.commons.JS7ConverterConfig.Platform;
import com.sos.js7.converter.commons.JS7ConverterMain;

public class JS7ConverterTest {

    @Ignore
    @Test
    public void test() throws IOException {
        Path input = Paths.get("src/test/resources/input");
        Path outputDir = Paths.get("src/test/resources/output");
        Path reportDir = Paths.get("src/test/resources/report");
        Path archive = Paths.get("src/test/resources/js7_converted.tar.gz");

        JS7Converter.CONFIG.getGenerateConfig().withWorkflows(true).withSchedules(true).withLocks(true).withCyclicOrders(false);
        JS7Converter.CONFIG.getParserConfig().withExcludedDirectoryNames(".sos-templates;.svn;.configuration;").withExcludedDirectoryPaths(
                "sos/;xxx/");
        JS7Converter.CONFIG.getAgentConfig().withForcedPlatform(Platform.UNIX).withDefaultAgent("my_agent_name").withMappings(
                "abcd=agent;xyz=agent_cluster");
        JS7Converter.CONFIG.getMockConfig().withUnixScript("$HOME/MockScript.sh").withWindowsScript("echo 123").withJitlJobsMockLevel("ERROR");
        JS7Converter.CONFIG.getScheduleConfig().withDefaultWorkingCalendarName("AnyDays").withDefaultNonWorkingCalendarName(null).withPlanOrders(true)
                .withSubmitOrders(true);
        JS7Converter.CONFIG.getJobConfig().withForcedFailOnErrWritten(true).withScriptNewLine("\n");
        JS7Converter.CONFIG.getSubFolderConfig().withMappings("aapg=2; ebzc=0; wmad=0; abcd=0").withSeparator("_");

        JS7Converter.convert(input, outputDir, reportDir);
        JS7ConverterMain.createArchiveFile(outputDir, archive);
    }
}
