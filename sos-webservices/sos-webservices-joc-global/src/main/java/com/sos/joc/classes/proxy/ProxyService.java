package com.sos.joc.classes.proxy;

import java.util.List;

import org.slf4j.MDC;

import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterService;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.JocClusterServices;
import com.sos.js7.event.controller.configuration.controller.ControllerConfiguration;


public class ProxyService extends JocClusterService {
    
    private static final String IDENTIFIER = JocClusterServices.proxy.name();

    public ProxyService(JocConfiguration jocConf, ThreadGroup clusterThreadGroup) {
        super(jocConf, clusterThreadGroup, IDENTIFIER);
    }

    @Override
    public JocClusterAnswer start(List<ControllerConfiguration> controllers) {
        MDC.put("clusterService", IDENTIFIER);
        if (Globals.sosCockpitProperties == null) {
            Globals.sosCockpitProperties = new JocCockpitProperties();
        }
        Proxies.startAll(Globals.sosCockpitProperties, ProxyUser.JOC);
        //Proxies.startAll(Globals.sosCockpitProperties, ProxyUser.HISTORY);
        return JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);
    }

    @Override
    public JocClusterAnswer stop() {
        MDC.put("clusterService", IDENTIFIER);
        Proxies.closeAll();
        return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
    }

}
