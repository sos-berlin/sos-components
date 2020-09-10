package com.sos.js7.order.initiator.classes;

import com.sos.js7.order.initiator.OrderInitiatorSettings;

public class OrderInitiatorGlobals {

    public static OrderInitiatorSettings orderInitiatorSettings;

    /* private static final Logger LOGGER = LoggerFactory.getLogger(Globals.class);
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
    */

}
