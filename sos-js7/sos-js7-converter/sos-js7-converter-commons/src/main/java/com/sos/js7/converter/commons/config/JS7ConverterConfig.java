package com.sos.js7.converter.commons.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import com.sos.js7.converter.commons.config.items.AgentConfig;
import com.sos.js7.converter.commons.config.items.CalendarConfig;
import com.sos.js7.converter.commons.config.items.GenerateConfig;
import com.sos.js7.converter.commons.config.items.JobConfig;
import com.sos.js7.converter.commons.config.items.MockConfig;
import com.sos.js7.converter.commons.config.items.ParserConfig;
import com.sos.js7.converter.commons.config.items.ScheduleConfig;
import com.sos.js7.converter.commons.config.items.SubFolderConfig;
import com.sos.js7.converter.commons.config.items.WorkflowConfig;

public class JS7ConverterConfig {

    public enum Platform {
        UNIX, WINDOWS
    }

    private final GenerateConfig generateConfig;
    private final ParserConfig parserConfig;
    private final WorkflowConfig workflowConfig;
    private final JobConfig jobConfig;
    private final AgentConfig agentConfig;
    private final MockConfig mockConfig;
    private final CalendarConfig calendarConfig;
    private final ScheduleConfig scheduleConfig;
    private final SubFolderConfig subFolderConfig;

    public JS7ConverterConfig() {
        generateConfig = new GenerateConfig();
        parserConfig = new ParserConfig();
        workflowConfig = new WorkflowConfig();
        jobConfig = new JobConfig();
        agentConfig = new AgentConfig();
        mockConfig = new MockConfig();
        calendarConfig = new CalendarConfig();
        scheduleConfig = new ScheduleConfig();
        subFolderConfig = new SubFolderConfig();
    }

    // TODO reflection
    public Properties parse(Path propertiesFile) throws Exception {
        Properties p = new Properties();
        if (Files.exists(propertiesFile)) {
            try (InputStream input = new FileInputStream(propertiesFile.toFile())) {
                p.load(input);
                // GENERATE
                generateConfig.parse(p);
                // PARSER
                parserConfig.parse(p);
                // WORKFLOW
                workflowConfig.parse(p);
                // JOB
                jobConfig.parse(p);
                // AGENT
                agentConfig.parse(p);
                // MOCK
                mockConfig.parse(p);
                // CALENDAR
                calendarConfig.parse(p);
                // SCHEDULE
                scheduleConfig.parse(p);
                // SUBFOLDER - TODO autosys only
                subFolderConfig.parse(p);

            } catch (Throwable e) {
                throw e;
            }
        } else {
            throw new Exception("[" + propertiesFile + "]propertiesFile not found");
        }
        return p;
    }

    public GenerateConfig getGenerateConfig() {
        return generateConfig;
    }

    public ParserConfig getParserConfig() {
        return parserConfig;
    }

    public WorkflowConfig getWorkflowConfig() {
        return workflowConfig;
    }

    public JobConfig getJobConfig() {
        return jobConfig;
    }

    public AgentConfig getAgentConfig() {
        return agentConfig;
    }

    public MockConfig getMockConfig() {
        return mockConfig;
    }

    public CalendarConfig getCalendarConfig() {
        return calendarConfig;
    }

    public ScheduleConfig getScheduleConfig() {
        return scheduleConfig;
    }

    public SubFolderConfig getSubFolderConfig() {
        return subFolderConfig;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(generateConfig.toString());
        sb.append(",").append(workflowConfig.toString());
        if (!parserConfig.isEmpty()) {
            sb.append(",").append(parserConfig.toString());
        }
        if (!jobConfig.isEmpty()) {
            sb.append(",").append(jobConfig.toString());
        }
        if (!agentConfig.isEmpty()) {
            sb.append(",").append(agentConfig.toString());
        }
        if (!mockConfig.isEmpty()) {
            sb.append(",").append(mockConfig.toString());
        }
        if (!calendarConfig.isEmpty()) {
            sb.append(",").append(calendarConfig.toString());
        }
        if (!scheduleConfig.isEmpty()) {
            sb.append(",").append(scheduleConfig.toString());
        }
        if (!subFolderConfig.isEmpty()) {
            sb.append(",").append(subFolderConfig.toString());
        }
        return sb.toString();
    }

}
