package com.sos.auth.classes;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.sos.joc.Globals;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.security.locker.Locker;
import com.sos.joc.model.security.locker.Variables;

public class SOSLockerHelper {

    public static Locker lockerPut(Locker locker) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {

        SOSLocker sosLocker = Globals.jocWebserviceDataContainer.getSOSLocker();
        String key = sosLocker.addContent(locker.getContent().getAdditionalProperties());
        if (sosLocker.getMaximumSizeReached(locker.getContent().getAdditionalProperties())) {
            sosLocker.removeContent(key);
            throw new JocException(new JocError("Size of locker content is too big"));
        }
        locker.setKey(key);
        locker.setContent(null);
        refreshTimer();
        return locker;
    }

    public static Locker lockerGet(String lockerKey) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        Locker locker = new Locker();
        locker.setKey(lockerKey);

        SOSLocker sosLocker = Globals.jocWebserviceDataContainer.getSOSLocker();

        if (sosLocker.isEmpty(lockerKey)) {
            throw new JocObjectNotExistException("Locker for key " + locker.getKey() + " is empty");
        }

        Map<String, String> content = sosLocker.getContent(lockerKey);
        if (content != null) {
            locker.setContent(new Variables());
            locker.getContent().setAdditionalProperties(content);
        }
        sosLocker.removeContent(lockerKey);
        SOSLockerHelper.refreshTimer();
        return locker;
    }
    
    public static void refreshTimer() {
        if (Globals.jocWebserviceDataContainer.getSosAuthLockerHandler() == null) {
            Globals.jocWebserviceDataContainer.setSosAuthLockerHandler(new SOSAuthLockerHandler());
        }
        Globals.jocWebserviceDataContainer.getSosAuthLockerHandler().start();
    }

}
