package com.sos.joc.lock.impl;

import java.time.Instant;
import java.util.Date;

import jakarta.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.lock.common.LockEntryHelper;
import com.sos.joc.lock.resource.ILockResource;
import com.sos.joc.model.lock.Lock;
import com.sos.joc.model.lock.LockFilter;
import com.sos.schema.JsonValidator;

import js7.data_for_java.controller.JControllerState;

@Path("lock")
public class LockResourceImpl extends JOCResourceImpl implements ILockResource {

    private static final String API_CALL = "./lock";
    private static final Logger LOGGER = LoggerFactory.getLogger(LockResourceImpl.class);

    @Override
    public JOCDefaultResponse postPermanent(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, LockFilter.class);
            LockFilter filter = Globals.objectMapper.readValue(filterBytes, LockFilter.class);
            JOCDefaultResponse response = initPermissions(filter.getControllerId(), getControllerPermissions(filter.getControllerId(), accessToken)
                    .getLocks().getView());
            if (response != null) {
                return response;
            }
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(getLock(filter)));
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
            Lock answer = new Lock();
            answer.setSurveyDate(Date.from(Instant.now()));
            final JControllerState currentstate = getCurrentState(filter.getControllerId());
            if (currentstate != null) {
                answer.setSurveyDate(Date.from(currentstate.instant()));
            }
            
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);
            DeployedContent dc = dbLayer.getDeployedInventory(filter.getControllerId(), DeployType.LOCK.intValue(), filter.getLockPath());
            Globals.disconnect(session);
            session = null;
            
            if (dc == null || dc.getContent() == null || dc.getContent().isEmpty()) {
                throw new DBMissingDataException(String.format("Lock '%s' doesn't exist", filter.getLockPath()));
            }
            checkFolderPermissions(dc.getPath());
            
            LockEntryHelper helper = new LockEntryHelper(filter.getControllerId(), filter.getCompact(), filter.getLimit());
            answer.setLock(helper.getLockEntry(currentstate, dc));
            answer.setDeliveryDate(Date.from(Instant.now()));
            return answer;
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private JControllerState getCurrentState(String controllerId) {
        JControllerState currentstate = null;
        try {
            currentstate = Proxy.of(controllerId).currentState();
        } catch (Exception e) {
            LOGGER.warn(e.toString());
        }
        return currentstate;
    }

}
