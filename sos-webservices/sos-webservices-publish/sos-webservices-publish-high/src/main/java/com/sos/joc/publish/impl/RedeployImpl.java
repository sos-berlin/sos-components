package com.sos.joc.publish.impl;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.publish.resource.IRedeploy;

@Path("inventory/deployment")
public class RedeployImpl extends JOCResourceImpl implements IRedeploy {

//    private static final String API_CALL = "./inventory/deployment/redeploy";
//    private static final Logger LOGGER = LoggerFactory.getLogger(RedeployImpl.class);
//    private DBLayerDeploy dbLayer = null;

    @Override
    public JOCDefaultResponse postRedeploy(String xAccessToken, byte[] filter) throws Exception {
//        SOSHibernateSession hibernateSession = null;
        try {
//            initLogging(API_CALL, filter, xAccessToken);
//            JsonValidator.validateFailFast(filter, RedeployFilter.class);
//            RedeployFilter reDeployFilter = Globals.objectMapper.readValue(filter, RedeployFilter.class);
//            
//            JOCDefaultResponse jocDefaultResponse = initPermissions("", 
//                    getPermissonsJocCockpit("", xAccessToken).getInventory().getConfigurations().getPublish().isDeploy());
//            if (jocDefaultResponse != null) {
//                return jocDefaultResponse;
//            }
//            String account = jobschedulerUser.getSosShiroCurrentUser().getUsername();
            //  Use ./inventory/export and ./inventory/deployment/import_deploy instead.
            throw new JocNotImplementedException("The web service is not available for Security Level HIGH.");
//            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
//            dbLayer = new DBLayerDeploy(hibernateSession);
//            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
//            Globals.disconnect(hibernateSession);
        }
    }

}