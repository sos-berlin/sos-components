package com.sos.js7.converter.commons.agent;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.sos.commons.util.SOSString;
import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.agent.ClusterAgent;
import com.sos.joc.model.agent.SubagentCluster;
import com.sos.js7.converter.commons.config.json.JS7Agent;

public class JS7AgentConverter {

    public enum JS7AgentConvertType {
        CONFIG_FORCED, CONFIG_MAPPINGS, CONFIG_DEFAULT, PROCESS_CLASS, JOB_CHAIN
    }

    public static final String DEFAULT_AGENT_NAME = "default_agent";
    public static final String DEFAULT_AGENT_URL = "http://localhost:4445";

    public static Agent convertStandaloneAgent(JS7Agent agent) {
        Agent a = JS7AgentHelper.copy(agent.getStandaloneAgent());
        if (a == null) {
            a = new Agent();
        }
        a.setAgentName(agent.getJS7AgentName());
        if (a.getAgentName() == null) {
            a.setAgentName(getAgentIdFromURL(a.getUrl(), 1));
        }
        a.setDeployed(null);
        a.setHidden(null);
        a.setDisabled(null);
        a.setIsClusterWatcher(null);
        return a;
    }

    public static ClusterAgent convertAgentCluster(JS7Agent agent) {
        ClusterAgent a = JS7AgentHelper.copy(agent.getAgentCluster());
        if (a == null) {
            a = new ClusterAgent();
        }
        a.setAgentName(agent.getJS7AgentName());
        a.setDeployed(null);
        a.setHidden(null);
        a.setDisabled(null);
        a.setIsClusterWatcher(null);
        return a;
    }

    public static List<SubagentCluster> convertSubagentClusters(JS7Agent agent) {
        List<SubagentCluster> a = JS7AgentHelper.copy(agent.getSubagentClusters());
        if (a == null) {
            a = new ArrayList<>();
        }
        return a;
    }

    public static String getAgentIdFromURL(String url, int counter) {
        if (SOSString.isEmpty(url)) {
            return "agent_" + counter;
        }
        try {
            URL u = new URL(url);
            return (u.getHost()).replaceAll("\\.", "-") + "-" + u.getPort();
        } catch (MalformedURLException e) {
            return "agent_" + counter;
        }
    }
}
