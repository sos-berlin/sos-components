package com.sos.js7.converter.js1.output.js7;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.js7.converter.commons.JS7ConverterConfig.AgentConfig;
import com.sos.js7.converter.commons.JS7ConverterConfig.GenerateConfig;
import com.sos.js7.converter.commons.JS7ConverterConfig.JobConfig;
import com.sos.js7.converter.commons.JS7ConverterConfig.MockConfig;
import com.sos.js7.converter.commons.JS7ConverterConfig.ParserConfig;
import com.sos.js7.converter.commons.JS7ConverterConfig.Platform;
import com.sos.js7.converter.commons.JS7ConverterConfig.ScheduleConfig;
import com.sos.js7.converter.commons.JS7ConverterMain;

public class JS7ConverterTest {

    @Ignore
    @Test
    public void test() throws IOException {
        // JS7Converter.CONFIG.getSubFolderConfig().withMappings("aapg=2; ebzc=0; wmad=0; abcd=0").withSeparator("_");

        GenerateConfig generate = JS7Converter.CONFIG.getGenerateConfig();
        generate.withWorkflows(true).withAgents(true).withSchedules(true).withLocks(true).withCyclicOrders(false);

        ParserConfig parser = JS7Converter.CONFIG.getParserConfig();
        parser.withExcludedDirectoryNames(".sos-templates;.svn;.configuration;").withExcludedDirectoryPaths("sos/;xxx/");

        AgentConfig agent = JS7Converter.CONFIG.getAgentConfig();
        agent.withForcedPlatform(Platform.WINDOWS).withDefaultAgent("agent_name").withMappings("abcd=agent;xyz=agent_cluster");

        MockConfig mock = JS7Converter.CONFIG.getMockConfig();
        mock.withUnixScript("$HOME/MockScript.sh").withWindowsScript("echo 123").withJitlJobsMockLevel("ERROR");

        ScheduleConfig schedule = JS7Converter.CONFIG.getScheduleConfig();
        schedule.withDefaultWorkingDayCalendarName("AnyDays").withDefaultNonWorkingDayCalendarName("NonWorking");
        schedule.withPlanOrders(true).withSubmitOrders(true);

        JobConfig job = JS7Converter.CONFIG.getJobConfig();
        // job.withForcedJitlLogLevel("DEBUG").withForcedFailOnErrWritten(true);
        job.withNotificationMailDefault("to@localhost.com", "cc@localhost.com", "bcc@localhost.com");
        job.withScriptNewLine("\n").withForcedV1Compatible(true);

        Path input = Paths.get("src/test/resources/input");
        Path outputDir = Paths.get("src/test/resources/output/live");
        Path reportDir = outputDir.getParent().resolve("report");
        Path archive = outputDir.getParent().resolve("js7_converted.tar.gz");

        JS7Converter.convert(input, outputDir, reportDir);
        JS7ConverterMain.createArchiveFile(outputDir, archive);
    }
}
