package com.sos.js7.converter.commons;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JS7ConverterConfig {

    public enum Platform {
        UNIX, WINDOWS
    }

    private final JobConfig jobConfig;

    private GenerateConfig generateConfig;
    private ScheduleConfig scheduleConfig;
    private SubFolderConfig subFolderConfig;
    private MockConfig mockConfig;

    public JS7ConverterConfig() {
        jobConfig = this.new JobConfig();
    }

    public JobConfig withJobConfig() {
        return jobConfig;
    }

    public GenerateConfig withGenerateConfig() {
        generateConfig = this.new GenerateConfig();
        return generateConfig;
    }

    public ScheduleConfig withScheduleConfig() {
        scheduleConfig = this.new ScheduleConfig();
        return scheduleConfig;
    }

    public SubFolderConfig withSubFolderConfig() {
        subFolderConfig = this.new SubFolderConfig();
        return subFolderConfig;
    }

    public MockConfig withMockConfig() {
        mockConfig = this.new MockConfig();
        return mockConfig;
    }

    public JobConfig getJobConfig() {
        return jobConfig;
    }

    public GenerateConfig getGenerateConfig() {
        return generateConfig;
    }

    public ScheduleConfig getScheduleConfig() {
        return scheduleConfig;
    }

    public SubFolderConfig getSubFolderConfig() {
        return subFolderConfig;
    }

    public MockConfig getMockConfig() {
        return mockConfig;
    }

    public class JobConfig {

        private String scriptNewLine = "\n";
        private Integer graceTimeout;
        private Integer parallelism;
        private Boolean failOnErrWritten;

        public JobConfig withScriptNewLine(String newLine) {
            this.scriptNewLine = newLine;
            return this;
        }

        public JobConfig withGraceTimeout(Integer graceTimeout) {
            this.graceTimeout = graceTimeout;
            return this;
        }

        public JobConfig withParallelism(Integer parallelism) {
            this.parallelism = parallelism;
            return this;
        }

        public JobConfig withFailOnErrWritten(Boolean failOnErrWritten) {
            this.failOnErrWritten = failOnErrWritten;
            return this;
        }

        public String getScriptNewLine() {
            return scriptNewLine;
        }

        public Integer getGraceTimeout() {
            return graceTimeout;
        }

        public Integer getParallelism() {
            return parallelism;
        }

        public Boolean getFailOnErrWritten() {
            return failOnErrWritten;
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

    public class ScheduleConfig {

        private String defaultCalendarName;
        private String defaultTimeZone = "Etc/UTC";
        private Boolean planOrders;
        private Boolean submitOrders;

        public ScheduleConfig withDefaultCalendarName(String defaultCalendarName) {
            this.defaultCalendarName = defaultCalendarName;
            return this;
        }

        public ScheduleConfig withDefaultTimeZone(String defaultTimeZone) {
            this.defaultTimeZone = defaultTimeZone;
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

        public String getDefaultCalendarName() {
            return defaultCalendarName;
        }

        public String getDefaultTimeZone() {
            return defaultTimeZone;
        }

        public Boolean planOrders() {
            return planOrders;
        }

        public Boolean submitOrders() {
            return submitOrders;
        }

    }

    public class SubFolderConfig {

        private Map<String, Integer> map;
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
            Map<String, Integer> map = null;
            if (mapping != null) {
                // map and remove duplicates
                map = Stream.of(mapping.trim().split(";")).map(e -> e.split("=")).filter(e -> e.length == 2).collect(Collectors.toMap(arr -> arr[0]
                        .trim(), arr -> Integer.parseInt(arr[1].trim()), (oldValue, newValue) -> oldValue));
                if (map.size() == 0) {
                    map = null;
                }
            }
            return withMapping(map);
        }

        public SubFolderConfig withMapping(Map<String, Integer> map) {
            this.map = map;
            return this;
        }

        public SubFolderConfig withSeparator(String separator) {
            this.separator = separator;
            return this;
        }

        public Map<String, Integer> getMap() {
            return map;
        }

        public String getSeparator() {
            return separator;
        }

    }

    public class MockConfig {

        private Platform platform = Platform.UNIX;
        private String agentName;
        private String script;

        public MockConfig withPlatform(Platform platform) {
            this.platform = platform;
            return this;
        }

        public MockConfig withAgentName(String agentName) {
            this.agentName = agentName;
            return this;
        }

        public MockConfig withScript(String script) {
            this.script = script;
            return this;
        }

        public Platform getPlatform() {
            return platform;
        }

        public String getAgentName() {
            return agentName;
        }

        public String getScript() {
            return script;
        }
    }
}
