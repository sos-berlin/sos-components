package com.sos.js7.converter.js1.output.js7.helper;

import com.sos.js7.converter.commons.config.json.JS7Agent;
import com.sos.js7.converter.js1.common.processclass.ProcessClass;

public class AgentHelper {

    private JS7Agent js7Agent;
    private ProcessClass processClass;
    private boolean js1AgentIsStandalone;

    public AgentHelper(String js7AgentName, ProcessClass processClass) {
        this((JS7Agent) null, processClass);
        this.js7Agent = new JS7Agent();
        this.js7Agent.setJS7AgentName(js7AgentName);
        if (processClass != null) {
            this.js7Agent.setOriginalAgentName(processClass.getName());
        }
    }

    public AgentHelper(JS7Agent js7Agent, ProcessClass processClass) {
        this.js7Agent = js7Agent;
        this.processClass = processClass;
        this.js1AgentIsStandalone = processClass == null || processClass.getRemoteSchedulers() == null || processClass.getRemoteSchedulers()
                .getRemoteScheduler().size() == 1;

    }

    public JS7Agent getJS7Agent() {
        return js7Agent;
    }

    public ProcessClass getProcessClass() {
        return processClass;
    }

    public boolean isJS1AgentIsStandalone() {
        return js1AgentIsStandalone;
    }

}
