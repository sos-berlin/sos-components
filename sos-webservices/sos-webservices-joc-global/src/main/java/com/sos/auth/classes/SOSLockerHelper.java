package com.sos.auth.classes;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.sos.joc.Globals;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.locker.Locker;

public class SOSLockerHelper {

    public static Locker lockerPut(Locker locker) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {

        SOSLocker sosLocker = Globals.jocWebserviceDataContainer.getSOSLocker();
        if (SOSAuthHelper.getCountAccounts() * 3 < sosLocker.getCount()) {
            throw new JocException(new JocError("No more lockers availabe. Maximum reached"));
        }
        String key = sosLocker.addContent(locker.getContent().getAdditionalProperties());
        if (sosLocker.getMaximumSizeReached(locker.getContent().getAdditionalProperties())) {
            sosLocker.removeContent(key);
            throw new JocException(new JocError("Size for content is to big"));
        }
        locker.setKey(key);
        locker.setContent(null);
        refreshTimer();
        return locker;
    }

    public static void refreshTimer() {
        if (Globals.jocWebserviceDataContainer.getSosAuthLockerHandler() == null) {
            Globals.jocWebserviceDataContainer.setSosAuthLockerHandler(new SOSAuthLockerHandler());
        }
        Globals.jocWebserviceDataContainer.getSosAuthLockerHandler().start();
    }

}
