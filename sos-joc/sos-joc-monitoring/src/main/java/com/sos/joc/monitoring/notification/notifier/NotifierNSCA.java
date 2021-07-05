package com.sos.joc.monitoring.notification.notifier;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jsendnsca.Level;
import com.googlecode.jsendnsca.MessagePayload;
import com.googlecode.jsendnsca.NagiosPassiveCheckSender;
import com.googlecode.jsendnsca.NagiosSettings;
import com.googlecode.jsendnsca.builders.MessagePayloadBuilder;
import com.googlecode.jsendnsca.builders.NagiosSettingsBuilder;
import com.googlecode.jsendnsca.encryption.Encryption;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.monitoring.configuration.monitor.MonitorNSCA;
import com.sos.joc.monitoring.db.DBLayerMonitoring;
import com.sos.joc.monitoring.exception.SOSNotifierSendException;

/** com.googlecode.jsendnsca.encryption.Encryption supports only 3 encryptions : NONE, XOR, TRIPLE_DES
 * 
 * "send_nsca.cfg" Note:
 * 
 * The encryption method you specify here must match the decryption method the nsca daemon uses (as specified in the nsca.cfg file)!!
 * 
 * Values:
 * 
 * 0=None (Do NOT use this option) <- Encryption.NONE
 * 
 * 1=Simple XOR (No security, just obfuscation, but very fast) <- Encryption.XOR
 * 
 * 2=DES,
 * 
 * 3=3DES (Triple DES) <- Encryption.TRIPLE_DES
 * 
 * 4=CAST-128, 5=CAST-256, 6=xTEA, 7=3WAY, 8=BLOWFISH, 9=TWOFISH
 * 
 * 10=LOKI97, 11=RC2, 12=ARCFOUR, 14=RIJNDAEL-128, 15=RIJNDAEL-192, 16=RIJNDAEL-256, 19=WAKE
 * 
 * 20=SERPENT, 22=ENIGMA (Unix crypt), 23=GOST, 24=SAFER64, 25=SAFER128, 26=SAFER+ */
public class NotifierNSCA extends ANotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifierNSCA.class);

    public static final String VAR_SERVICE_NAME = "SERVICE_NAME";

    private final MonitorNSCA monitor;
    private NagiosSettings settings = null;
    private String message;

    public NotifierNSCA(MonitorNSCA monitor) {
        this.monitor = monitor;
    }

    public void init() throws Exception {
        NagiosSettingsBuilder nb = new NagiosSettingsBuilder().withNagiosHost(monitor.getMonitorHost());

        if (monitor.getMonitorPort() > -1) {
            nb.withPort(monitor.getMonitorPort());
        }
        if (monitor.getMonitorConnectionTimeout() > -1) {
            nb.withConnectionTimeout(monitor.getMonitorConnectionTimeout());
        }
        if (monitor.getMonitorResponseTimeout() > -1) {
            nb.withResponseTimeout(monitor.getMonitorResponseTimeout());
        }
        if (monitor.getMonitorPort() > -1) {
            nb.withPort(monitor.getMonitorPort());
        }
        if (!SOSString.isEmpty(monitor.getMonitorEncryption())) {
            nb.withEncryption(Encryption.valueOf(monitor.getMonitorEncryption()));
        }
        if (!SOSString.isEmpty(monitor.getMonitorPassword())) {
            nb.withPassword(monitor.getMonitorPassword());
        }
        settings = nb.create();
    }

    @Override
    public void close() {
    }

    @Override
    public void notify(DBLayerMonitoring dbLayer, DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, ServiceStatus status,
            ServiceMessagePrefix prefix) throws SOSNotifierSendException {

        try {
            evaluate(mo, mos, status, prefix);

            Map<String, String> map = new HashMap<>();
            map.put(VAR_SERVICE_NAME, getServiceName());
            message = resolve(monitor.getMessage(), true, map);
            setMessagePrefix(prefix);

            MessagePayload payload = new MessagePayloadBuilder().withHostname(monitor.getServiceHost()).withLevel(getLevel(status)).withServiceName(
                    getServiceName()).withMessage(message).create();

            NagiosPassiveCheckSender sender = new NagiosPassiveCheckSender(settings);

            LOGGER.info(String.format("[%s-%s][nsca][execute][monitor host=%s:%s][service host=%s][level=%s]%s", getServiceStatus(),
                    getServiceMessagePrefix(), settings.getNagiosHost(), settings.getPort(), payload.getHostname(), payload.getLevel(), payload
                            .getMessage()));
            sender.send(payload);
        } catch (Throwable e) {
            throw new SOSNotifierSendException(String.format("[%s name=\"%s\"]can't send notification", monitor.getRefElementName(), monitor
                    .getMonitorName()), e);
        }
    }

    // TODO
    private String getServiceName() {
        return null;
    }

    private void setMessagePrefix(ServiceMessagePrefix prefix) {
        if (message == null) {
            return;
        }
        if (prefix == null) {
            return;
        }

        if (!prefix.equals(ServiceMessagePrefix.SUCCESS)) {
            String msg = message.trim().toLowerCase();
            String prefixName = prefix.name().trim().toLowerCase();
            if (!msg.startsWith(prefixName)) {
                message = prefix.name() + " " + message;
            }
        }
    }

    private Level getLevel(ServiceStatus status) {
        if (status.equals(ServiceStatus.OK)) {
            Level level = status2level(monitor.getServiceStatusOnSuccess());
            return level == null ? Level.OK : level;
        } else {
            Level level = status2level(monitor.getServiceStatusOnError());
            return level == null ? Level.CRITICAL : level;
        }
    }

    private Level status2level(String status) {
        Level l = null;
        if (status != null) {
            switch (status) {
            case "0":
                l = Level.OK;
                break;
            case "1":
                l = Level.WARNING;
                break;
            case "2":
                l = Level.CRITICAL;
                break;
            case "3":
                l = Level.UNKNOWN;
                break;
            }
        }
        return l;
    }

}
