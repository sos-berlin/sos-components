package com.sos.js7.converter.commons;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.util.SOSString;

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
    private final ScheduleConfig scheduleConfig;
    private final SubFolderConfig subFolderConfig;

    public JS7ConverterConfig() {
        generateConfig = this.new GenerateConfig();
        parserConfig = this.new ParserConfig();
        jobConfig = this.new JobConfig();
        workflowConfig = new WorkflowConfig();
        agentConfig = this.new AgentConfig();
        mockConfig = this.new MockConfig();
        scheduleConfig = this.new ScheduleConfig();
        subFolderConfig = this.new SubFolderConfig();
    }

    // TODO reflection
    public Properties parse(Path propertiesFile) throws Exception {
        Properties p = new Properties();
        if (Files.exists(propertiesFile)) {
            try (InputStream input = new FileInputStream(propertiesFile.toFile())) {
                p.load(input);

                p.entrySet().forEach(e -> {
                    String val = e.getValue().toString().trim();
                    switch (e.getKey().toString()) {
                    // GENERATE
                    case "generateConfig.workflows":
                        generateConfig.workflows = Boolean.parseBoolean(val);
                        break;
                    case "generateConfig.locks":
                        generateConfig.locks = Boolean.parseBoolean(val);
                        break;
                    case "generateConfig.schedules":
                        generateConfig.schedules = Boolean.parseBoolean(val);
                        break;
                    case "generateConfig.calendars":
                        generateConfig.calendars = Boolean.parseBoolean(val);
                        break;
                    case "generateConfig.cyclicOrders":
                        generateConfig.cyclicOrders = Boolean.parseBoolean(val);
                        break;
                    // PARSER:
                    case "parserConfig.excludedDirectoryNames":
                        parserConfig.withExcludedDirectoryNames(val);
                        break;
                    // WORKFLOW
                    case "workflowConfig.defaultTimeZone":
                        workflowConfig.defaultTimeZone = val;
                        break;
                    // JOB
                    case "jobConfig.scriptNewLine":
                        jobConfig.scriptNewLine = val;
                        break;
                    case "jobConfig.forcedGraceTimeout":
                        jobConfig.forcedGraceTimeout = Integer.parseInt(val);
                        break;
                    case "jobConfig.forcedParallelism":
                        jobConfig.forcedParallelism = Integer.parseInt(val);
                        break;
                    case "jobConfig.forcedFailOnErrWritten":
                        jobConfig.forcedFailOnErrWritten = Boolean.parseBoolean(val);
                        break;
                    // AGENT
                    case "agentConfig.mapping":
                        agentConfig.withMapping(val);
                        break;
                    case "agentConfig.forcedPlatform":
                        agentConfig.forcedPlatform = Platform.valueOf(val);
                        break;
                    case "agentConfig.forcedName":
                        agentConfig.forcedName = val;
                        break;
                    case "agentConfig.defaultName":
                        agentConfig.defaultName = val;
                        break;
                    // MOCK
                    case "mockConfig.script":
                        mockConfig.script = val;
                        break;
                    // SCHEDULE
                    case "scheduleConfig.forcedWorkingCalendarName":
                        scheduleConfig.forcedWorkingCalendarName = val;
                        break;
                    case "scheduleConfig.forcedNonWorkingCalendarName":
                        scheduleConfig.forcedNonWorkingCalendarName = val;
                        break;
                    case "scheduleConfig.defaultWorkingCalendarName":
                        scheduleConfig.defaultWorkingCalendarName = val;
                        break;
                    case "scheduleConfig.defaultNonWorkingCalendarName":
                        scheduleConfig.defaultNonWorkingCalendarName = val;
                        break;
                    case "scheduleConfig.defaultTimeZone":
                        scheduleConfig.defaultTimeZone = val;
                        break;
                    case "scheduleConfig.planOrders":
                        scheduleConfig.planOrders = Boolean.parseBoolean(val);
                        break;
                    case "scheduleConfig.submitOrders":
                        scheduleConfig.submitOrders = Boolean.parseBoolean(val);
                        break;
                    // SubFolders
                    case "subFolderConfig.mapping":
                        subFolderConfig.withMapping(val);
                        break;
                    case "subFolderConfig.separator":
                        subFolderConfig.separator = val;
                        break;
                    }
                });

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

    public ScheduleConfig getScheduleConfig() {
        return scheduleConfig;
    }

    public SubFolderConfig getSubFolderConfig() {
        return subFolderConfig;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SOSString.toString(generateConfig));
        sb.append(",").append(SOSString.toString(workflowConfig));
        if (!parserConfig.isEmpty()) {
            sb.append(",").append(SOSString.toString(parserConfig));
        }
        if (!jobConfig.isEmpty()) {
            sb.append(",").append(SOSString.toString(jobConfig));
        }
        if (!agentConfig.isEmpty()) {
            sb.append(",").append(SOSString.toString(agentConfig));
        }
        if (!mockConfig.isEmpty()) {
            sb.append(",").append(SOSString.toString(mockConfig));
        }
        if (!scheduleConfig.isEmpty()) {
            sb.append(",").append(SOSString.toString(scheduleConfig));
        }
        if (!subFolderConfig.isEmpty()) {
            sb.append(",").append(SOSString.toString(subFolderConfig));
        }
        return sb.toString();
    }

    public class ParserConfig {

        private Set<String> excludedDirectoryNames;

        public ParserConfig withExcludedDirectoryNames(String val) {
            if (!SOSString.isEmpty(val)) {
                excludedDirectoryNames = Arrays.stream(val.split(",")).map(e -> e.trim()).collect(Collectors.toSet());
            }
            return this;
        }

        public boolean isEmpty() {
            return excludedDirectoryNames == null || excludedDirectoryNames.size() == 0;
        }

        public Set<String> getExcludedDirectoryNames() {
            return excludedDirectoryNames;
        }
    }

    public class GenerateConfig {

        private boolean workflows = true;
        private boolean locks;
        private boolean schedules;
        private boolean calendars;
        private boolean cyclicOrders;

        public GenerateConfig withWorkflows(boolean val) {
            this.workflows = val;
            return this;
        }

        public GenerateConfig withLocks(boolean val) {
            this.locks = val;
            return this;
        }

        public GenerateConfig withSchedules(boolean val) {
            this.schedules = val;
            return this;
        }

        public GenerateConfig withCalendars(boolean val) {
            this.calendars = val;
            return this;
        }

        public GenerateConfig withCyclicOrders(boolean val) {
            this.cyclicOrders = val;
            return this;
        }

        public boolean getWorkflows() {
            return workflows;
        }

        public boolean getLocks() {
            return locks;
        }

        public boolean getSchedules() {
            return schedules;
        }

        public boolean getCalendars() {
            return calendars;
        }

        public boolean getCyclicOrders() {
            return cyclicOrders;
        }

    }

    public class WorkflowConfig {

        private String defaultTimeZone = "Etc/UTC";

        public WorkflowConfig withDefaultTimeZone(String timeZone) {
            this.defaultTimeZone = timeZone;
            return this;
        }

        public String getDefaultTimeZone() {
            return defaultTimeZone;
        }
    }

    public class JobConfig {

        private String scriptNewLine = "\n";
        private Integer forcedGraceTimeout;
        private Integer forcedParallelism;
        private Boolean forcedFailOnErrWritten;

        public JobConfig withScriptNewLine(String newLine) {
            this.scriptNewLine = newLine;
            return this;
        }

        public JobConfig withForcedGraceTimeout(Integer graceTimeout) {
            this.forcedGraceTimeout = graceTimeout;
            return this;
        }

        public JobConfig withForcedParallelism(Integer parallelism) {
            this.forcedParallelism = parallelism;
            return this;
        }

        public JobConfig withForcedFailOnErrWritten(Boolean failOnErrWritten) {
            this.forcedFailOnErrWritten = failOnErrWritten;
            return this;
        }

        public String getScriptNewLine() {
            return scriptNewLine;
        }

        public Integer getForcedGraceTimeout() {
            return forcedGraceTimeout;
        }

        public Integer getForcedParallelism() {
            return forcedParallelism;
        }

        public Boolean getForcedFailOnErrWritten() {
            return forcedFailOnErrWritten;
        }

        public boolean isEmpty() {
            return forcedGraceTimeout == null && forcedParallelism == null && forcedFailOnErrWritten == null;
        }
    }

    public class AgentConfig {

        private Map<String, String> mapping;
        private Platform forcedPlatform;
        private String forcedName; // use this instead of evaluated agent name
        private String defaultName;// when agent can't be evaluated

        public AgentConfig withForcedPlatform(Platform platform) {
            this.forcedPlatform = platform;
            return this;
        }

        public AgentConfig withForcedName(String agentName) {
            this.forcedName = agentName;
            return this;
        }

        public AgentConfig withDefaultName(String agentName) {
            this.defaultName = agentName;
            return this;
        }

        /** Agent mapping<br/>
         * Example 1<br>
         * - input map: my_agent_1 = agent; my_agent_2 = agent; my_agent_3 = cluster<br/>
         **/
        public AgentConfig withMapping(String mapping) {
            Map<String, String> map = new HashMap<>();
            if (mapping != null) {
                // map and remove duplicates
                map = Stream.of(mapping.trim().split(";")).map(e -> e.split("=")).filter(e -> e.length == 2).collect(Collectors.toMap(arr -> arr[0]
                        .trim(), arr -> arr[1].trim(), (oldValue, newValue) -> oldValue));
            }
            return withMapping(map);
        }

        public AgentConfig withMapping(Map<String, String> map) {
            this.mapping = map;
            return this;
        }

        public String getForcedName() {
            return forcedName;
        }

        public String getDefaultName() {
            return defaultName;
        }

        public Map<String, String> getMapping() {
            if (mapping == null) {
                mapping = new HashMap<>();
            }
            return mapping;
        }

        public Platform getForcedPlatform() {
            return forcedPlatform;
        }

        public boolean isEmpty() {
            return (mapping == null || mapping.size() == 0) && forcedPlatform == null && forcedName == null && defaultName == null;
        }

    }

    public class MockConfig {

        private String script;

        public MockConfig withScript(String script) {
            this.script = script;
            return this;
        }

        public String getScript() {
            return script;
        }

        public boolean isEmpty() {
            return script == null;
        }
    }

    public class ScheduleConfig {

        private String forcedWorkingCalendarName;
        private String forcedNonWorkingCalendarName;
        private String defaultWorkingCalendarName;
        private String defaultNonWorkingCalendarName;

        private String defaultTimeZone = "Etc/UTC";
        private Boolean planOrders;
        private Boolean submitOrders;

        public ScheduleConfig withForcedWorkingCalendarName(String calendarName) {
            this.forcedWorkingCalendarName = calendarName;
            return this;
        }

        public ScheduleConfig withForcedNonWorkingCalendarName(String calendarName) {
            this.forcedNonWorkingCalendarName = calendarName;
            return this;
        }

        public ScheduleConfig withDefaultWorkingCalendarName(String calendarName) {
            this.defaultWorkingCalendarName = calendarName;
            return this;
        }

        public ScheduleConfig withDefaultNonWorkingCalendarName(String calendarName) {
            this.defaultNonWorkingCalendarName = calendarName;
            return this;
        }

        public ScheduleConfig withDefaultTimeZone(String timeZone) {
            this.defaultTimeZone = timeZone;
            return this;
        }

        public ScheduleConfig withPlanOrders(boolean planOrders) {
            this.planOrders = planOrders;
            return this;
        }

        public ScheduleConfig withSubmitOrders(boolean submitOrders) {
            this.submitOrders = submitOrders;
            return this;
        }

        public String getForcedWorkingCalendarName() {
            return forcedWorkingCalendarName;
        }

        public String getForcedNonWorkingCalendarName() {
            return forcedNonWorkingCalendarName;
        }

        public String getDefaultWorkingCalendarName() {
            return defaultWorkingCalendarName;
        }

        public String getDefaultNonWorkingCalendarName() {
            return defaultNonWorkingCalendarName;
        }

        public String getDefaultTimeZone() {
            return defaultTimeZone;
        }

        public boolean planOrders() {
            return planOrders == null ? false : planOrders;
        }

        public boolean submitOrders() {
            return submitOrders == null ? false : submitOrders;
        }

        public boolean isEmpty() {
            return defaultWorkingCalendarName == null && defaultNonWorkingCalendarName == null && planOrders == null && submitOrders == null;
        }
    }

    public class SubFolderConfig {

        private Map<String, Integer> mapping;
        private String separator = "_";

        /** Sub Folder mapping - extract job names parts(parts are separated by subFolderSeparator) to create a sub folders<br/>
         * Example 1<br>
         * - input map: aapg = 2; ebzc = 0; wmad = 0<br/>
         * - input jil job name (application aapg): xxx_yyy_zzz_my_job<br/>
         * - output: zzz/my_job ("zzz" has the index 2 if the job name is separated by "_")
         * 
         * @param map
         * @return */
        public SubFolderConfig withMapping(String mapping) {
            Map<String, Integer> map = new HashMap<>();
            if (mapping != null) {
                // map and remove duplicates
                map = Stream.of(mapping.trim().split(";")).map(e -> e.split("=")).filter(e -> e.length == 2).collect(Collectors.toMap(arr -> arr[0]
                        .trim(), arr -> Integer.parseInt(arr[1].trim()), (oldValue, newValue) -> oldValue));
            }
            return withMapping(map);
        }

        public SubFolderConfig withMapping(Map<String, Integer> map) {
            this.mapping = map;
            return this;
        }

        public SubFolderConfig withSeparator(String separator) {
            this.separator = separator;
            return this;
        }

        public Map<String, Integer> getMapping() {
            if (mapping == null) {
                mapping = new HashMap<>();
            }
            return mapping;
        }

        public String getSeparator() {
            return separator;
        }

        public boolean isEmpty() {
            return mapping == null || mapping.size() == 0;
        }
    }

}
