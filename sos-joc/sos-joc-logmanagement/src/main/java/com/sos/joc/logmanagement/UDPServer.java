package com.sos.joc.logmanagement;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.networknt.schema.JsonSchema;
import com.sos.joc.Globals;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsLogNotification;
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.event.EventBus;
import com.sos.joc.log4j2.NotificationAppender;
import com.sos.joc.logmanagement.exception.SOSLogManagementException;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.schema.JsonValidator;

public class UDPServer implements Runnable {
    
    protected static JsonSchema schema = null;
    private static final URI validationUri = URI.create("classpath:/raml/api/schemas/logManagement/logEvent-schema.json");
    
    private static final int BUFFER_SIZE = 8192;
    private static final Logger LOGGER = LoggerFactory.getLogger("JOCLogNotification");
    private static final Marker MARKER = MarkerFactory.getMarker("JOCLogNotification");
    private static final Marker NOT_NOTIFY_LOGGER = MarkerFactory.getMarker(WebserviceConstants.NOT_NOTIFY_LOGGER.getName());
    //private static UDPServer instance = null;
    
    private DatagramSocket socket = null;
    private Thread thread = null;
    private boolean running = false;
    private String host = null;
    private int port = ConfigurationGlobalsLogNotification.getDefaultPort();
    private boolean isActive = ConfigurationGlobalsLogNotification.getDefaultIsActive();
    private int maxMessagesPerSecond = ConfigurationGlobalsLogNotification.getDefaultMaxMessagesPerSecond();
    private volatile CopyOnWriteArrayList<EventHandler> logEvents = new CopyOnWriteArrayList<>();
    private final static String threadNamePrefix = "Thread-LogService-";
    private AtomicInteger threadNameSuffix = new AtomicInteger(0);
    private Timer timer = new Timer();
//    private JocClusterHibernateFactory factory;
//    private Path hibernateConfigFile;
    
    protected UDPServer() {
        setConfiguration();
    }
    
    protected UDPServer(ConfigurationGlobalsLogNotification conf) {
        setConfiguration(conf);
    }
    
    protected final void start() {
        if (!this.isActive) {
            JocClusterServiceLogger.setLogger(ClusterServices.cluster.name());
            LOGGER.info(NOT_NOTIFY_LOGGER, "[skipped] The start of Log Management service is skipped due to the settings");
            JocClusterServiceLogger.removeLogger(ClusterServices.cluster.name());
        } else {
            forceStart();
        }
    }
    
    //for Unit Test
    protected final void forceStart() {
        if (this.thread == null) {
            Thread thread = new Thread(this, "Thread-" + UDPServer.class.getSimpleName());

            this.thread = thread;
            thread.start();
        }
    }
    
    private void setConfiguration() {
        setConfiguration(Globals.getConfigurationGlobalsLogNotification()); 
    }

    private void setConfiguration(ConfigurationGlobalsLogNotification conf) {
        JocClusterServiceLogger.setLogger(ClusterServices.lognotification.name());
        if (conf != null) {
            this.port = conf.getPort();
            this.isActive = conf.isActive();
            this.maxMessagesPerSecond = conf.getMaxMessagesPerSecond();
        }
        try {
            schema = JsonValidator.getSchema(validationUri, false, false);
        } catch (Exception e) {
            LOGGER.error(NOT_NOTIFY_LOGGER, "Schema for validating messages not found: " + validationUri);
        }
    }
    
    protected int getPort() {
        return this.port;
    }
    
    protected void stop() {
        this.running = false;
        
        this.sleep(1);
        
        if (this.socket != null && !this.socket.isClosed()) {
            this.socket.close();
            this.sleep(1);
            //System.out.print(" Thread alive? " + this.thread.isAlive());
            this.thread = null;
        }
        if (this.timer != null) {
            this.timer.cancel();
            this.timer.purge();
        }
        
        execFinalLogEvents();
        if (this.isActive) {
            LOGGER.info(NOT_NOTIFY_LOGGER, "UDPServer is stopped");
        }
        JocClusterServiceLogger.removeLogger(ClusterServices.lognotification.name());
    }
    
    private void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            //
        }
    }
    
    private DatagramSocket createDatagramSocket() throws SocketException, UnknownHostException {
        DatagramSocket datagramSocket = null;
        
        if (this.host != null && !this.host.isBlank()) {
            InetAddress inetAddress = InetAddress.getByName(this.host);
            datagramSocket = new DatagramSocket(this.port, inetAddress);
        } else {
            datagramSocket = new DatagramSocket(this.port);
        }
        
        return datagramSocket;
    }
    
    private void execLogEventsThreaded() {
        if (this.logEvents.size() > 0) {
            if (this.logEvents.size() > maxMessagesPerSecond) {
                this.running = false;
                this.logEvents.clear();
                LOGGER.warn(MARKER, "UDP server received more than " + maxMessagesPerSecond
                        + " messages per second. It will be stopped due to the settings.");
                this.stop();
            } else {
                final Stream<EventHandler> eventStream = this.logEvents.stream();
                this.logEvents.clear();
                new Thread(() -> doIt(eventStream), threadNamePrefix + threadNameSuffix.incrementAndGet()).start();
            }
        }
    }

    private void execFinalLogEvents() {
        final Stream<EventHandler> eventStream = this.logEvents.stream();
        this.logEvents.clear();
        doIt(eventStream);
    }
    
    private static void doIt(Stream<EventHandler> eventStream) {
//        eventStream.map(EventHandler::mapToLogEvent).filter(Objects::nonNull).distinct().map(EventHandler::mapLogEventToSystemNotificationLogEvent)
//                .forEach(evt -> EventBus.getInstance().post(evt));
        eventStream.map(EventHandler::mapToLogEvent).filter(Objects::nonNull).distinct().map(EventHandler::mapLogEventToSystemNotificationLogEvent)
                .forEach(evt -> LOGGER.info(evt.toString()));
    }
    
    public void run() {
        try {
            JocClusterServiceLogger.setLogger(ClusterServices.lognotification.name());
            this.socket = createDatagramSocket();
            
            this.running = true;
            LOGGER.info(NOT_NOTIFY_LOGGER, "UDPServer is started on port " + this.port);
            
            this.timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    JocClusterServiceLogger.setLogger(ClusterServices.lognotification.name());
                    execLogEventsThreaded();
                }

            }, TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(1));
            
        } catch (Exception e) {
            LOGGER.error(MARKER, "", e);
            throw new SOSLogManagementException(e);
        }
            
        byte[] receiveData = new byte[BUFFER_SIZE];
                    
        while(this.running) {
            try {

                DatagramPacket dp = new DatagramPacket(receiveData, receiveData.length);

                this.socket.receive(dp);
                
                if (NotificationAppender.doNotify) {
                    logEvents.add(new EventHandler(dp));
                } else {
                    LOGGER.debug(NOT_NOTIFY_LOGGER, "Received message from " + dp.getAddress().getHostName()
                            + " but notification is not yet available");
                }

            } catch (SocketException se) {
                //
                
            } catch (Exception ioe) {
                LOGGER.warn(MARKER, "", ioe);
            }
        }
    }
}


