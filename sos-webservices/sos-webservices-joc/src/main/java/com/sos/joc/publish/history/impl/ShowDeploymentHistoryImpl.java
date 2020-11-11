package com.sos.joc.publish.history.impl;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.DBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrderHistoryFilter;
import com.sos.joc.model.publish.DepHistory;
import com.sos.joc.model.publish.ShowDepHistoryFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.history.resource.IShowDeploymentHistory;
import com.sos.schema.JsonValidator;

@Path("publish")
public class ShowDeploymentHistoryImpl extends JOCResourceImpl implements IShowDeploymentHistory {

    private static final String API_CALL = "./publish/show_dep_history";
    private static final Logger LOGGER = LoggerFactory.getLogger(ShowDeploymentHistoryImpl.class);

    @Override
    public JOCDefaultResponse postShowDeploymentHistory(String xAccessToken, byte[] showDepHistoryFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, showDepHistoryFilter, xAccessToken);
            JsonValidator.validateFailFast(showDepHistoryFilter, ShowDepHistoryFilter.class);
            ShowDepHistoryFilter filter = Globals.objectMapper.readValue(showDepHistoryFilter, ShowDepHistoryFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getPermissonsJocCockpit(null, xAccessToken).getHistory().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            DepHistory depHistory = new DepHistory();
            return JOCDefaultResponse.responseStatus200("");
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
             if (hibernateSession != null) {
                 hibernateSession.close();
             }
        }
    }

}
