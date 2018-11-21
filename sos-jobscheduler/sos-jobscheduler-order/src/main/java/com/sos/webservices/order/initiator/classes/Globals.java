package com.sos.webservices.order.initiator.classes;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateOpenSessionException;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.webservices.order.initiator.OrderInitiatorSettings;

public class Globals {

    private static final Logger LOGGER = LoggerFactory.getLogger(Globals.class);
    public static SOSHibernateFactory sosHibernateFactory;
    public static OrderInitiatorSettings orderInitiatorSettings;

    private static SOSHibernateFactory getHibernateFactory() throws JocConfigurationException {
        if (sosHibernateFactory == null) {
            try {
                sosHibernateFactory = new SOSHibernateFactory(orderInitiatorSettings.getHibernateConfigurationFile().toString());
                sosHibernateFactory.addClassMapping(DBLayer.getOrderInitatorClassMapping());
                sosHibernateFactory.addClassMapping(DBLayer.getHistoryClassMapping());
                sosHibernateFactory.build();
            } catch (Exception e) {
                sosHibernateFactory = null;
                throw new JocConfigurationException(e);
            }
        }
        return sosHibernateFactory;
    }

    public static SOSHibernateSession createSosHibernateStatelessConnection(String identifier) throws JocConfigurationException,
            DBConnectionRefusedException {
        if (sosHibernateFactory == null) {
            getHibernateFactory();
        }

        try {
            if (sosHibernateFactory == null) {
                JocError error = new JocError();
                error.setCode("");
                error.setMessage("Could not create sosHibernateFactory");
                throw new JocException(error);
            }
            LOGGER.debug("Create session:" + identifier);
            SOSHibernateSession sosHibernateSession = sosHibernateFactory.openStatelessSession(identifier);
            return sosHibernateSession;
        } catch (SOSHibernateOpenSessionException e) {
            throw new DBConnectionRefusedException(e);
        } catch (JocException ee) {
            throw new DBConnectionRefusedException(ee);
        }
    }

    public static void beginTransaction(SOSHibernateSession connection) {
        try {
            if (connection != null) {
                connection.beginTransaction();
            }
        } catch (Exception e) {
        }
    }

    public static void rollback(SOSHibernateSession connection) {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (Exception e) {
        }
    }

    public static void commit(SOSHibernateSession connection) {
        try {
            if (connection != null) {
                connection.commit();
            }
        } catch (Exception e) {
        }
    }

    public static void disconnect(SOSHibernateSession sosHibernateSession) {
        if (sosHibernateSession != null) {
            sosHibernateSession.close();
        }
    }

    public static String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        return ("/" + path.trim()).replaceAll("//+", "/").replaceFirst("/$", "");
    }

    public static String getParent(String path) {
        Path p = Paths.get(path).getParent();
        if (p == null) {
            return null;
        } else {
            return p.toString().replace('\\', '/');
        }
    }

}
