package com.sos.joc.deploy.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.inventory.DBItemJSObject;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.deploy.mapper.JSObjectDBItemMapper;
import com.sos.joc.deploy.mapper.UpDownloadMapper;
import com.sos.joc.deploy.resource.IDeploySaveConfigurationResource;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.deploy.JSObject;

@Path("deploy")
public class DeploySaveConfigurationImpl extends JOCResourceImpl implements IDeploySaveConfigurationResource {

    private static final String API_CALL = "./deploy/save";

	@Override
	public JOCDefaultResponse postDeploySaveConfiguration(String xAccessToken, final byte[] jsObj) throws Exception {
		
		JSObject jsObject = UpDownloadMapper.initiateObjectMapper().readValue(jsObj, JSObject.class);
		SOSHibernateSession connection = null;
        try {
        	// TODO: set correct permissions when exist
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, jsObject, xAccessToken, jsObject.getJobschedulerId(),
                    /* getPermissonsJocCockpit(filter.getJobschedulerId(), xAccessToken).getJobChain().getView().isStatus() */
                    true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBItemJSObject objectToSave = new DBItemJSObject(); 
            
//            jsObject.setDeliveryDate(Date.from(Instant.now()));
    		jsObject.setEditAccount(getAccount());
    		jsObject.setModified(Date.from(Instant.now()));
        	objectToSave = JSObjectDBItemMapper.mapJsObjectToDBitem(jsObject);
//            }
    		if (objectToSave.getId() != null) {
    			connection.update(objectToSave);
    		} else {
    			connection.save(objectToSave);
    		}
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
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
