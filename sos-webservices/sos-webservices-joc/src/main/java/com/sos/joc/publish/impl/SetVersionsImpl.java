package com.sos.joc.publish.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.deployment.DBItemDepVersions;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.publish.ConfigurationVersion;
import com.sos.joc.model.publish.DeploymentVersion;
import com.sos.joc.model.publish.SetVersionsFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.ISetVersions;

@Path("publish")
public class SetVersionsImpl extends JOCResourceImpl implements ISetVersions {

    private static final String API_CALL = "./publish/set_versions";

    @Override
    public JOCDefaultResponse postSetVersion(String xAccessToken, SetVersionsFilter filter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, filter, xAccessToken, "", 
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

    private void updateVersions(SetVersionsFilter filter, DBLayerDeploy dbLayer) throws SOSHibernateException {
        for (ConfigurationVersion configurationWithVersion : filter.getConfigurations()) {
            DBItemDepVersions newVersion = new DBItemDepVersions();
            newVersion.setInvConfigurationId(configurationWithVersion.getConfigurationId());
            newVersion.setVersion(configurationWithVersion.getVersion());
            newVersion.setModified(Date.from(Instant.now()));
            dbLayer.getSession().save(newVersion);
        }
        for (DeploymentVersion deploymentWithVersion : filter.getDeployments()) {
            DBItemDepVersions newVersion = new DBItemDepVersions();
            newVersion.setInvConfigurationId(deploymentWithVersion.getDeploymentId());
            newVersion.setVersion(deploymentWithVersion.getVersion());
            newVersion.setModified(Date.from(Instant.now()));
            dbLayer.getSession().save(newVersion);
        }
    }
    
}
