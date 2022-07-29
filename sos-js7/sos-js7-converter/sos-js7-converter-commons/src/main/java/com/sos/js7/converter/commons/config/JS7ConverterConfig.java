package com.sos.js7.converter.commons.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.config.json.JS7Agent;
import com.sos.js7.converter.commons.report.ConfigReport;

public class JS7ConverterConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7ConverterConfig.class);

    private static final String LIST_VALUE_DELIMITER = ";";

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

    private Path propertiesFile;

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
        this.propertiesFile = propertiesFile;
        Properties p = new Properties();
        if (Files.exists(propertiesFile)) {
            try (InputStream input = new FileInputStream(propertiesFile.toFile())) {
                p.load(input);

                p.entrySet().forEach(e -> {
                    String key = e.getKey().toString().trim();
                    String val = e.getValue().toString().trim();
                    try {
                        switch (key) {
                        // GENERATE
                        case "generateConfig.workflows":
                            generateConfig.workflows = Boolean.parseBoolean(val);
                            break;
                        case "generateConfig.agents":
                            generateConfig.agents = Boolean.parseBoolean(val);
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
                        case "parserConfig.excludedDirectoryPaths":
                            parserConfig.withExcludedDirectoryPaths(val);
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
                        case "jobConfig.forcedV1Compatible":
                            jobConfig.forcedV1Compatible = Boolean.parseBoolean(val);
                            break;
                        case "jobConfig.jitl.forcedLogLevel":
                            jobConfig.forcedJitlLogLevel = val;
                            break;
                        case "jobConfig.notification.mail.defaultTo":
                            jobConfig.notificationMailDefaultTo = val;
                            break;
                        case "jobConfig.notification.mail.defaultCc":
                            jobConfig.notificationMailDefaultCc = val;
                            break;
                        case "jobConfig.notification.mail.defaultBcc":
                            jobConfig.notificationMailDefaultBcc = val;
                            break;
                        // AGENT
                        case "agentConfig.mappings":
                            agentConfig.withMappings(val);
                            break;
                        case "agentConfig.forcedControllerId":
                            agentConfig.withForcedControllerId(val);
                            break;
                        case "agentConfig.defaultControllerId":
                            agentConfig.withDefaultControllerId(val);
                            break;
                        case "agentConfig.forcedAgent":
                            agentConfig.withForcedAgent(val);
                            break;
                        case "agentConfig.defaultAgent":
                            agentConfig.withDefaultAgent(val);
                            break;
                        // MOCK
                        case "mockConfig.shell.windowsScript":
                            mockConfig.windowsScript = val;
                            break;
                        case "mockConfig.shell.unixScript":
                            mockConfig.unixScript = val;
                            break;
                        case "mockConfig.jitl.mockLevel":
                            mockConfig.jitlJobsMockLevel = val;
                            break;
                        // SCHEDULE
                        case "scheduleConfig.forcedWorkingDayCalendarName":
                            scheduleConfig.forcedWorkingDayCalendarName = val;
                            break;
                        case "scheduleConfig.forcedNonWorkingDayCalendarName":
                            scheduleConfig.forcedNonWorkingDayCalendarName = val;
                            break;
                        case "scheduleConfig.defaultWorkingDayCalendarName":
                            scheduleConfig.defaultWorkingDayCalendarName = val;
                            break;
                        case "scheduleConfig.defaultNonWorkingDayCalendarName":
                            scheduleConfig.defaultNonWorkingDayCalendarName = val;
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
                        case "subFolderConfig.mappings":
                            subFolderConfig.withMappings(val);
                            break;
                        case "subFolderConfig.separator":
                            subFolderConfig.separator = val;
                            break;
                        }
                    } catch (Throwable ee) {
                        String msg = getErrorMessage(key, val);
                        LOGGER.error(msg);
                        ConfigReport.INSTANCE.addErrorRecord(null, msg, ee);
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

    private JS7Agent readAgentJson(final String val) throws Exception {
        if (val == null) {
            return null;
        }
        String value = val;
        if (value.toLowerCase().endsWith(".json")) {
            Path jsonFile = Paths.get(val);
            if (!jsonFile.isAbsolute()) {
                if (propertiesFile != null && propertiesFile.getParent() != null) {
                    jsonFile = propertiesFile.getParent().resolve(val);
                }
            }
            if (!Files.exists(jsonFile)) {
                throw new Exception("[" + jsonFile.toAbsolutePath() + "]file not found");
            }
            value = SOSPath.readFile(jsonFile, StandardCharsets.UTF_8);
        }
        return JS7ConverterHelper.JSON_OM.readValue(value, JS7Agent.class);

    }

    private String getErrorMessage(String name, String value) {
        return String.format("[cannot parse config][%s]%s", name, value);
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
        private Map<Integer, Set<String>> excludedDirectoryPaths; // level, paths

        public ParserConfig withExcludedDirectoryNames(String val) {
            if (!SOSString.isEmpty(val)) {
                excludedDirectoryNames = Arrays.stream(val.split(LIST_VALUE_DELIMITER)).map(e -> e.trim()).collect(Collectors.toSet());
            }
            return this;
        }

        public ParserConfig withExcludedDirectoryPaths(String val) {
            if (!SOSString.isEmpty(val)) {
                List<String> list = Arrays.stream(val.split(LIST_VALUE_DELIMITER)).map(e -> JS7ConverterHelper.normalizeDirectoryPath(e)).distinct()
                        .collect(Collectors.toList());
                excludedDirectoryPaths = new HashMap<>();
                for (String p : list) {
                    int arrLen = p.split("/").length;
                    if (arrLen > 0) {
                        Integer level = Integer.valueOf(arrLen - 1); // /sos/xxx/ <- level 2
                        Set<String> set = excludedDirectoryPaths.get(level);
                        if (set == null) {
                            set = new HashSet<>();
                        }
                        set.add(p);
                        excludedDirectoryPaths.put(level, set);
                    }
                }
            }
            return this;
        }

        public boolean hasExcludedDirectoryNames() {
            return excludedDirectoryNames != null && excludedDirectoryNames.size() > 0;
        }

        public boolean hasExcludedDirectoryPaths() {
            return excludedDirectoryPaths != null && excludedDirectoryPaths.size() > 0;
        }

        public boolean isEmpty() {
            return hasExcludedDirectoryNames() || hasExcludedDirectoryPaths();
        }

        public Set<String> getExcludedDirectoryNames() {
            return excludedDirectoryNames;
        }

        public Map<Integer, Set<String>> getExcludedDirectoryPaths() {
            return excludedDirectoryPaths;
        }

    }

    public class GenerateConfig {

        private boolean workflows = true;
        private boolean agents = true;
        private boolean locks = true;
        private boolean schedules = true;
        private boolean calendars = true;
        private boolean cyclicOrders;

        public GenerateConfig withWorkflows(boolean val) {
            this.workflows = val;
            return this;
        }

        public GenerateConfig withAgents(boolean val) {
            this.agents = val;
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

        public boolean getAgents() {
            return agents;
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

        public WorkflowConfig withDefaultTimeZone(String val) {
            this.defaultTimeZone = val;
            return this;
        }

        public String getDefaultTimeZone() {
            return defaultTimeZone;
        }
    }

    public class JobConfig {

        private String scriptNewLine = "\n";
        private String forcedJitlLogLevel;
        private Integer forcedGraceTimeout;
        private Integer forcedParallelism;
        private Boolean forcedFailOnErrWritten;
        private Boolean forcedV1Compatible;

        private String notificationMailDefaultTo;
        private String notificationMailDefaultCc;
        private String notificationMailDefaultBcc;

        public JobConfig withScriptNewLine(String val) {
            this.scriptNewLine = val;
            return this;
        }

        public JobConfig withForcedJitlLogLevel(String val) {
            this.forcedJitlLogLevel = val;
            return this;
        }

        public JobConfig withForcedGraceTimeout(Integer val) {
            this.forcedGraceTimeout = val;
            return this;
        }

        public JobConfig withForcedParallelism(Integer val) {
            this.forcedParallelism = val;
            return this;
        }

        public JobConfig withForcedFailOnErrWritten(Boolean val) {
            this.forcedFailOnErrWritten = val;
            return this;
        }

        public JobConfig withForcedV1Compatible(Boolean val) {
            this.forcedV1Compatible = val;
            return this;
        }

        public JobConfig withNotificationMailDefault(String to, String cc, String bcc) {
            this.notificationMailDefaultTo = to;
            this.notificationMailDefaultCc = cc;
            this.notificationMailDefaultBcc = bcc;
            return this;
        }

        public String getScriptNewLine() {
            return scriptNewLine;
        }

        public String getForcedJitlLogLevel() {
            return forcedJitlLogLevel;
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

        public Boolean getForcedV1Compatible() {
            return forcedV1Compatible;
        }

        public boolean isForcedV1Compatible() {
            return forcedV1Compatible != null && forcedV1Compatible;
        }

        public String getNotificationMailDefaultTo() {
            return notificationMailDefaultTo;
        }

        public String getNotificationMailDefaultCc() {
            return notificationMailDefaultCc;
        }

        public String getNotificationMailDefaultBcc() {
            return notificationMailDefaultBcc;
        }

        public boolean isEmpty() {
            return forcedGraceTimeout == null && forcedParallelism == null && forcedFailOnErrWritten == null && forcedV1Compatible == null
                    && notificationMailDefaultTo == null && notificationMailDefaultCc == null && notificationMailDefaultBcc == null;
        }
    }

    public class AgentConfig {

        private Map<String, JS7Agent> mappings;
        private String forcedControllerId;
        private String defaultControllerId;
        private JS7Agent forcedAgent; // use this instead of evaluated agent name
        private JS7Agent defaultAgent;// when agent can't be evaluated

        public AgentConfig withForcedControllerId(String val) {
            this.forcedControllerId = val;
            return this;
        }

        public AgentConfig withDefaultControllerId(String val) {
            this.defaultControllerId = val;
            return this;
        }

        public AgentConfig withForcedAgent(String val) throws Exception {
            this.forcedAgent = readAgentJson(val);
            return this;
        }

        public AgentConfig withDefaultAgent(String val) throws Exception {
            this.defaultAgent = readAgentJson(val);
            return this;
        }

        /** Agent mapping<br/>
         * Example 1<br>
         * - input map:<br/>
         * agentConfig.mappings = my_agent_1={"agentId":"primaryAgent", "platform":"UNIX", "controllerId":"js7","url":"http://localhost:4445"}<br/>
         **/
        public AgentConfig withMappings(String mappings) throws Exception {
            Map<String, JS7Agent> map = new HashMap<>();
            if (mappings != null) {
                String[] arr = mappings.trim().split(LIST_VALUE_DELIMITER);
                for (int i = 0; i < arr.length; i++) {
                    String[] marr = arr[i].split("=");
                    if (marr.length != 2) {
                        continue;
                    }
                    String key = marr[0].trim();
                    String val = marr[1].trim();
                    JS7Agent a = readAgentJson(val);
                    if (a != null) {
                        map.put(key, a);
                    }
                }
            }
            return withMappings(map);
        }

        public AgentConfig withMappings(Map<String, JS7Agent> map) {
            this.mappings = map;
            return this;
        }

        public JS7Agent getForcedAgent() {
            return forcedAgent;
        }

        public JS7Agent getDefaultAgent() {
            return defaultAgent;
        }

        public Map<String, JS7Agent> getMappings() {
            if (mappings == null) {
                mappings = new HashMap<>();
            }
            return mappings;
        }

        public String getForcedControllerId() {
            return forcedControllerId;
        }

        public String getDefaultControllerId() {
            return defaultControllerId;
        }

        public boolean isEmpty() {
            return (mappings == null || mappings.size() == 0) && forcedAgent == null && defaultAgent == null && forcedControllerId == null
                    && defaultControllerId == null;
        }

    }

    public class MockConfig {

        private String windowsScript;
        private String unixScript;
        private String jitlJobsMockLevel; // see com.sos.jitl.jobs.common.JobArguments

        public MockConfig withWindowsScript(String val) {
            this.windowsScript = val;
            return this;
        }

        public MockConfig withUnixScript(String val) {
            this.unixScript = val;
            return this;
        }

        public MockConfig withJitlJobsMockLevel(String val) {
            this.jitlJobsMockLevel = val;
            return this;
        }

        public String getWindowsScript() {
            return windowsScript;
        }

        public String getUnixScript() {
            return unixScript;
        }

        public String getJitlJobsMockLevel() {
            return jitlJobsMockLevel;
        }

        public boolean hasScript() {
            return windowsScript != null || unixScript != null;
        }

        public boolean isEmpty() {
            return !hasScript() && jitlJobsMockLevel == null;
        }
    }

    public class ScheduleConfig {

        private String forcedWorkingDayCalendarName;
        private String forcedNonWorkingDayCalendarName;
        private String defaultWorkingDayCalendarName;
        private String defaultNonWorkingDayCalendarName;

        private String defaultTimeZone = "Etc/UTC";
        private Boolean planOrders;
        private Boolean submitOrders;

        public ScheduleConfig withForcedWorkingDayCalendarName(String val) {
            this.forcedWorkingDayCalendarName = val;
            return this;
        }

        public ScheduleConfig withForcedNonWorkingDayCalendarName(String val) {
            this.forcedNonWorkingDayCalendarName = val;
            return this;
        }

        public ScheduleConfig withDefaultWorkingDayCalendarName(String val) {
            this.defaultWorkingDayCalendarName = val;
            return this;
        }

        public ScheduleConfig withDefaultNonWorkingDayCalendarName(String val) {
            this.defaultNonWorkingDayCalendarName = val;
            return this;
        }

        public ScheduleConfig withDefaultTimeZone(String val) {
            this.defaultTimeZone = val;
            return this;
        }

        public ScheduleConfig withPlanOrders(boolean val) {
            this.planOrders = val;
            return this;
        }

        public ScheduleConfig withSubmitOrders(boolean val) {
            this.submitOrders = val;
            return this;
        }

        public String getForcedWorkingDayCalendarName() {
            return forcedWorkingDayCalendarName;
        }

        public String getForcedNonWorkingDayCalendarName() {
            return forcedNonWorkingDayCalendarName;
        }

        public String getDefaultWorkingDayCalendarName() {
            return defaultWorkingDayCalendarName;
        }

        public String getDefaultNonWorkingDayCalendarName() {
            return defaultNonWorkingDayCalendarName;
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
            return forcedWorkingDayCalendarName == null && forcedNonWorkingDayCalendarName == null && defaultWorkingDayCalendarName == null
                    && defaultNonWorkingDayCalendarName == null && planOrders == null && submitOrders == null;
        }
    }

    public class SubFolderConfig {

        private Map<String, Integer> mappings;
        private String separator = "_";

        /** Sub Folder mapping - extract job names parts(parts are separated by subFolderSeparator) to create a sub folders<br/>
         * Example 1<br>
         * - input map: aapg = 2; ebzc = 0; wmad = 0<br/>
         * - input jil job name (application aapg): xxx_yyy_zzz_my_job<br/>
         * - output: zzz/my_job ("zzz" has the index 2 if the job name is separated by "_")
         * 
         * @param map
         * @return */
        public SubFolderConfig withMappings(String mapping) {
            Map<String, Integer> map = new HashMap<>();
            if (mapping != null) {
                // map and remove duplicates
                map = Stream.of(mapping.trim().split(LIST_VALUE_DELIMITER)).map(e -> e.split("=")).filter(e -> e.length == 2).collect(Collectors
                        .toMap(arr -> arr[0].trim(), arr -> Integer.parseInt(arr[1].trim()), (oldValue, newValue) -> oldValue));
            }
            return withMappings(map);
        }

        public SubFolderConfig withMappings(Map<String, Integer> val) {
            this.mappings = val;
            return this;
        }

        public SubFolderConfig withSeparator(String val) {
            this.separator = val;
            return this;
        }

        public Map<String, Integer> getMappings() {
            if (mappings == null) {
                mappings = new HashMap<>();
            }
            return mappings;
        }

        public String getSeparator() {
            return separator;
        }

        public boolean isEmpty() {
            return mappings == null || mappings.size() == 0;
        }
    }

}
