package com.sos.joc.publish.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.publish.DeployFilter;
import com.sos.joc.publish.resource.IDeploy;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("inventory/deployment")
public class DeployImpl extends ADeploy implements IDeploy {
    
    public static final JocSecurityLevel SEC_LVL = JocSecurityLevel.MEDIUM; 
    
    @Override
    public JOCDefaultResponse postDeploy(String xAccessToken, byte[] filter) {
        return postDeploy(xAccessToken, filter, false);
    }

    public JOCDefaultResponse postDeploy(String xAccessToken, byte[] filter, boolean withoutFolderDeletion) {
        SOSHibernateSession hibernateSession = null;
        try {
            filter = initLogging(API_CALL, filter, xAccessToken, CategoryType.DEPLOYMENT);
            JsonValidator.validate(filter, DeployFilter.class);
            DeployFilter deployFilter = Globals.objectMapper.readValue(filter, DeployFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).map(p -> p.getInventory().getDeploy()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            DBItemJocAuditLog dbAuditlog = storeAuditLog(deployFilter.getAuditLog());
            
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            deploy(xAccessToken, deployFilter, hibernateSession, dbAuditlog, SEC_LVL, API_CALL);
            
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }
    
}