package com.sos.joc.classes;
 

import com.sos.auth.classes.SOSAuthAccessTokenHandler;
import com.sos.auth.classes.SOSAuthCurrentAccountsList;
 

public final class JocWebserviceDataContainer {
    private static JocWebserviceDataContainer instance;
    
    private static SOSAuthAccessTokenHandler sosAuthAccessTokenHandler;
    private static SOSAuthCurrentAccountsList currentAccountsList;
    

    private JocWebserviceDataContainer() {
        sosAuthAccessTokenHandler = new SOSAuthAccessTokenHandler();
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

    public  void setCurrentAccountsList(SOSAuthCurrentAccountsList currentAccountsList) {
        JocWebserviceDataContainer.currentAccountsList = currentAccountsList;
    }

    
    public SOSAuthAccessTokenHandler getSosAuthAccessTokenHandler() {
        return sosAuthAccessTokenHandler;
    }

    
    public void setSosAuthAccessTokenHandler(SOSAuthAccessTokenHandler sosAuthAccessTokenHandler) {
        JocWebserviceDataContainer.sosAuthAccessTokenHandler = sosAuthAccessTokenHandler;
    }

   
}

   
