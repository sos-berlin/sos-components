package com.sos.joc.cluster.api;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class JocClusterMeta {

    public static final String API_PATH = "/api/cluster";

    public static enum RequestPath {
        switchMember, restart, status
    }

    public enum HandlerIdentifier {

        cluster("cluster"),
        history("history"),
        dailyplan("dailyplan");
        private final String value;
        private final static Map<String, HandlerIdentifier> CONSTANTS = new HashMap<String, HandlerIdentifier>();

        static {
            for (HandlerIdentifier c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private HandlerIdentifier(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static HandlerIdentifier fromValue(String value) {
            HandlerIdentifier constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }
}
