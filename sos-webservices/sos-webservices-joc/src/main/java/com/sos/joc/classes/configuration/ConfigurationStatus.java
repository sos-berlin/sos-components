package com.sos.joc.classes.configuration;

import java.util.Optional;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.model.common.ConfigurationState;
import com.sos.joc.model.common.ConfigurationStateText;

public class ConfigurationStatus {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationStatus.class);

    public static ConfigurationState getConfigurationStatus() {
        ConfigurationState confStatus = new ConfigurationState();
        confStatus.setMessage("myMessage");
        confStatus.setSeverity(-1);
        confStatus.set_text(ConfigurationStateText.CHANGED_FILE_NOT_LOADED);
        return confStatus;
    }

    /** @param jsonObject has to be the 'obstacles' json array of a json object
     *            of orders[], jobs[], job_chains[], etc. collection from a
     *            JobScheduler response
     * 
     * @return ConfigurationStatusSchema
     */
    public static ConfigurationState getConfigurationStatus(JsonArray obstacles) {
        try {
            Optional<JsonValue> fileBasedObstacleItem = obstacles.stream().filter(p -> "fileBasedObstacles".equalsIgnoreCase(((JsonObject) p).getString("TYPE"))).findFirst();
            if (!fileBasedObstacleItem.isPresent()) {
                return null;
            }
            JsonArray fileBasedObstacles = ((JsonObject) fileBasedObstacleItem.get()).getJsonArray("fileBasedObstacles");
            ConfigurationState errorInConfFileStatus = null;
            ConfigurationState removingDelayedStatus = null;
            ConfigurationState replacementStatus = null;
            ConfigurationState notLoadedStatus = null;
            StringBuilder s = new StringBuilder();
            for (JsonObject fileBasedObstacle : fileBasedObstacles.getValuesAs(JsonObject.class)) {
                switch (fileBasedObstacle.getString("TYPE", "").toLowerCase()) {
                case "not_initialized":
                case "notinitialized": // not yet in JSON response
                    errorInConfFileStatus = new ConfigurationState();
                    setSeverity(errorInConfFileStatus, ConfigurationStateText.ERROR_IN_CONFIGURATION_FILE);
                    setMessage(errorInConfFileStatus, fileBasedObstacle);
                    break;
                case "badstate":
                    if ("not_initialized".equals(fileBasedObstacle.getString("state", ""))) {
                        errorInConfFileStatus = new ConfigurationState();
                        setSeverity(errorInConfFileStatus, ConfigurationStateText.ERROR_IN_CONFIGURATION_FILE);
                        setMessage(errorInConfFileStatus, fileBasedObstacle); 
                    }
                case "removed":
                    removingDelayedStatus = new ConfigurationState();
                    setSeverity(removingDelayedStatus, ConfigurationStateText.REMOVING_DELAYED);
                    setMessage(removingDelayedStatus, fileBasedObstacle);
                    break;
                case "replaced":
                    if (!fileBasedObstacle.containsKey("error")) {
                        replacementStatus = new ConfigurationState();
                        setSeverity(replacementStatus, ConfigurationStateText.REPLACEMENT_IS_STANDING_BY);
                    } else {
                        notLoadedStatus = new ConfigurationState();
                        setSeverity(notLoadedStatus, ConfigurationStateText.CHANGED_FILE_NOT_LOADED);
                        setMessage(notLoadedStatus, fileBasedObstacle);
                    }
                    break;
                case "missingrequisites":
                    if (fileBasedObstacle.containsKey("paths")) {
                        for (JsonString path : fileBasedObstacle.getJsonArray("paths").getValuesAs(JsonString.class)) {
                            s.append(path.getString());
                            s.append("; ");
                        }
                    }
                    break;
                }
            }
            if (errorInConfFileStatus != null) {
                return errorInConfFileStatus;
            }
            if (notLoadedStatus != null) {
                return notLoadedStatus;
            }
            if (removingDelayedStatus != null) {
                return removingDelayedStatus;
            }
            if (replacementStatus != null) {
                return replacementStatus;
            }
            if (s.length() > 0) {
                String message = s.toString().replaceFirst(";\\s*$", "");
                if (!message.isEmpty()) {
                    ConfigurationState missingResourceStatus = new ConfigurationState();
                    setSeverity(missingResourceStatus, ConfigurationStateText.RESOURCE_IS_MISSING);
                    missingResourceStatus.setMessage(message);
                    return missingResourceStatus;
                }
            }

        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return null;
    }

    private static void setMessage(ConfigurationState confStatus, JsonObject jsonObj) {
        confStatus.setMessage(jsonObj.getString("error"));
    }

    private static void setSeverity(ConfigurationState confStatus, ConfigurationStateText text) {
        confStatus.set_text(text);
        switch (confStatus.get_text()) {
        case CHANGED_FILE_NOT_LOADED:
        case RESOURCE_IS_MISSING:
        case ERROR_IN_CONFIGURATION_FILE:
            confStatus.setSeverity(2);
            break;
        case REMOVING_DELAYED:
        case REPLACEMENT_IS_STANDING_BY:
            confStatus.setSeverity(5);
            break;
        case OK:
            confStatus.setSeverity(4);
            break;
        }
    }
}
