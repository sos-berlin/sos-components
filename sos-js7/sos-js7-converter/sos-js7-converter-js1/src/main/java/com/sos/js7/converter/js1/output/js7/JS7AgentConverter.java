package com.sos.js7.converter.js1.output.js7;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.sos.commons.util.SOSString;
import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.agent.ClusterAgent;
import com.sos.joc.model.agent.SubAgent;
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
        return a;
    }

    public static ClusterAgent convertAgentCluster(JS7Agent agent) {
        ClusterAgent a = JS7AgentHelper.copy(agent.getAgentCluster());
        if (a == null) {
            a = new ClusterAgent();
        }
        a.setAgentName(agent.getJS7AgentName());
        return a;
    }

    public static List<SubAgent> convert(RemoteSchedulers rs) {
        if (rs == null || rs.getRemoteScheduler() == null) {
            return null;
        }

        List<SubAgent> result = new ArrayList<>();
        int i = 0;
        for (RemoteScheduler r : rs.getRemoteScheduler()) {
            i++;
            SubAgent sa = new SubAgent();
            sa.setAgentId(getAgentIdFromURL(r.getRemoteScheduler(), i));
            sa.setDeployed(null);
            sa.setDisabled(null);
            sa.setIsClusterWatcher(null);
            sa.setIsDirector(null);
            sa.setOrdering(null);
            sa.setSubagentId(null);
            sa.setSyncState(null);
            sa.setTitle(null);
            sa.setUrl(r.getRemoteScheduler());
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
            return u.getHost() + "_" + u.getPort();
        } catch (MalformedURLException e) {
            return "agent_" + counter;
        }
    }

}
