package com.sos.joc.publish.mapper;

import java.util.Map;
import java.util.Set;

import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.model.sign.JocKeyPair;

public class SignedItemsSpec {
    
    JocKeyPair keyPair;
    Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations; 
    Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedDeployables;
    Set<UpdateableWorkflowJobAgentName> updateableWorkflowJobAgentNames;
    Set<UpdateableFileOrderSourceAgentName> updateableFileOrderSourceAgentNames;
    
    public SignedItemsSpec (JocKeyPair keyPair,
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations, 
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedDeployables,
            Set<UpdateableWorkflowJobAgentName> updateableWorkflowJobAgentNames, 
            Set<UpdateableFileOrderSourceAgentName> updateableFileOrderSourceAgentNames) {
        this.keyPair = keyPair;
        this.verifiedConfigurations = verifiedConfigurations;
        this.verifiedDeployables = verifiedDeployables;
        this.updateableWorkflowJobAgentNames = updateableWorkflowJobAgentNames;
        this.updateableFileOrderSourceAgentNames = updateableFileOrderSourceAgentNames;
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

}
