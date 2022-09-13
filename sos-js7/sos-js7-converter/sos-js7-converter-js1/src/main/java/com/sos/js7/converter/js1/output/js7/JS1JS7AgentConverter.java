package com.sos.js7.converter.js1.output.js7;

import java.util.ArrayList;
import java.util.List;

import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.agent.SubagentDirectorType;
import com.sos.js7.converter.commons.agent.JS7AgentConverter;
import com.sos.js7.converter.js1.common.processclass.RemoteSchedulers;
import com.sos.js7.converter.js1.common.processclass.RemoteSchedulers.RemoteScheduler;

public class JS1JS7AgentConverter {

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
            sa.setSubagentId(JS7AgentConverter.getAgentIdFromURL(r.getRemoteScheduler(), i));
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

}
