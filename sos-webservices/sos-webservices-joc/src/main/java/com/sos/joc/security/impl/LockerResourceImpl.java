package com.sos.joc.security.impl;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSAuthLockerHandler;
import com.sos.auth.classes.SOSLocker;
import com.sos.auth.classes.SOSLockerHelper;
import com.sos.inventory.model.common.Variables;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.security.locker.Locker;
import com.sos.joc.model.security.locker.LockerFilter;
import com.sos.joc.security.resource.ILockerResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("iam")
public class LockerResourceImpl extends JOCResourceImpl implements ILockerResource {

    private static final String API_CALL_LOCKER_GET = "./iam/locker/get";
    private static final String API_CALL_LOCKER_PUT = "./iam/locker/put";
    private static final String API_CALL_LOCKER_RENEW = "./iam/locker/renew";
    private static final String API_CALL_LOCKER_REMOVE = "./iam/locker/remove";

    @Override
    public JOCDefaultResponse postLockerGet(byte[] body) {
        try {
            body = initLogging(API_CALL_LOCKER_GET, body, CategoryType.IDENTITY);
            JsonValidator.validateFailFast(body, LockerFilter.class);
            LockerFilter lockerFilter = Globals.objectMapper.readValue(body, LockerFilter.class);

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
            SOSLockerHelper.refreshTimer();
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(locker));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    @Override
    public JOCDefaultResponse postLockerPut(byte[] body) {
        try {
            body = initLogging(API_CALL_LOCKER_PUT, body, CategoryType.IDENTITY);
            JsonValidator.validateFailFast(body, Locker.class);
            Locker locker = Globals.objectMapper.readValue(body, Locker.class);
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(SOSLockerHelper.lockerPut(locker)));
        } catch (JocException e) {
            return responseStatus434JSError(e);
        } catch (Exception e) {
            return responseStatusJSError(e);
        }

    }
    
    @Override
    public JOCDefaultResponse postLockerRenew(byte[] body) {
        try {

            body = initLogging(API_CALL_LOCKER_RENEW, body, CategoryType.IDENTITY);
            JsonValidator.validateFailFast(body, Locker.class);
            LockerFilter lockerFilter = Globals.objectMapper.readValue(body, LockerFilter.class);
            Globals.jocWebserviceDataContainer.getSOSLocker().renewContent(lockerFilter.getKey());
            SOSLockerHelper.refreshTimer();
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(lockerFilter));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    @Override
    public JOCDefaultResponse postLockerRemove(byte[] body) {
        try {

            body = initLogging(API_CALL_LOCKER_REMOVE, body, CategoryType.IDENTITY);
            JsonValidator.validateFailFast(body, Locker.class);
            LockerFilter lockerFilter = Globals.objectMapper.readValue(body, LockerFilter.class);
            Globals.jocWebserviceDataContainer.getSOSLocker().removeContent(lockerFilter.getKey());
            SOSLockerHelper.refreshTimer();
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(lockerFilter));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}