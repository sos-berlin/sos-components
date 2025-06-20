package com.sos.joc.publish.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.deployment.DBItemDepVersions;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.publish.SetVersionFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.ISetVersion;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("inventory/deployment")
public class SetVersionImpl extends JOCResourceImpl implements ISetVersion {

    private static final String API_CALL = "./inventory/deployment/set_version";

    @Override
    public JOCDefaultResponse postSetVersion(String xAccessToken, byte[] setVersionFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            setVersionFilter = initLogging(API_CALL, setVersionFilter, xAccessToken, CategoryType.DEPLOYMENT);
            JsonValidator.validateFailFast(setVersionFilter, SetVersionFilter.class);
            SetVersionFilter filter = Globals.objectMapper.readValue(setVersionFilter, SetVersionFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(xAccessToken).map(p -> p.getInventory().getDeploy()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            storeAuditLog(filter.getAuditLog());
            updateVersions(filter, dbLayer);
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

    private void updateVersions(SetVersionFilter filter, DBLayerDeploy dbLayer) throws SOSHibernateException {
        List<DBItemDeploymentHistory> depHistoryItems = dbLayer.getFilteredDeployments(filter);
        depHistoryItems.stream().forEach(item -> {
            DBItemDepVersions newVersion = new DBItemDepVersions();
            newVersion.setInvConfigurationId(item.getInventoryConfigurationId());
            newVersion.setDepHistoryId(item.getId());
            newVersion.setVersion(filter.getVersion());
            newVersion.setModified(Date.from(Instant.now()));
            try {
                dbLayer.getSession().save(newVersion);
            } catch (SOSHibernateException e) {
                throw new JocSosHibernateException(e.getMessage(), e);
            }
        });
    }
    
}