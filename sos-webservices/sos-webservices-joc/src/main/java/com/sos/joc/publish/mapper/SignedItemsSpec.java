package com.sos.joc.publish.mapper;

import java.util.Map;
import java.util.Set;

import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.model.sign.JocKeyPair;

public class SignedItemsSpec {
    
    private JocKeyPair keyPair;
    private Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations; 
    private Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedDeployables;
    private Set<UpdateableWorkflowJobAgentName> updateableWorkflowJobAgentNames;
    private Set<UpdateableFileOrderSourceAgentName> updateableFileOrderSourceAgentNames;
    private Long auditlogId;
    
    public SignedItemsSpec (JocKeyPair keyPair,
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations, 
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedDeployables,
            Set<UpdateableWorkflowJobAgentName> updateableWorkflowJobAgentNames, 
            Set<UpdateableFileOrderSourceAgentName> updateableFileOrderSourceAgentNames,
            Long auditlogId) {
        this.keyPair = keyPair;
        this.verifiedConfigurations = verifiedConfigurations;
        this.verifiedDeployables = verifiedDeployables;
        this.updateableWorkflowJobAgentNames = updateableWorkflowJobAgentNames;
        this.updateableFileOrderSourceAgentNames = updateableFileOrderSourceAgentNames;
        this.auditlogId = auditlogId;
    }

    
    public JocKeyPair getKeyPair() {
        return keyPair;
    }
    
    public Map<DBItemInventoryConfiguration, DBItemDepSignatures> getVerifiedConfigurations() {
        return verifiedConfigurations;
    }
    
    public Map<DBItemDeploymentHistory, DBItemDepSignatures> getVerifiedDeployables() {
        return verifiedDeployables;
    }
    
    public Set<UpdateableWorkflowJobAgentName> getUpdateableWorkflowJobAgentNames() {
        return updateableWorkflowJobAgentNames;
    }

    public Set<UpdateableFileOrderSourceAgentName> getUpdateableFileOrderSourceAgentNames() {
        return updateableFileOrderSourceAgentNames;
    }

	public Long getAuditlogId() {
		return auditlogId;
	}
    
}
