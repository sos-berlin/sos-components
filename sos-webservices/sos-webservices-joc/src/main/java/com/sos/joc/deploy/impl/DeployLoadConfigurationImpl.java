package com.sos.joc.deploy.impl;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.inventory.DBItemJSObject;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.deploy.DeployDBLayer;
import com.sos.joc.deploy.mapper.JSObjectDBItemMapper;
import com.sos.joc.deploy.resource.IDeployLoadConfigurationResource;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.deploy.DeployLoadFilter;
import com.sos.joc.model.deploy.JSObject;

@Path("deploy")
public class DeployLoadConfigurationImpl extends JOCResourceImpl implements IDeployLoadConfigurationResource {

    private static final String API_CALL = "./deploy/load";

	@Override
	public JOCDefaultResponse postDeployLoadConfiguration(String xAccessToken, DeployLoadFilter filter) throws Exception {
		// TODO Auto-generated method stub
		SOSHibernateSession connection = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, filter, xAccessToken, filter.getJobschedulerId(),
                    /*getPermissonsJocCockpit(filter.getJobschedulerId(), xAccessToken).getJobChain().getView().isStatus()*/
                    true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
        	DeployDBLayer dbLayer = new DeployDBLayer(connection);
        	
            DBItemJSObject dbItemJsObject = dbLayer.getJSObject(filter.getJobschedulerId(), filter.getPath(), filter.getObjectType().value()); 
            JSObject jsObject = JSObjectDBItemMapper.mapDBitemToJsObject(dbItemJsObject);
//            jsObject.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseHtmlStatus200(jsObject);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
	}

}
