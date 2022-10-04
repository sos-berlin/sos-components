package com.sos.joc.classes;
 

import com.sos.auth.classes.SOSAuthAccessTokenHandler;
import com.sos.auth.classes.SOSAuthCurrentAccountsList;
import com.sos.auth.classes.SOSAuthLockerHandler;
import com.sos.auth.classes.SOSLocker;
 

public final class JocWebserviceDataContainer {
    private static JocWebserviceDataContainer instance;
    
    private static SOSAuthLockerHandler sosAuthLockerHandler;
    private static SOSAuthAccessTokenHandler sosAuthAccessTokenHandler;
    private static SOSAuthCurrentAccountsList currentAccountsList;
    private static SOSLocker sosLocker;
    

    private JocWebserviceDataContainer() {
        sosAuthAccessTokenHandler = new SOSAuthAccessTokenHandler();
        sosAuthLockerHandler = new SOSAuthLockerHandler();
    }

    public static synchronized JocWebserviceDataContainer getInstance() {
        if (instance == null) {
            instance = new JocWebserviceDataContainer();
        }
        return instance;
    }

    public  SOSAuthCurrentAccountsList getCurrentAccountsList() {
        return currentAccountsList;
    }

    public  SOSLocker getSOSLocker() {
        if (sosLocker == null) {
            sosLocker = new SOSLocker();
        }
        return sosLocker;
    }
    
    public  void setCurrentAccountsList(SOSAuthCurrentAccountsList currentAccountsList) {
        JocWebserviceDataContainer.currentAccountsList = currentAccountsList;
    }

    
    public SOSAuthAccessTokenHandler getSosAuthAccessTokenHandler() {
        return sosAuthAccessTokenHandler;
    }

    
    public void setSosAuthAccessTokenHandler(SOSAuthAccessTokenHandler sosAuthAccessTokenHandler) {
        JocWebserviceDataContainer.sosAuthAccessTokenHandler = sosAuthAccessTokenHandler;
    }

    public SOSAuthLockerHandler getSosAuthLockerHandler() {
        return sosAuthLockerHandler;
    }

    
    public void setSosAuthLockerHandler(SOSAuthLockerHandler sosAuthLockerHandler) {
        JocWebserviceDataContainer.sosAuthLockerHandler = sosAuthLockerHandler;
    }

   
}

   
