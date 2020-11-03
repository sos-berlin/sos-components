package com.sos.joc.publish.impl;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.SetVersionAudit;
import com.sos.joc.db.deployment.DBItemDepVersions;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.publish.SetVersionFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.ISetVersion;
import com.sos.schema.JsonValidator;

@Path("publish")
public class SetVersionImpl extends JOCResourceImpl implements ISetVersion {

    private static final String API_CALL = "./publish/set_version";

    @Override
    public JOCDefaultResponse postSetVersion(String xAccessToken, byte[] setVersionFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, setVersionFilter, xAccessToken);
            JsonValidator.validateFailFast(setVersionFilter, SetVersionFilter.class);
            SetVersionFilter filter = Globals.objectMapper.readValue(setVersionFilter, SetVersionFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, 
                    getPermissonsJocCockpit("", xAccessToken).getInventory().getConfigurations().getPublish().isSetVersion());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            updateVersions(filter, dbLayer);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

    private void updateVersions(SetVersionFilter filter, DBLayerDeploy dbLayer) throws SOSHibernateException {
        Set<String> paths = new HashSet<String>();
        for (Long deploymentId : filter.getDeployments()) {
            DBItemDeploymentHistory depHistoryItem = dbLayer.getSession().get(DBItemDeploymentHistory.class, deploymentId);
            DBItemDepVersions newVersion = new DBItemDepVersions();
            if (depHistoryItem != null) {
                newVersion.setInvConfigurationId(depHistoryItem.getInventoryConfigurationId());
                paths.add(depHistoryItem.getPath());
            }
            newVersion.setDepHistoryId(deploymentId);
            newVersion.setVersion(filter.getVersion());
            newVersion.setModified(Date.from(Instant.now()));
            dbLayer.getSession().save(newVersion);
        }
        SetVersionAudit audit = new SetVersionAudit(filter, paths, "version updated.");
        logAuditMessage(audit);
        storeAuditLogEntry(audit);
    }
    
}