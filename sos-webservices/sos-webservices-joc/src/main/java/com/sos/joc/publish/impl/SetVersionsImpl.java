package com.sos.joc.publish.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.deployment.DBItemInventoryConfiguration;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.publish.JSObjectPathVersion;
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
                    /*getPermissonsJocCockpit("", xAccessToken).getPublish().isSetVersion()*/
                    true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            List<DBItemInventoryConfiguration> drafts = dbLayer.getFilteredInventoryConfigurations(getPathListFromFilter(filter));
            updateVersions(drafts, filter, hibernateSession);
            // TODO: clone these objects to a versionized Table 
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

    private List<String> getPathListFromFilter (SetVersionsFilter filter) {
        List<String> paths = new ArrayList<String>();
        for (JSObjectPathVersion jsObject : filter.getJsObjects()) {
            paths.add(jsObject.getPath());
        }
        return paths;
    }
    
    private void updateVersions(List<DBItemInventoryConfiguration> drafts, SetVersionsFilter filter, SOSHibernateSession session) throws SOSHibernateException {
        for(DBItemInventoryConfiguration draft : drafts) {
            String oldVersion = draft.getVersion();
            for(JSObjectPathVersion objectFromFilter : filter.getJsObjects()) {
                if (objectFromFilter.getPath().equals(draft.getPath())) {
                    draft.setVersion(objectFromFilter.getVersion());
                    draft.setParentVersion(oldVersion);
                    session.update(draft);
                    break;
                } else {
                    continue;
                }
            }
        }
    }
}
