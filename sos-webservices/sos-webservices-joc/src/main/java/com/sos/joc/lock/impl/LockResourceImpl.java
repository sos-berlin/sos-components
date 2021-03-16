package com.sos.joc.lock.impl;

import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.lock.common.LockEntryHelper;
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
            DeployedContent dc = dbLayer.getDeployedInventory(filter.getControllerId(), DeployType.LOCK.intValue(), filter.getLockPath());
            Globals.disconnect(session);
            session = null;

            Lock answer = new Lock();
            LockEntryHelper helper = new LockEntryHelper(filter.getControllerId());
            answer.setLock(helper.getLockEntry(Proxy.of(filter.getControllerId()).currentState(), dc, dc.getPath()));
            answer.setDeliveryDate(new Date());
            return answer;
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

}
