package com.sos.joc.monitoring.notification.notifier;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.monitoring.configuration.monitor.jms.MonitorJMS;
import com.sos.joc.monitoring.configuration.monitor.jms.ObjectHelper;
import com.sos.joc.monitoring.db.DBLayerMonitoring;
import com.sos.joc.monitoring.exception.SOSNotifierSendException;

public class NotifierJMS extends ANotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifierJMS.class);

    private final MonitorJMS monitor;
    private Connection connection = null;
    private Session session = null;
    private String url4log;
    private String userName;
    private String password;

    public NotifierJMS(MonitorJMS monitor) {
        this.monitor = monitor;
    }

    public void init() throws Exception {
        createConnection();
    }

    @Override
    public void close() {
        closeConnection();
    }

    @Override
    public void notify(DBLayerMonitoring dbLayer, DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, ServiceStatus status,
            ServiceMessagePrefix prefix) throws SOSNotifierSendException {

        MessageProducer producer = null;
        try {
            producer = createProducer();
            evaluate(mo, mos, status, prefix);

            String message = resolve(monitor.getMessage(), true);

            producer.setPriority(monitor.getPriority());
            producer.setDeliveryMode(monitor.getDeliveryMode());
            producer.setTimeToLive(monitor.getTimeToLive());

            LOGGER.info(String.format("[%s-%s][jms][execute][destination %s(%s)]%s", getServiceStatus(), getServiceMessagePrefix(), monitor
                    .getDestinationName(), monitor.getDestination(), message));
            producer.send(session.createTextMessage(message));
        } catch (Throwable e) {
            throw new SOSNotifierSendException(String.format("[%s name=\"%s\"]can't send notification", monitor.getRefElementName(), monitor
                    .getMonitorName()), e);
        } finally {
            if (producer != null) {
                try {
                    producer.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private void createConnection() throws Exception {

        ConnectionFactory factory = createFactory();
        try {
            if (SOSString.isEmpty(userName)) {
                connection = factory.createConnection();
            } else {
                connection = factory.createConnection(userName, password);
            }
            if (!SOSString.isEmpty(monitor.getClientId())) {
                connection.setClientID(monitor.getClientId());
            }
            connection.start();
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][exception occurred while trying to connect]%s", url4log, e.toString()), e);
            throw e;

        }
        try {
            session = connection.createSession(false, monitor.getAcknowledgeMode());
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][exception occurred while trying to create Session]%s", url4log, e.toString()), e);
            throw e;
        }
    }

    private ConnectionFactory createFactory() throws Exception {

        if (monitor.getConnectionFactory() != null) {
            try {
                userName = monitor.getConnectionFactory().getUserName();
                password = monitor.getConnectionFactory().getPassword();
                // TODO
                url4log = "";

                return (ConnectionFactory) ObjectHelper.newInstance(monitor.getConnectionFactory().getJavaClass(), monitor.getConnectionFactory()
                        .getConstructorArguments());
            } catch (Throwable e) {
                LOGGER.error(String.format("can't initialize ConnectionFactory[class=%s]: %s", monitor.getConnectionFactory().getJavaClass(), e
                        .toString()), e);
                throw e;
            }
        } else if (monitor.getConnectionJNDI() != null) {
            try {
                Properties env = loadJndiFile(monitor.getConnectionJNDI().getFile());
                if (env != null) {
                    url4log = env.getProperty(Context.PROVIDER_URL);
                    userName = env.getProperty(Context.SECURITY_PRINCIPAL);
                    password = env.getProperty(Context.SECURITY_CREDENTIALS);
                }
                Context jndi = new InitialContext(env);
                return (ConnectionFactory) jndi.lookup(monitor.getConnectionJNDI().getLookupName());
            } catch (Throwable e) {
                LOGGER.error(String.format("can't initialize ConnectionFactory[jndi file=%s, lookupName=%s]: %s", monitor.getConnectionJNDI()
                        .getFile(), monitor.getConnectionJNDI().getLookupName(), e.toString()), e);
                throw e;
            }
        } else {
            throw new Exception(String.format("can't initialize ConnectionFactory: connection element not found (%s or %s)",
                    MonitorJMS.ELEMENT_NAME_CONNECTION_FACTORY, MonitorJMS.ELEMENT_NAME_CONNECTION_JNDI));
        }
    }

    private Properties loadJndiFile(String fileName) throws Exception {
        InputStream is = null;
        try {
            Properties p = new Properties();
            is = new FileInputStream(fileName);
            p.load(is);
            return p;
        } catch (Throwable e) {
            throw new Exception(String.format("can't load jndi file=%s: %s", fileName, e.toString()), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private void closeConnection() {
        if (session != null) {
            try {
                session.close();
            } catch (Throwable e) {
            }
        }
        if (connection != null) {
            try {
                connection.stop();
            } catch (Throwable e) {
            }
            try {
                connection.close();
            } catch (Throwable e) {
            }
        }
        session = null;
        connection = null;
    }

    private MessageProducer createProducer() throws Exception {
        Destination destination = null;
        String name = monitor.getDestinationName();
        try {
            name = normalizeDestinationName(name);
            if (monitor.isQueueDestination()) {
                destination = session.createQueue(name);
            } else {
                destination = session.createTopic(name);
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][%s][%s]exception occurred while trying to create Destination: %s", url4log, monitor.getDestination(),
                    name, e.toString()), e);
            throw e;
        }
        try {
            return session.createProducer(destination);
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][%s][%s]exception occurred while trying to create MessageProducer: %s", url4log, monitor.getDestination(),
                    name, e.toString()), e);
            throw e;
        }
    }

    private String normalizeDestinationName(String name) {
        name = name.replaceAll("&amp;", "&");
        return name;
    }

}
