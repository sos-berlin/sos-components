package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateQueryNonUniqueResultException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.inventory.resource.IGetPathResource;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.common.Err420;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.path.PathFilter;
import com.sos.joc.model.inventory.path.PathResponse;
import com.sos.schema.JsonValidator;


@Path(JocInventory.APPLICATION_PATH)
public class GetPathResourceImpl extends JOCResourceImpl implements IGetPathResource {

    private static final String API_CALL = "./inventory/path";
    private InventoryDBLayer dbLayer = null;
    
    @Override
    public JOCDefaultResponse postGetPath(String xAccessToken, byte[] body) {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(JocInventory.getResourceImplPath("path"), body, xAccessToken);
            JsonValidator.validateFailFast(body, PathFilter.class);
            PathFilter filter = Globals.objectMapper.readValue(body, PathFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", 
                    getPermissonsJocCockpit("", xAccessToken).getInventory().getConfigurations().isEdit());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            dbLayer = new InventoryDBLayer(hibernateSession);
            PathResponse response = new PathResponse();
            String path = null;
            try {
                if (filter.getUseDrafts()) {
                    path = dbLayer.getPathByNameFromInvConfigurations(filter.getName(), ConfigurationType.fromValue(filter.getObjectType()));
                } else {
                    switch(ConfigurationType.fromValue(filter.getObjectType())) {
                        case WORKFLOW:
                        case JOBCLASS:
                        case JUNCTION:
                        case FILEORDERSOURCE:
                        case LOCK:
                            path = dbLayer.getPathByNameFromLatestActiveDepHistoryItem(filter.getName(), ConfigurationType.fromValue(filter.getObjectType()));
                            break;
                        case NONWORKINGDAYSCALENDAR:
                        case SCHEDULE:
                        case WORKINGDAYSCALENDAR:
                            path = dbLayer.getPathByNameFromInvReleasedConfigurations(filter.getName(), ConfigurationType.fromValue(filter.getObjectType()));
                            break;
                        case FOLDER:
                        case JOB:
                            throw new JocNotImplementedException();                
                    }
                }
            } catch (Exception e) {
                if (e instanceof JocNotImplementedException) {
                    throw e;
                } else if (e instanceof SOSHibernateQueryNonUniqueResultException) {
                    throw new JocSosHibernateException(
                            String.format("Could not determine path for name %1$s, multiple objects found in database.", filter.getName()), e);
                } else if (e.getCause() instanceof NullPointerException) {
                    throw new JocSosHibernateException(String.format("Could not determine path for name %1$s, no object found in database.", filter.getName()), e);
                } 
            }
            if (path == null) {
                Err420 error = new Err420();
                Err err = new Err();
                err.setMessage(String.format("Could not determine path for name %1$s, no object found in database.", filter.getName()));
                error.setError(err);
                return JOCDefaultResponse.responseStatus420(error);
            } else {
                response.setPath(path);
            }
            response.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(response);
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
