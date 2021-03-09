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
    Set<UpdateableWorkflowJobAgentName> updateableAgentNames;
    Set<DbItemConfWithOriginalContent> unmodified;
    
    public SignedItemsSpec (JocKeyPair keyPair,
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> verifiedConfigurations, 
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedDeployables,
            Set<UpdateableWorkflowJobAgentName> updateableAgentNames,
            Set<DbItemConfWithOriginalContent> unmodified) {
        this.keyPair = keyPair;
        this.verifiedConfigurations = verifiedConfigurations;
        this.verifiedDeployables = verifiedDeployables;
        this.updateableAgentNames = updateableAgentNames;
        this.unmodified = unmodified;
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

    
    public Set<UpdateableWorkflowJobAgentName> getUpdateableAgentNames() {
        return updateableAgentNames;
    }

    
    public Set<DbItemConfWithOriginalContent> getUnmodified() {
        return unmodified;
    }
    
}
