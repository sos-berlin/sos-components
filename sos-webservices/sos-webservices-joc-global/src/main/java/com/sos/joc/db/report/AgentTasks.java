package com.sos.joc.db.report;

import com.sos.joc.model.report.Agent;

public class AgentTasks extends Agent {

    public AgentTasks(Long numOfSuccessfulTasks, String controllerId, String agent, String cause) {
        setNumOfSuccessfulTasks(numOfSuccessfulTasks);
        setControllerId(controllerId);
        setAgent(agent);
    }
}
