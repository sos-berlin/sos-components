package com.sos.js7.converter.js1.output.js7;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.sos.commons.util.SOSString;
import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.agent.ClusterAgent;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.agent.SubagentCluster;
import com.sos.joc.model.agent.SubagentClusters;
import com.sos.joc.model.agent.SubagentDirectorType;
import com.sos.js7.converter.commons.JS7AgentHelper;
import com.sos.js7.converter.commons.config.json.JS7Agent;
import com.sos.js7.converter.js1.common.processclass.RemoteSchedulers;
import com.sos.js7.converter.js1.common.processclass.RemoteSchedulers.RemoteScheduler;

public class JS7AgentConverter {

    public enum JS7AgentConvertType {
        CONFIG_FORCED, CONFIG_MAPPINGS, CONFIG_DEFAULT, PROCESS_CLASS, JOB_CHAIN
    }

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

    public static List<SubAgent> convert(RemoteSchedulers rs, String agentId) {
        if (rs == null || rs.getRemoteScheduler() == null) {
            return null;
        }

        List<SubAgent> result = new ArrayList<>();
        int i = 0;
        for (RemoteScheduler r : rs.getRemoteScheduler()) {
            i++;
            SubAgent sa = new SubAgent();
            sa.setAgentId(agentId);
            sa.setSubagentId(getAgentIdFromURL(r.getRemoteScheduler(), i));
            sa.setUrl(r.getRemoteScheduler());
            if (i == 1) {
                sa.setIsDirector(SubagentDirectorType.PRIMARY_DIRECTOR);
                sa.setTitle("Primary Director " + agentId);
            } else {
                sa.setIsDirector(SubagentDirectorType.NO_DIRECTOR);
                sa.setTitle(null);
            }

            sa.setDeployed(null);
            sa.setDisabled(null);
            sa.setOrdering(null);
            sa.setSyncState(null);

            sa.setIsClusterWatcher(null);
            sa.setWithGenerateSubagentCluster(null);

            result.add(sa);
        }
        return result;
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
