package com.sos.joc.db.inventory;

import java.util.HashMap;
import java.util.Map;

public class InventoryMeta {

    public enum ConfigurationType {
        WORKFLOW(1L), WORKFLOW_JOB(2L), JOB_CLASS(3L), AGENT_CLUSTER(4L), LOCK(5L), JUNCTION(6L), ORDER(7L), CALENDAR(8L);

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

}
