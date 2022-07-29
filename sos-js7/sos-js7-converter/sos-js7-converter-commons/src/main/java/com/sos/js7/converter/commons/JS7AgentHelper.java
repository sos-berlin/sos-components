package com.sos.js7.converter.commons;

import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.agent.ClusterAgent;
import com.sos.js7.converter.commons.config.json.JS7Agent;

public class JS7AgentHelper {

    public static JS7Agent copy(JS7Agent agent) {
        if (agent == null) {
            return null;
        }

        JS7Agent a = new JS7Agent();
        a.setAgentCluster(copy(agent.getAgentCluster()));
        a.setJS1AgentName(agent.getJS1AgentName());
        a.setJS7AgentName(agent.getJS7AgentName());
        a.setPlatform(agent.getPlatform());
        a.setStandaloneAgent(copy(agent.getStandaloneAgent()));
        a.setSubagentClusterId(agent.getSubagentClusterId());

        return a;
    }

    public static Agent copy(Agent agent) {
        if (agent == null) {
            return null;
        }
        Agent a = new Agent();
        a.setAgentId(agent.getAgentId());
        a.setAgentName(agent.getAgentName());
        a.setAgentNameAliases(agent.getAgentNameAliases());
        a.setControllerId(agent.getControllerId());
        a.setDeployed(agent.getDeployed());
        a.setDisabled(agent.getDisabled());
        a.setHidden(agent.getHidden());
        a.setIsClusterWatcher(agent.getIsClusterWatcher());
        a.setOrdering(agent.getOrdering());
        a.setSyncState(agent.getSyncState());
        a.setTitle(agent.getTitle());
        a.setUrl(agent.getUrl());
        return a;
    }

    // TODO
    public static ClusterAgent copy(ClusterAgent agent) {
        if (agent == null) {
            return null;
        }
        ClusterAgent a = new ClusterAgent();
        a.setAgentId(agent.getAgentId());
        a.setAgentName(agent.getAgentName());
        a.setAgentNameAliases(agent.getAgentNameAliases());
        a.setControllerId(agent.getControllerId());
        a.setDeployed(agent.getDeployed());
        a.setDisabled(agent.getDisabled());
        a.setHidden(agent.getHidden());
        a.setIsClusterWatcher(agent.getIsClusterWatcher());
        a.setOrdering(agent.getOrdering());
        a.setSyncState(agent.getSyncState());
        a.setTitle(agent.getTitle());
        a.setUrl(agent.getUrl());

        a.setSubagents(JS7ConverterHelper.copy(agent.getSubagents()));
        return a;
    }

}
