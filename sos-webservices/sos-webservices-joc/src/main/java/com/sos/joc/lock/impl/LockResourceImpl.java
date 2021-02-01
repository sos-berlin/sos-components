package com.sos.joc.lock.impl;

import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.lock.resource.ILockResource;
import com.sos.joc.model.lock.Lock;
import com.sos.joc.model.lock.LockFilter;
import com.sos.schema.JsonValidator;

@Path("lock")
public class LockResourceImpl extends JOCResourceImpl implements ILockResource {

    private static final String API_CALL = "./lock";

    @Override
    public JOCDefaultResponse postPermanent(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, LockFilter.class);
            LockFilter filter = Globals.objectMapper.readValue(filterBytes, LockFilter.class);
            JOCDefaultResponse response = initPermissions(filter.getControllerId(), getPermissonsJocCockpit(filter.getControllerId(), accessToken)
                    .getOrder().getView().isStatus());
            if (response != null) {
                return response;
            }
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsString(getLock(filter)));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private Lock getLock(LockFilter filter) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(API_CALL);

            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);
            DeployedContent content = dbLayer.getDeployedInventory(filter.getControllerId(), DeployType.LOCK.intValue(), filter.getLockPath());

            com.sos.inventory.model.lock.Lock lock = null;
            if (content != null && content.getContent() != null && !content.getContent().isEmpty()) {
                lock = Globals.objectMapper.readValue(content.getContent(), com.sos.inventory.model.lock.Lock.class);
            }

            if (lock == null) {
                throw new DBMissingDataException(String.format("Lock '%s' doesn't exist", filter.getLockPath()));
            }

            Lock answer = new Lock();
            answer.setDeliveryDate(new Date());
            answer.setLock(lock);
            return answer;
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    public JOCDefaultResponse postVolatile(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL + "/v", filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, LockFilter.class);
            LockFilter filter = Globals.objectMapper.readValue(filterBytes, LockFilter.class);
            JOCDefaultResponse response = initPermissions(filter.getControllerId(), getPermissonsJocCockpit(filter.getControllerId(), accessToken)
                    .getOrder().getView().isStatus());
            if (response != null) {
                return response;
            }

            // String lockId = JocInventory.pathToName(filter.getLockId());

            return JOCDefaultResponse.responseStatus200(null);

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
