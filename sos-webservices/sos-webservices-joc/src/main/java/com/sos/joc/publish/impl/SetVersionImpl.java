package com.sos.joc.publish.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.db.inventory.deprecated.DBItemInventoryConfiguration;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.publish.SetVersionFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.ISetVersion;

@Path("publish")
public class SetVersionImpl extends JOCResourceImpl implements ISetVersion {

    private static final String API_CALL = "./publish/set_version";

    @Override
    public JOCDefaultResponse postSetVersion(String xAccessToken, SetVersionFilter filter) throws Exception {
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
            List<DBItemInventoryConfiguration> drafts = dbLayer.getFilteredInventoryConfigurationsForSetVersion(filter);
            // TOREVIEW: should this be better atomic? update all db items or none?
            for(DBItemInventoryConfiguration draft : drafts) {
                String oldVersion = draft.getVersion();
                draft.setVersion(filter.getVersion());
                draft.setParentVersion(oldVersion);
                hibernateSession.update(draft);
            }
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

}
