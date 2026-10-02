package com.sos.joc.publish.record;

import java.util.Map;
import java.util.Set;

import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.publish.mapper.UpdateableFileOrderSourceAgentName;
import com.sos.joc.publish.mapper.UpdateableWorkflowJobAgentName;

public record DeployRecord(Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedDeployables, 
        Set<UpdateableWorkflowJobAgentName> updateableAgentNames, Set<UpdateableFileOrderSourceAgentName> updateableAgentNamesFileOrderSources) {}
