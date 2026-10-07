package com.sos.joc.classes.publish.record;

import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.publish.DeployAction;

public record DeployTransportRecord(String account, String commitId, JOCResourceImpl impl, String wsIdentifier, String dailyPlanDate, 
        boolean includeLate, String transactionId, DeployAction action) {

}
