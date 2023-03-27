package com.sos.js7.converter.commons.config.items;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.config.json.JS7Agent;

public class AgentConfig extends AConfigItem {

    private static final String CONFIG_KEY = "agentConfig";

    private Map<String, JS7Agent> mappings;
    private String forcedControllerId;
    private String defaultControllerId;
    private JS7Agent forcedAgent; // use this instead of evaluated agent name
    private JS7Agent defaultAgent;// when agent can't be evaluated

    public AgentConfig() {
        super(CONFIG_KEY);
    }

    @Override
    protected void parse(String key, String val) throws Exception {
        switch (key) {
        case "forcedControllerId":
            withForcedControllerId(val);
            break;
        case "defaultControllerId":
            withDefaultControllerId(val);
            break;
        case "forcedAgent":
            withForcedAgent(val);
            break;
        case "defaultAgent":
            withDefaultAgent(val);
            break;
        case "mappings":
            withMappings(val);
            break;
        }
    }

    @Override
    public boolean isEmpty() {
        return (mappings == null || mappings.size() == 0) && forcedAgent == null && defaultAgent == null && forcedControllerId == null
                && defaultControllerId == null;
    }

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

    private JS7Agent readAgentJson(final String val) throws Exception {
        if (val == null) {
            return null;
        }
        String value = val;
        if (value.toLowerCase().endsWith(".json")) {
            Path jsonFile = Paths.get(val);
            if (!jsonFile.isAbsolute()) {
                if (getPropertiesFile() != null && getPropertiesFile().getParent() != null) {
                    jsonFile = getPropertiesFile().getParent().resolve(val);
                }
            }
            if (!Files.exists(jsonFile)) {
                throw new Exception("[" + jsonFile.toAbsolutePath() + "]file not found");
            }
            value = SOSPath.readFile(jsonFile, StandardCharsets.UTF_8);
        }
        return JS7ConverterHelper.JSON_OM.readValue(value, JS7Agent.class);
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append("[");

        List<String> l = new ArrayList<>();
        if (forcedControllerId != null) {
            l.add("forcedControllerId=" + forcedControllerId);
        }
        if (defaultControllerId != null) {
            l.add("defaultControllerId=" + defaultControllerId);
        }
        if (forcedAgent != null) {
            l.add("forcedAgent=" + SOSString.toString(forcedAgent));
        }
        if (defaultAgent != null) {
            l.add("defaultAgent=" + SOSString.toString(defaultAgent));
        }
        if (mappings != null) {
            l.add("mappings=" + mappings);
        }

        sb.append(String.join(",", l));
        sb.append("]");
        return sb.toString();
    }

}
