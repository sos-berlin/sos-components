package com.sos.joc.logmanagement;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.event.bean.monitoring.SystemNotificationLogEvent;
import com.sos.joc.logmanagement.exception.SOSUnexpectedMessageException;
import com.sos.joc.model.log.Level;
import com.sos.joc.model.log.LogEvent;
import com.sos.schema.JsonValidator;

public class EventHandler {

    private byte[] raw = null; //<134>1 2024-07-30T14:47:02.814Z OH JS7 Controller
    private InetAddress inetAddress = null;
    private static final Pattern syslogHeaderPattern = Pattern.compile(
            "<\\d{1,3}>\\d{0,2}\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{1,9})Z)\\s+(\\S+)\\s+(.+)",
            Pattern.DOTALL + Pattern.MULTILINE);
    private static final Logger LOGGER = LoggerFactory.getLogger("JOCLogNotification");
    private static final Marker MARKER = MarkerFactory.getMarker("JOCLogNotification");
    private static final Marker NOT_NOTIFY_LOGGER = MarkerFactory.getMarker(WebserviceConstants.NOT_NOTIFY_LOGGER.getName());
    private static final Level expectedMinLogLevel = Level.WARN;
    private static final char rightAngleBracket = '〉'; //https://www.compart.com/de/unicode/U+3009
    private static final char leftAngleBracket = '〈'; //https://www.compart.com/de/unicode/U+3008
    
    protected EventHandler(DatagramPacket dp) {
        copyData(dp.getData(), dp.getLength());
        inetAddress = dp.getAddress();
    }

    protected EventHandler(byte[] message, int length) {
        copyData(message, length);
    }
    
    public SystemNotificationLogEvent mapToSystemNotificationLogEvent() {
        String message = newString();
        LOGGER.debug(NOT_NOTIFY_LOGGER, "Received message from " + inetAddress.getHostName() + ": " + message);
        try {
            String[] messageParts = message.split("\\s+\\{", 2);

            if (messageParts.length == 2) {
                String logMessage = getLogMessage(getHeaderInfoAsJSON(messageParts[0]), messageParts[1]);
                LogEvent evt;
                
                if (UDPServer.schema != null) {
                    try {
                        JsonValidator.validate(logMessage.getBytes(StandardCharsets.UTF_8), UDPServer.schema, true);
                    } catch (Exception e) {
                        throw new SOSUnexpectedMessageException(e);
                    }
                }
                try {
                    logMessage = sanitize(logMessage);
                    evt = Globals.objectMapper.readValue(logMessage, LogEvent.class);
                } catch (Exception e) {
                    throw new SOSUnexpectedMessageException(e);
                }
                if (!evt.getLevel().isMoreSpecificThan(expectedMinLogLevel)) {
                    throw new SOSUnexpectedMessageException("Only warnings and errors are expected"); 
                }
                //addHeaderInfo(messageParts[0], evt);
                setIds(evt);
                
                SystemNotificationLogEvent notificationEvt = new SystemNotificationLogEvent(evt.getHost(), evt.getProduct().value(), evt
                        .getClusterId(), evt.getInstanceId(), evt.getLevel().value(), evt.getTimestamp().toInstant(), evt.getLogger(), evt
                                .getMessage(), evt.getThrown());
                LOGGER.debug(NOT_NOTIFY_LOGGER, "Notification event: " + notificationEvt.toString());

                return notificationEvt;
            } else {
                throw new SOSUnexpectedMessageException("Couldn't find message body in JSON format");
            }

        } catch (SOSUnexpectedMessageException e) {
            LOGGER.error(NOT_NOTIFY_LOGGER, e.toString() + ":\n" + message);
        } catch (Exception e) {
            LOGGER.error(MARKER, "", e);
        }
        return null;
    }
    
    private String getLogMessage(String firstMessagePart, String secondMessagePart) {
        String logMessage = "{" + firstMessagePart + secondMessagePart.trim();
        if (!logMessage.endsWith("}")) {
            if (!logMessage.endsWith("\"")) {
                logMessage += "\"";
            }
            logMessage += "}";
        }
        return logMessage;
    }
    
    private void copyData(byte[] message, int length) {
        this.raw = new byte[length];
        System.arraycopy(message, 0, this.raw, 0, length);
    }
    
    private String newString() {
        return new String(this.raw, StandardCharsets.UTF_8);
    }
    
    private String sanitize(String s) {
        // < and > will be replaced by 〈〉
        return s.replace('<', leftAngleBracket).replace('>', rightAngleBracket);
    }
    
    private void setIds(LogEvent evt) {
        if (evt.getProduct() != null) {
            switch (evt.getProduct()) {
            case AGENT:
                // TODO ?? agentId
                if (evt.getInstanceId() != null) {
                    evt.setInstanceId(evt.getInstanceId().replaceFirst("^(Suba|A)gent:", ""));
                }
                break;
            case CONTROLLER:
                // TODO ?? controllerId
                if (evt.getClusterId() != null) {
                    evt.setClusterId(evt.getClusterId().replaceFirst("^Controller:", ""));
                    if (evt.getInstanceId() != null) {
                        if (evt.getInstanceId().isBlank()) {
                            evt.setInstanceId("Backup"); 
                        }
                    } else {
                        evt.setInstanceId("Backup"); 
                    }
                }
                break;
            }
        }
    }
    
    private String getHeaderInfoAsJSON(String header) {
        Matcher m = syslogHeaderPattern.matcher(header);
        StringBuilder s = new StringBuilder();
        if (m.find()) {
            s.append("\"timestamp\":\"").append(m.group(1)).append("\",");
            String host = m.group(2);
            if (host == null || host.isBlank()) {
                host = inetAddress.getHostAddress();
            }
            s.append("\"host\":\"").append(host.trim()).append("\",");
            s.append("\"product\":\"").append(m.group(3).trim().toUpperCase()).append("\",");
            return s.toString();
        } else {
            throw new SOSUnexpectedMessageException("Couldn't find message header according RFC5424 with timestamp, hostname and application name"); 
        }
    }
    
//    private void addHeaderInfo(String header, LogEvent evt) {
//        Matcher m = syslogHeaderPattern.matcher(header);
//        if (m.find()) {
//            evt.setTimestamp(Instant.parse(m.group(1)));
//            evt.setHost(m.group(2).trim());
//            evt.setProduct(Product.fromValue(m.group(3).trim().toUpperCase()));
//            setClusterId(evt);
//        } else {
//            throw new SOSUnexpectedMessageException("Couldn't find message header according RFC5424 with timestamp, hostname and application name"); 
//        }
//    }
}
