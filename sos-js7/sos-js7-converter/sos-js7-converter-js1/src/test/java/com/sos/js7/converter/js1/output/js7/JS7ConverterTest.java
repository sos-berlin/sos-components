package com.sos.js7.converter.js1.output.js7;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.converter.commons.JS7ConverterMain;
import com.sos.js7.converter.commons.config.JS7ConverterConfig.AgentConfig;
import com.sos.js7.converter.commons.config.JS7ConverterConfig.GenerateConfig;
import com.sos.js7.converter.commons.config.JS7ConverterConfig.JobConfig;
import com.sos.js7.converter.commons.config.JS7ConverterConfig.MockConfig;
import com.sos.js7.converter.commons.config.JS7ConverterConfig.ParserConfig;
import com.sos.js7.converter.commons.config.JS7ConverterConfig.ScheduleConfig;
import com.sos.js7.converter.commons.config.JS7ConverterConfig.WorkflowConfig;

public class JS7ConverterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7ConverterTest.class);

    @SuppressWarnings("unused")
    @Ignore
    @Test
    public void test() throws Exception {
        // - Configuration ---------------------------------
        // JS7Converter.CONFIG.getSubFolderConfig().withMappings("aapg=2; ebzc=0; wmad=0; abcd=0").withSeparator("_");

        // # Generate
        GenerateConfig generate = JS7Converter.CONFIG.getGenerateConfig();
        // generate.withWorkflows(true).withAgents(true).withSchedules(true).withLocks(true).withCyclicOrders(false);

        // # Parser
        ParserConfig parser = JS7Converter.CONFIG.getParserConfig();
        parser.withExcludedDirectoryNames(".sos-templates;.svn;.configuration;").withExcludedDirectoryPaths("sos/");

        // # Workflow
        WorkflowConfig workflow = JS7Converter.CONFIG.getWorkflowConfig();

        // # Workflow Job
        JobConfig job = JS7Converter.CONFIG.getJobConfig();
        job.withForcedV1Compatible(true);
        // job.withForcedJitlLogLevel("DEBUG").withForcedFailOnErrWritten(true);
        // job.withNotificationMailDefault("to@localhost.com", "cc@localhost.com", "bcc@localhost.com");
        // job.withScriptNewLine("\n").withForcedV1Compatible(true);

        // # Agent
        AgentConfig agent = JS7Converter.CONFIG.getAgentConfig();
        // agent.withDefaultControllerId("js7");
        // agent.withMappings("agent=src/test/resources/agent_cluster.json");
        // agent.withForcedAgent("src/test/resources/agent_cluster.json");
        // agent.withForcedAgent(
        // "{\"standaloneAgent\":{\"agentName\":\"forced_agent_name\", \"url\":\"http://forced_agent:6666\",\"controllerId\":\"forced_agent_controller_id\"}}");
        // agent.withDefaultAgent(
        // "{\"standaloneAgent\":{\"agentName\":\"default_agent_name\",\"url\":\"http://default_agent:6666\",\"controllerId\":\"default_agent_controller_id\"}}");
        agent.withDefaultAgent("{\"standaloneAgent\":{\"agentName\":\"agent_name\"}}");

        // # Mock
        MockConfig mock = JS7Converter.CONFIG.getMockConfig();
        // mock.withUnixScript("$HOME/MockScript.sh").withWindowsScript("echo 123").withJitlJobsMockLevel("ERROR");

        // # Schedule
        ScheduleConfig schedule = JS7Converter.CONFIG.getScheduleConfig();
        // schedule.withDefaultWorkingDayCalendarName("AnyDays").withDefaultNonWorkingDayCalendarName("NonWorking");
        schedule.withPlanOrders(true).withSubmitOrders(true);

        // - Execute ---------------------------------
        Path baseDir = Paths.get("src/test/resources");
        Path input = baseDir.resolve("input");
        Path outputDir = baseDir.resolve("output/live");
        Path reportDir = outputDir.getParent().resolve("report");
        Path archive = outputDir.getParent().resolve("js7_converted.zip");

        JS7Converter.convert(input, outputDir, reportDir);
        JS7ConverterMain.createArchiveFile(outputDir, archive);
    }

    @Ignore
    @Test
    public void testUniqueName() throws Exception {
        Pattern p = Pattern.compile("(.*)(-copy)([0-9]+)$");

        String a = "xxx-copy10000";
        Matcher m = p.matcher(a);

        if (m.find()) {
            if (m.groupCount() == 3) {
                String output = m.group(1) + m.group(2) + (Integer.parseInt(m.group(3)) + 1); // ;m.replaceFirst("$1$2" + (Integer.parseInt(m.group(3)) + 1));
                LOGGER.info("[FOUND][groupCount=3]" + output);
            } else {
                LOGGER.info("[FOUND]groupCount=" + m.groupCount());
            }
        } else {
            LOGGER.info("[NOT FOUND]");
        }
    }

}
