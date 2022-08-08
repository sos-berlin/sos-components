package com.sos.js7.converter.commons;

import java.util.ArrayList;
import java.util.List;

import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.agent.ClusterAgent;
import com.sos.joc.model.agent.SubAgentId;
import com.sos.joc.model.agent.SubagentCluster;
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

    public static List<SubagentCluster> copy(List<SubagentCluster> clusters) {
        if (clusters == null) {
            return null;
        }
        List<SubagentCluster> a = new ArrayList<>();
        for (SubagentCluster c : clusters) {
            a.add(copy(c));
        }
        return a;
    }

    private static SubagentCluster copy(SubagentCluster cluster) {
        SubagentCluster a = new SubagentCluster();
        a.setAgentId(cluster.getAgentId());
        a.setControllerId(cluster.getControllerId());
        a.setDeployed(cluster.getDeployed());
        a.setOrdering(cluster.getOrdering());
        a.setSubagentClusterId(cluster.getSubagentClusterId());
        a.setSubagentIds(copySubAgentId(cluster.getSubagentIds()));
        a.setSyncState(cluster.getSyncState());
        a.setTitle(cluster.getTitle());
        return a;
    }

    private static List<SubAgentId> copySubAgentId(List<SubAgentId> subagentIds) {
        List<SubAgentId> a = new ArrayList<>();
        for (SubAgentId subagentId : subagentIds) {
            SubAgentId id = new SubAgentId();
            id.setPriority(subagentId.getPriority());
            id.setSubagentId(subagentId.getSubagentId());
            a.add(id);
        }
        return a;
    }
}
