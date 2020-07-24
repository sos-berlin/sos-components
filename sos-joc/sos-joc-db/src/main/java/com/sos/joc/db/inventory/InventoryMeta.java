package com.sos.joc.db.inventory;

import java.util.HashMap;
import java.util.Map;

public class InventoryMeta {

    public enum ConfigurationType {
        FOLDER(0), WORKFLOW(1), JOBCLASS(2), AGENTCLUSTER(3), LOCK(4), JUNCTION(5), CALENDAR(6), ORDER(7);

        private final Integer value;
        private final static Map<Integer, ConfigurationType> CONSTANTS = new HashMap<Integer, ConfigurationType>();

        static {
            for (ConfigurationType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private ConfigurationType(Integer val) {
            value = val;
        }

        public Integer value() {
            return value;
        }

        public static ConfigurationType fromValue(Integer value) {
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
        SHELL(0), JAVA(1);

        private final Integer value;
        private final static Map<Integer, JobType> CONSTANTS = new HashMap<Integer, JobType>();

        static {
            for (JobType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private JobType(Integer val) {
            value = val;
        }

        public Integer value() {
            return value;
        }

        public static JobType fromValue(Integer value) {
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
        NORMAL(0), CRITICAL(1);

        private final Integer value;
        private final static Map<Integer, JobCriticality> CONSTANTS = new HashMap<Integer, JobCriticality>();

        static {
            for (JobCriticality c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private JobCriticality(Integer val) {
            value = val;
        }

        public Integer value() {
            return value;
        }

        public static JobCriticality fromValue(Integer value) {
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
        INFO(0), DEBUG(1), TRACE(2);

        private final Integer value;
        private final static Map<Integer, JobLogLevel> CONSTANTS = new HashMap<Integer, JobLogLevel>();

        static {
            for (JobLogLevel c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private JobLogLevel(Integer val) {
            value = val;
        }

        public Integer value() {
            return value;
        }

        public static JobLogLevel fromValue(Integer value) {
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
        SUCCESS(0), FAILURE(1);

        private final Integer value;
        private final static Map<Integer, JobRetunCodeMeaning> CONSTANTS = new HashMap<Integer, JobRetunCodeMeaning>();

        static {
            for (JobRetunCodeMeaning c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private JobRetunCodeMeaning(Integer val) {
            value = val;
        }

        public Integer value() {
            return value;
        }

        public static JobRetunCodeMeaning fromValue(Integer value) {
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
        AWAIT(0), PUBLISH(1);

        private final Integer value;
        private final static Map<Integer, JunctionType> CONSTANTS = new HashMap<Integer, JunctionType>();

        static {
            for (JunctionType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private JunctionType(Integer val) {
            value = val;
        }

        public Integer value() {
            return value;
        }

        public static JunctionType fromValue(Integer value) {
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
        FIXED_PRIORITY(0), ROUND_ROBIN(1);

        private final Integer value;
        private final static Map<Integer, AgentClusterSchedulingType> CONSTANTS = new HashMap<Integer, AgentClusterSchedulingType>();

        static {
            for (AgentClusterSchedulingType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private AgentClusterSchedulingType(Integer val) {
            value = val;
        }

        public Integer value() {
            return value;
        }

        public static AgentClusterSchedulingType fromValue(Integer value) {
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
        WORKINGDAYSCALENDAR(0), NONWORKINGDAYSCALENDAR(1);

        private final Integer value;
        private final static Map<Integer, CalendarType> CONSTANTS = new HashMap<Integer, CalendarType>();

        static {
            for (CalendarType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private CalendarType(Integer val) {
            value = val;
        }

        public Integer value() {
            return value;
        }

        public static CalendarType fromValue(Integer value) {
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
        STRING(0), INTEGER(1), NUMBER(2), BOOLEAN(3);

        private final Integer value;
        private final static Map<Integer, ArgumentType> CONSTANTS = new HashMap<Integer, ArgumentType>();

        static {
            for (ArgumentType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private ArgumentType(Integer val) {
            value = val;
        }

        public Integer value() {
            return value;
        }

        public static ArgumentType fromValue(Integer value) {
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
        EXCLUSIVE(0), SHARED(1), QUANTITATIVE_RESOURCES(2);

        private final Integer value;
        private final static Map<Integer, LockType> CONSTANTS = new HashMap<Integer, LockType>();

        static {
            for (LockType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private LockType(Integer val) {
            value = val;
        }

        public Integer value() {
            return value;
        }

        public static LockType fromValue(Integer value) {
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
