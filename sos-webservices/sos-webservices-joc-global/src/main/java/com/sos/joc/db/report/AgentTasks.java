package com.sos.joc.db.report;

import com.sos.joc.model.report.Agent;

public class AgentTasks extends Agent {

    public AgentTasks(Long numOfSuccessfulTasks, String jobschedulerId, String agent, String cause) {
        setNumOfSuccessfulTasks(numOfSuccessfulTasks);
        setJobschedulerId(jobschedulerId);
        setAgent(agent);
    }
}
