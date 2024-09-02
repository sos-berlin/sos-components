package com.sos.js7.converter.commons.agent;

import java.util.ArrayList;
import java.util.List;

import com.sos.inventory.model.job.Job;
import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.agent.ClusterAgent;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.agent.SubAgentId;
import com.sos.joc.model.agent.SubagentCluster;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.config.json.JS7Agent;

public class JS7AgentHelper {

    public static JS7Agent copy(JS7Agent agent) {
        if (agent == null) {
            return null;
        }

        JS7Agent a = new JS7Agent();
        a.setAgentCluster(copy(agent.getAgentCluster()));
        a.setOriginalAgentName(agent.getOriginalAgentName());
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
        a.setTitle(JS7ConverterHelper.getJS7InventoryObjectTitle(agent.getTitle()));
        a.setAgentName(agent.getAgentName());
        a.setAgentNameAliases(agent.getAgentNameAliases());
        a.setControllerId(agent.getControllerId());
        a.setDeployed(agent.getDeployed());
        a.setDisabled(agent.getDisabled());
        a.setHidden(agent.getHidden());
        a.setOrdering(agent.getOrdering());
        a.setSyncState(agent.getSyncState());
        a.setUrl(agent.getUrl());
        a.setVersion(agent.getVersion());
        return a;
    }

    // TODO
    public static ClusterAgent copy(ClusterAgent agent) {
        if (agent == null) {
            return null;
        }
        ClusterAgent a = new ClusterAgent();
        a.setAgentId(agent.getAgentId());
        a.setTitle(JS7ConverterHelper.getJS7InventoryObjectTitle(agent.getTitle()));
        a.setAgentName(agent.getAgentName());
        a.setAgentNameAliases(agent.getAgentNameAliases());
        a.setControllerId(agent.getControllerId());
        a.setDeployed(agent.getDeployed());
        a.setDisabled(agent.getDisabled());
        a.setHidden(agent.getHidden());
        a.setOrdering(agent.getOrdering());
        a.setSyncState(agent.getSyncState());
        a.setUrl(agent.getUrl());
        a.setVersion(agent.getVersion());

        a.setSubagents(copySubagents(agent.getSubagents()));
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

    public static Job setAgent(Job job, JS7Agent js7Agent) {
        if (job != null && js7Agent != null) {
            job.setAgentName(js7Agent.getJS7AgentName());
            job.setSubagentClusterId(js7Agent.getSubagentClusterId());
        }
        return job;
    }

    public static List<SubAgent> copySubagents(List<SubAgent> l) {
        if (l == null) {
            return null;
        }
        return new ArrayList<>(l);
    }

    private static SubagentCluster copy(SubagentCluster cluster) {
        SubagentCluster a = new SubagentCluster();
        a.setAgentId(cluster.getAgentId());
        a.setTitle(JS7ConverterHelper.getJS7InventoryObjectTitle(cluster.getTitle()));
        a.setControllerId(cluster.getControllerId());
        a.setDeployed(cluster.getDeployed());
        a.setOrdering(cluster.getOrdering());
        a.setSubagentClusterId(cluster.getSubagentClusterId());
        a.setSubagentIds(copySubAgentId(cluster.getSubagentIds()));
        a.setSyncState(cluster.getSyncState());
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
