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
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.configuration.monitor.MonitorNSCA;

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

    private String serviceName;
    private Level level;

    public NotifierNSCA(MonitorNSCA monitor, Configuration conf) throws Exception {
        this.monitor = monitor;
        init();
    }

    private void init() throws Exception {
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
    public void notify(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, Status status) {

        try {
            set(mo, mos);
            set(status);

            Map<String, String> map = new HashMap<>();
            map.put(VAR_SERVICE_NAME, serviceName);
            message = resolve(monitor.getMessage(), status, true, map);

            MessagePayload payload = new MessagePayloadBuilder().withHostname(monitor.getServiceHost()).withLevel(level).withServiceName(serviceName)
                    .withMessage(message).create();

            NagiosPassiveCheckSender sender = new NagiosPassiveCheckSender(settings);

            StringBuilder info = new StringBuilder();
            info.append("[monitor host=").append(settings.getNagiosHost()).append(":").append(settings.getPort()).append("]");
            info.append("[service host=").append(payload.getHostname()).append("]");
            info.append("[level=").append(payload.getLevel()).append("]");
            info.append(payload.getMessage());
            LOGGER.info(getInfo4execute(mo, mos, status, info.toString()));

            sender.send(payload);
        } catch (Throwable e) {
            LOGGER.error(getInfo4executeException(mo, mos, status, "[" + monitor.getInfo().toString() + "]", e));
        }
    }

    private void set(Status status) {
        switch (status) {
        case SUCCESS:
            serviceName = monitor.getServiceNameOnSuccess();
            level = status2level(monitor.getServiceStatusOnSuccess(), Level.OK);
            break;
        case RECOVERED:
            serviceName = monitor.getServiceNameOnError();
            level = Level.OK;
            break;
        case ERROR:
        default:
            serviceName = monitor.getServiceNameOnError();
            level = status2level(monitor.getServiceStatusOnError(), Level.CRITICAL);
            break;
        }
    }

    private Level status2level(String status, Level defaultLevel) {
        if (status != null) {
            switch (status) {
            case "0":
                return Level.OK;
            case "1":
                return Level.WARNING;
            case "2":
                return Level.CRITICAL;
            case "3":
                return Level.UNKNOWN;
            }
        }
        return defaultLevel;
    }

}
