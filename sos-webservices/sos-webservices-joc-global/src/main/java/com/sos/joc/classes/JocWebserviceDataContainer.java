package com.sos.joc.classes;
 

import com.sos.auth.classes.SOSAuthCurrentAccountsList;
 

public final class JocWebserviceDataContainer {
    private static JocWebserviceDataContainer instance;
    
    public static SOSAuthCurrentAccountsList currentAccountsList;
    

    private JocWebserviceDataContainer() {

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

   
}

   
