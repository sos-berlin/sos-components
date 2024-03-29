package com.sos.joc.classes;


import com.sos.joc.Globals;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class ServletContextClass implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        try {
            Globals.getHibernateFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        if (Globals.sosHibernateFactory != null) {
//            if (Globals.sosHibernateFactory.dbmsIsH2()) {
//                SOSHibernateSession connection = null;
//                try {
//                    connection = Globals.createSosHibernateStatelessConnection("closeH2");
//                    connection.createQuery("SHUTDOWN").executeUpdate();
//                } catch (Exception e) {
//                    //
//                } finally {
//                    Globals.disconnect(connection);
//                }
//            }
            Globals.sosHibernateFactory.close();
        }
    }

}
