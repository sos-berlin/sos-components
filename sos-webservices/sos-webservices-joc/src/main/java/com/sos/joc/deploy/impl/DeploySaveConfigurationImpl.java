package com.sos.joc.deploy.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.inventory.DBItemJSObject;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.deploy.mapper.JSObjectDBItemMapper;
import com.sos.joc.deploy.resource.IDeploySaveConfigurationResource;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.deploy.DeployFilter;
import com.sos.joc.model.deploy.JSObject;
import com.sos.joc.model.deploy.JSObjects;

@Path("deploy")
public class DeploySaveConfigurationImpl extends JOCResourceImpl implements IDeploySaveConfigurationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploySaveConfigurationImpl.class);
    private static final String API_CALL = "./deploy/save";

	@Override
	public JOCDefaultResponse postDeploySaveConfiguration(String xAccessToken, DeployFilter filter) throws Exception {
		// TODO Auto-generated method stub
		SOSHibernateSession connection = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, filter, xAccessToken, filter.getJobschedulerId(),
                    /* getPermissonsJocCockpit(filter.getJobschedulerId(), xAccessToken).getJobChain().getView().isStatus() */
                    true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            List<DBItemJSObject> objectsToSave = new ArrayList<DBItemJSObject>(); 
            
            JSObjects jsObjects = new JSObjects();
            jsObjects.setJsObjects(filter.getJsObjects());
            jsObjects.setDeliveryDate(Date.from(Instant.now()));
            
            for (JSObject jsObject : jsObjects.getJsObjects()) {
            	objectsToSave.add(JSObjectDBItemMapper.mapJsObjectToDBitem(jsObject));
            }
        	for (DBItemJSObject dbItem : objectsToSave) {
        		if (dbItem.getId() != null) {
        			connection.update(dbItem);
        		} else {
        			connection.save(dbItem);
        		}
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
