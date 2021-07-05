package com.sos.joc.monitoring.configuration.monitor.mail;

import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.jobresource.JobResource;

public class MailResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailResource.class);

    private static final String ARG_FROM = "from";
    private static final String ARG_FROM_NAME = "from_name";
    private static final String ARG_REPLAY_TO = "replay_to";
    private static final String ARG_TO = "to";
    private static final String ARG_CC = "cc";
    private static final String ARG_BCC = "bcc";

    private static ObjectMapper MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);

    private Properties properties;
    private String from;
    private String fromName;
    private String replayTo;
    private String to;
    private String cc;
    private String bcc;

    public void parse(String content) {
        init();
        try {
            Environment env = MAPPER.readValue(content, JobResource.class).getArguments();
            Map<String, String> arguments = env.getAdditionalProperties();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[parse]Environment=" + SOSString.toString(env));
            }
            if (arguments != null) {
                arguments.entrySet().stream().filter(e -> e.getKey().startsWith("mail.")).forEach(e -> {
                    properties.put(e.getKey(), e.getValue());
                });
                from = arguments.get(ARG_FROM);
                fromName = arguments.get(ARG_FROM_NAME);
                replayTo = arguments.get(ARG_REPLAY_TO);
                to = arguments.get(ARG_TO);
                cc = arguments.get(ARG_CC);
                bcc = arguments.get(ARG_BCC);
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void init() {
        properties = new Properties();
        from = null;
        fromName = null;
        replayTo = null;
        to = null;
        cc = null;
        bcc = null;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getFrom() {
        return from;
    }

    public String getFromName() {
        return fromName;
    }

    public String getReplayTo() {
        return replayTo;
    }

    public String getTo() {
        return to;
    }

    public String getCC() {
        return cc;
    }

    public String getBCC() {
        return bcc;
    }

}
