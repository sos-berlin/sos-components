package com.sos.joc.db.inventory;

import java.util.HashMap;
import java.util.Map;

public class InventoryMeta {

    public enum ConfigurationType {
        // TODO JOB should be changed to WORKFLOWJOB
        WORKFLOW(1L), JOB(2L), JOBCLASS(3L), AGENTCLUSTER(4L), LOCK(5L), JUNCTION(6L), ORDER(7L), CALENDAR(8L), FOLDER(9L);

        private final Long value;
        private final static Map<Long, ConfigurationType> CONSTANTS = new HashMap<Long, ConfigurationType>();

        static {
            for (ConfigurationType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private ConfigurationType(Long val) {
            value = val;
        }

        public Long value() {
            return value;
        }

        public static ConfigurationType fromValue(Long value) {
            if (value == null) {
                return null;
            }
            ConfigurationType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(String.valueOf(value));
            } else {
                return constant;
            }
        }

    }

    public enum JobType {
        SHELL(1L), JAVA(2L);

        private final Long value;
        private final static Map<Long, JobType> CONSTANTS = new HashMap<Long, JobType>();

        static {
            for (JobType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private JobType(Long val) {
            value = val;
        }

        public Long value() {
            return value;
        }

        public static JobType fromValue(Long value) {
            if (value == null) {
                return null;
            }
            JobType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(String.valueOf(value));
            } else {
                return constant;
            }
        }

    }

    public enum JobCriticality {
        NORMAL(1L), CRITICAL(2L);

        private final Long value;
        private final static Map<Long, JobCriticality> CONSTANTS = new HashMap<Long, JobCriticality>();

        static {
            for (JobCriticality c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private JobCriticality(Long val) {
            value = val;
        }

        public Long value() {
            return value;
        }

        public static JobCriticality fromValue(Long value) {
            if (value == null) {
                return null;
            }
            JobCriticality constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(String.valueOf(value));
            } else {
                return constant;
            }
        }

    }

    public enum JobLogLevel {
        INFO(1L), DEBUG(2L), TRACE(3L);

        private final Long value;
        private final static Map<Long, JobLogLevel> CONSTANTS = new HashMap<Long, JobLogLevel>();

        static {
            for (JobLogLevel c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private JobLogLevel(Long val) {
            value = val;
        }

        public Long value() {
            return value;
        }

        public static JobLogLevel fromValue(Long value) {
            if (value == null) {
                return null;
            }
            JobLogLevel constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(String.valueOf(value));
            } else {
                return constant;
            }
        }

    }

    public enum JobRetunCodeMeaning {
        SUCCESS(1L), FAILURE(2L);

        private final Long value;
        private final static Map<Long, JobRetunCodeMeaning> CONSTANTS = new HashMap<Long, JobRetunCodeMeaning>();

        static {
            for (JobRetunCodeMeaning c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private JobRetunCodeMeaning(Long val) {
            value = val;
        }

        public Long value() {
            return value;
        }

        public static JobRetunCodeMeaning fromValue(Long value) {
            if (value == null) {
                return null;
            }
            JobRetunCodeMeaning constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(String.valueOf(value));
            } else {
                return constant;
            }
        }

    }

    public enum JunctionType {
        AWAIT(1L), PUBLISH(2L);

        private final Long value;
        private final static Map<Long, JunctionType> CONSTANTS = new HashMap<Long, JunctionType>();

        static {
            for (JunctionType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private JunctionType(Long val) {
            value = val;
        }

        public Long value() {
            return value;
        }

        public static JunctionType fromValue(Long value) {
            if (value == null) {
                return null;
            }
            JunctionType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(String.valueOf(value));
            } else {
                return constant;
            }
        }

    }

    public enum AgentClusterSchedulingType {
        FIXED_PRIORITY(1L), ROUND_ROBIN(2L);

        private final Long value;
        private final static Map<Long, AgentClusterSchedulingType> CONSTANTS = new HashMap<Long, AgentClusterSchedulingType>();

        static {
            for (AgentClusterSchedulingType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private AgentClusterSchedulingType(Long val) {
            value = val;
        }

        public Long value() {
            return value;
        }

        public static AgentClusterSchedulingType fromValue(Long value) {
            if (value == null) {
                return null;
            }
            AgentClusterSchedulingType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(String.valueOf(value));
            } else {
                return constant;
            }
        }

    }

    public enum CalendarType {
        WORKINGDAYSCALENDAR(1L), NONWORKINGDAYSCALENDAR(2L);

        private final Long value;
        private final static Map<Long, CalendarType> CONSTANTS = new HashMap<Long, CalendarType>();

        static {
            for (CalendarType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private CalendarType(Long val) {
            value = val;
        }

        public Long value() {
            return value;
        }

        public static CalendarType fromValue(Long value) {
            if (value == null) {
                return null;
            }
            CalendarType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(String.valueOf(value));
            } else {
                return constant;
            }
        }

    }

    public enum ArgumentType {
        // integer (int, long), number(floating values)
        STRING(1L), INTEGER(2L), NUMBER(3L), BOOLEAN(4L);

        private final Long value;
        private final static Map<Long, ArgumentType> CONSTANTS = new HashMap<Long, ArgumentType>();

        static {
            for (ArgumentType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private ArgumentType(Long val) {
            value = val;
        }

        public Long value() {
            return value;
        }

        public static ArgumentType fromValue(Long value) {
            if (value == null) {
                return null;
            }
            ArgumentType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(String.valueOf(value));
            } else {
                return constant;
            }
        }

    }

    public enum LockType {
        EXCLUSIVE(1L), SHARED(2L), QUANTITATIVE_RESOURCES(3L);

        private final Long value;
        private final static Map<Long, LockType> CONSTANTS = new HashMap<Long, LockType>();

        static {
            for (LockType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private LockType(Long val) {
            value = val;
        }

        public Long value() {
            return value;
        }

        public static LockType fromValue(Long value) {
            if (value == null) {
                return null;
            }
            LockType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(String.valueOf(value));
            } else {
                return constant;
            }
        }

    }
}
