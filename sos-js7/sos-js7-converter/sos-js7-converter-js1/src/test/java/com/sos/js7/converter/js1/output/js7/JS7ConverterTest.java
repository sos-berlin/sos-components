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
        JS7Converter.CONFIG.getGenerateConfig().withWorkflows(true).withSchedules(true).withLocks(true).withCyclicOrders(false);
        JS7Converter.CONFIG.getParserConfig().withExcludedDirectoryNames(".sos-templates;.svn;.configuration;").withExcludedDirectoryPaths(
                "sos/;xxx/");
        JS7Converter.CONFIG.getAgentConfig().withForcedPlatform(Platform.WINDOWS).withDefaultAgent("agent_name").withMappings(
                "abcd=agent;xyz=agent_cluster");
        JS7Converter.CONFIG.getMockConfig().withUnixScript("$HOME/MockScript.sh").withWindowsScript("echo 123").withJitlJobsMockLevel("ERROR");
        JS7Converter.CONFIG.getScheduleConfig().withDefaultWorkingDayCalendarName("AnyDays").withDefaultNonWorkingDayCalendarName("NonWorking")
                .withPlanOrders(true).withSubmitOrders(true);
        JS7Converter.CONFIG.getJobConfig().withJitlLogLevel("DEBUG").withForcedFailOnErrWritten(true).withNotificationMailDefaultTo(
                "to@localhost.com").withNotificationMailDefaultCc("cc@localhost.com").withNotificationMailDefaultBcc("bcc@localhost.com")
                .withScriptNewLine("\n");
        JS7Converter.CONFIG.getSubFolderConfig().withMappings("aapg=2; ebzc=0; wmad=0; abcd=0").withSeparator("_");

        Path input = Paths.get("src/test/resources/input");
        Path outputDir = Paths.get("src/test/resources/output/live");
        Path reportDir = outputDir.getParent().resolve("report");
        Path archive = outputDir.getParent().resolve("js7_converted.tar.gz");

        JS7Converter.convert(input, outputDir, reportDir);
        JS7ConverterMain.createArchiveFile(outputDir, archive);
    }
}
