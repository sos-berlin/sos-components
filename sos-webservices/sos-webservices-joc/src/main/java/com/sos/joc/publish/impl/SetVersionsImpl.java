package com.sos.joc.publish.impl;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.deployment.DBItemDepVersions;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.publish.DeploymentVersion;
import com.sos.joc.model.publish.SetVersionsFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.ISetVersions;
import com.sos.schema.JsonValidator;

@Path("inventory/deployment")
public class SetVersionsImpl extends JOCResourceImpl implements ISetVersions {

    private static final String API_CALL = "./inventory/deployment/set_versions";

    @Override
    public JOCDefaultResponse postSetVersion(String xAccessToken, byte[] setVersionsFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, setVersionsFilter, xAccessToken);
            JsonValidator.validateFailFast(setVersionsFilter, SetVersionsFilter.class);
            SetVersionsFilter filter = Globals.objectMapper.readValue(setVersionsFilter, SetVersionsFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(xAccessToken).map(p -> p.getInventory().getDeploy()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            storeAuditLog(filter.getAuditLog(), CategoryType.DEPLOYMENT);
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

    private void updateVersions(SetVersionsFilter filter, DBLayerDeploy dbLayer) {
        Map<String, String> versionWithPaths = new HashMap<String, String>();
        List<DBItemDeploymentHistory> depHistoryItems = dbLayer.getFilteredDeployments(filter);
        depHistoryItems.stream().forEach(item -> {
            DBItemDepVersions newVersion = new DBItemDepVersions();
            newVersion.setInvConfigurationId(item.getInventoryConfigurationId());
            DeploymentVersion version = filter.getDeployConfigurations().stream().filter(versionItem -> versionItem.getConfiguration().getPath()
                    .equals(item.getPath())).collect(Collectors.toList()).get(0);
            versionWithPaths.put(version.getVersion(), version.getConfiguration().getPath());
            newVersion.setDepHistoryId(item.getId());
            newVersion.setVersion(version.getVersion());
            newVersion.setModified(Date.from(Instant.now()));
            try {
                dbLayer.getSession().save(newVersion);
            } catch (SOSHibernateException e) {
                throw new JocSosHibernateException(e);
            }
        });
    }

}
