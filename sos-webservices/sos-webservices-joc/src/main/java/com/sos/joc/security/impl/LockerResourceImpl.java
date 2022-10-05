package com.sos.joc.security.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthLockerHandler;
import com.sos.auth.classes.SOSLocker;
import com.sos.inventory.model.common.Variables;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.security.locker.Locker;
import com.sos.joc.model.security.locker.LockerFilter;
import com.sos.joc.security.classes.SecurityHelper;
import com.sos.joc.security.resource.ILockerResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("iam")
public class LockerResourceImpl extends JOCResourceImpl implements ILockerResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockerResourceImpl.class);

    private static final String API_CALL_LOCKER_GET = "./iam/locker/get";
    private static final String API_CALL_LOCKER_PUT = "./iam/locker/put";
    private static final String API_CALL_LOCKER_RENEW = "./iam/locker/renew";
    private static final String API_CALL_LOCKER_REMOVE = "./iam/locker/remove";

    @Override
    public JOCDefaultResponse postLockerGet(byte[] body) {
        try {
            initLogging(API_CALL_LOCKER_GET, body);
            LockerFilter lockerFilter = Globals.objectMapper.readValue(body, LockerFilter.class);
            JsonValidator.validateFailFast(body, LockerFilter.class);

            Locker locker = new Locker();
            locker.setKey(lockerFilter.getKey());

            SOSLocker sosLocker = Globals.jocWebserviceDataContainer.getSOSLocker();

            if (sosLocker.isEmpty(locker.getKey())) {
                throw new JocObjectNotExistException("Locker for key " + locker.getKey() + " is empty");
            }

            Map<String, Object> content = sosLocker.getContent(lockerFilter.getKey());
            if (content != null) {
                locker.setContent(new Variables());
                locker.getContent().setAdditionalProperties(content);
            }
            sosLocker.removeContent(lockerFilter.getKey());
            refreshTimer();
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(locker));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse postLockerPut(byte[] body) {
        try {
            initLogging(API_CALL_LOCKER_PUT, body);
            Locker locker = Globals.objectMapper.readValue(body, Locker.class);
            JsonValidator.validateFailFast(body, Locker.class);
            SOSLocker sosLocker = Globals.jocWebserviceDataContainer.getSOSLocker();
            if (SecurityHelper.getCountAccounts() * 3 < sosLocker.getCount()) {
                throw new JocObjectNotExistException("No more lockers availabe. Maximum reached");

            }

            String key = sosLocker.addContent(locker.getContent().getAdditionalProperties());
            locker.setKey(key);
            locker.setContent(null);
            refreshTimer();
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(locker));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse postLockerRenew(byte[] body) {
        try {

            initLogging(API_CALL_LOCKER_RENEW, body);
            LockerFilter lockerFilter = Globals.objectMapper.readValue(body, LockerFilter.class);
            JsonValidator.validateFailFast(body, Locker.class);
            Globals.jocWebserviceDataContainer.getSOSLocker().renewContent(lockerFilter.getKey());
            refreshTimer();
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(lockerFilter));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse postLockerRemove(byte[] body) {
        try {

            initLogging(API_CALL_LOCKER_REMOVE, body);
            LockerFilter lockerFilter = Globals.objectMapper.readValue(body, LockerFilter.class);
            JsonValidator.validateFailFast(body, Locker.class);
            Globals.jocWebserviceDataContainer.getSOSLocker().removeContent(lockerFilter.getKey());
            refreshTimer();
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(lockerFilter));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private void refreshTimer() {
        if (Globals.jocWebserviceDataContainer.getSosAuthLockerHandler() == null) {
            Globals.jocWebserviceDataContainer.setSosAuthLockerHandler(new SOSAuthLockerHandler());
        }
        Globals.jocWebserviceDataContainer.getSosAuthLockerHandler().start();

    }

}