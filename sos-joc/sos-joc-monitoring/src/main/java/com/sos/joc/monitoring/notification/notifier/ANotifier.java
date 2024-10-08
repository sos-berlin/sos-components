package com.sos.joc.monitoring.notification.notifier;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSParameterSubstitutor;
import com.sos.commons.util.SOSString;
import com.sos.history.JobWarning;
import com.sos.inventory.model.job.JobCriticality;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.history.common.HistorySeverity;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.db.monitoring.DBItemNotification;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.monitoring.bean.SystemMonitoringEvent;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.configuration.monitor.AMonitor;
import com.sos.joc.monitoring.model.OrderNotifyAnalyzer;
import com.sos.monitoring.notification.NotificationStatus;
import com.sos.monitoring.notification.NotificationType;
import com.sos.monitoring.notification.OrderNotificationRange;

public abstract class ANotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(ANotifier.class);

    protected static ObjectMapper JSON_OM = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
            DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).configure(
                    SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);

    protected static final String PREFIX_ENV_VAR = "JS7";
    protected static final String PREFIX_COMMON_VAR = "MON";

    private static final String PREFIX_TABLE_ORDERS = "MON_O";
    private static final String PREFIX_TABLE_ORDER_STEPS = "MON_OS";
    private static final String PREFIX_TABLE_NOTIFICATIONS = "MON_N";

    private static final String COMMON_VAR_TIME_ZONE = PREFIX_COMMON_VAR + "_TIME_ZONE";

    private JocVariables jocVariables;
    private Map<String, String> tableFields;
    private Map<String, String> commonVars;
    private Map<String, String> systemVars;
    private NotificationStatus status;
    private TimeZone timeZone;
    private int nr;

    // OrderNotifications
    public abstract NotifyResult notify(NotificationType type, TimeZone timeZone, DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos,
            DBItemNotification mn);

    // SystemNotifications
    public abstract NotifyResult notify(NotificationType type, TimeZone timeZone, String jocId, SystemMonitoringEvent event, Date dateTime,
            String exception);

    public abstract void close();

    public abstract StringBuilder getSendInfo();

    public abstract AMonitor getMonitor();

    protected Map<String, String> getTableFields() {
        return tableFields;
    }

    protected Map<String, String> getSystemVars() {
        return systemVars;
    }

    protected Map<String, String> getCommonVars() {
        return commonVars;
    }

    protected String getValue(NotificationType type) {
        return type == null ? "" : type.name();
    }

    // OrderNotification
    protected void set(NotificationType type, TimeZone timeZone, DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, DBItemNotification mn) {
        this.timeZone = timeZone;

        setStatus(type);
        setCommonVars();
        setTableFields(mo, mos, mn);

        this.jocVariables = new JocVariables(mo, mos);
    }

    // SystemNotification
    protected void set(NotificationType type, TimeZone timeZone, String jocId, SystemMonitoringEvent event, Date dateTime, String exception) {
        this.timeZone = timeZone;

        setStatus(type);
        setCommonVars();
        setSystemVars(type, timeZone, event, dateTime, exception);

        this.jocVariables = new JocVariables(jocId, event);
    }

    // jocId is set by JocVariables
    private void setSystemVars(NotificationType type, TimeZone timeZone, SystemMonitoringEvent event, Date dateTime, String exception) {
        String source = getVarValue(event.getSource());

        systemVars = new HashMap<>();
        systemVars.put("MON_SN_TYPE", getVarValue(type.name()));
        systemVars.put("MON_SN_CATEGORY", event.getCategory().name());
        systemVars.put("MON_SN_SOURCE", source);
        systemVars.put("MON_SN_SECTION", source);// MON_SN_SECTION deprecated with 2.5.2 - use MON_SN_SOURCE instead
        systemVars.put("MON_SN_NOTIFIER", getVarValue(event.getLoggerName()));
        systemVars.put("MON_SN_TIME", dateTime2String(timeZone, dateTime));
        systemVars.put("MON_SN_MESSAGE", event.getMessage());
        systemVars.put("MON_SN_EXCEPTION", getVarValue(exception));
    }

    public static String getVarValue(String val) {
        if (val == null) {
            return "";
        }
        return val;
    }

    private String dateTime2String(TimeZone timeZone, Date dateTime) {
        if (dateTime == null) {
            dateTime = new Date();
        }
        try {
            return SOSDate.getDateTimeWithZoneOffsetAsString(dateTime, timeZone);
        } catch (SOSInvalidDataException e) {
            return "";
        }
    }

    protected String resolveSystemVars(String msg, boolean resolveEnv) {
        return resolveSystemVars(msg, resolveEnv, null);
    }

    protected String resolveSystemVars(String msg, boolean resolveEnv, Map<String, String> map) {
        SOSParameterSubstitutor ps = new SOSParameterSubstitutor(false, "${", "}");
        jocVariables.addKeys(ps);

        commonVars.entrySet().forEach(e -> {
            ps.addKey(e.getKey(), e.getValue());
        });

        systemVars.entrySet().forEach(e -> {
            ps.addKey(e.getKey(), e.getValue());
        });

        if (map != null) {
            map.entrySet().forEach(e -> {
                ps.addKey(e.getKey(), e.getValue());
            });
        }
        String m = ps.replace(msg);
        return resolveEnv ? ps.replaceEnvVars(m) : m;
    }

    protected String resolve(String msg, boolean resolveEnv) {
        return resolve(msg, resolveEnv, null);
    }

    protected String resolve(String msg, boolean resolveEnv, Map<String, String> map) {
        SOSParameterSubstitutor ps = new SOSParameterSubstitutor(false, "${", "}");
        jocVariables.addKeys(ps);

        commonVars.entrySet().forEach(e -> {
            ps.addKey(e.getKey(), e.getValue());
        });

        tableFields.entrySet().forEach(e -> {
            ps.addKey(e.getKey(), e.getValue());
        });

        if (map != null) {
            map.entrySet().forEach(e -> {
                ps.addKey(e.getKey(), e.getValue());
            });
        }
        String m = ps.replace(msg);
        return resolveEnv ? ps.replaceEnvVars(m) : m;
    }

    protected void setStatus(NotificationType type) {
        switch (type) {
        case SUCCESS:
        case RECOVERED:
        case ACKNOWLEDGED:
            status = NotificationStatus.OK;
            break;
        case WARNING:
            status = NotificationStatus.WARNING;
            break;
        case ERROR:
            status = NotificationStatus.CRITICAL;
            break;
        }
    }

    // SystemNotification
    protected String getInfo4execute(boolean isExecute, SystemMonitoringEvent event, NotificationType type, String addInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(Configuration.LOG_INTENT);
        sb.append("[").append(nr).append("]");
        sb.append("[").append(isExecute ? "execute" : "executed").append("]");
        sb.append("[").append(getClass().getSimpleName()).append(" ").append(getMonitorInfo(getMonitor())).append("]");
        sb.append(getInfo(event, type));
        if (addInfo != null) {
            sb.append(addInfo);
        }
        return sb.toString();
    }

    protected String getInfo4executeFailed(SystemMonitoringEvent event, NotificationType type, String addInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(Configuration.LOG_INTENT);
        sb.append("[").append(nr).append("]");
        sb.append("[failed]");
        sb.append("[").append(getClass().getSimpleName()).append(" ").append(getMonitorInfo(getMonitor())).append("]");
        sb.append(getInfo(event, type));
        if (addInfo != null) {
            sb.append(addInfo);
        }
        return sb.toString();
    }

    // HistoryNotification
    protected String getInfo4execute(boolean isExecute, DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, NotificationType type,
            String addInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(Configuration.LOG_INTENT);
        sb.append("[").append(nr).append("]");
        sb.append("[").append(isExecute ? "execute" : "executed").append("]");
        sb.append("[").append(getClass().getSimpleName()).append(" ").append(getMonitorInfo(getMonitor())).append("]");
        sb.append(getInfo(mo, mos, type));
        if (addInfo != null) {
            sb.append(addInfo);
        }
        return sb.toString();
    }

    protected String getInfo4executeFailed(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, NotificationType type, String addInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(Configuration.LOG_INTENT);
        sb.append("[").append(nr).append("]");
        sb.append("[failed]");
        sb.append("[").append(getClass().getSimpleName()).append(" ").append(getMonitorInfo(getMonitor())).append("]");
        sb.append(getInfo(mo, mos, type));
        if (addInfo != null) {
            sb.append(addInfo);
        }
        return sb.toString();
    }

    public static String getTypeAsString(NotificationType type) {
        return "on " + type.value();
    }

    public static StringBuilder getMainInfo(AMonitor monitor) {
        return getMainInfo(monitor, null);
    }

    private static StringBuilder getMainInfo(AMonitor monitor, NotificationType type) {
        if (monitor == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(monitor.getType().value()).append(" ").append(getMonitorInfo(monitor)).append("]");
        if (type != null) {
            sb.append("[").append(getTypeAsString(type)).append("]");
        }
        return sb;
    }

    private static String getMonitorInfo(AMonitor monitor) {
        StringBuilder sb = new StringBuilder();
        sb.append("monitor=").append(monitor.getMonitorName());
        sb.append(",timeZone=").append(monitor.getTimeZoneValue());
        return sb.toString();
    }

    public static StringBuilder getInfo(OrderNotifyAnalyzer analyzer, AMonitor monitor, NotificationType type) {
        if (analyzer == null || monitor == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(getMainInfo(monitor, type));
        return sb.append(getInfo(analyzer));
    }

    public static StringBuilder getInfo(OrderNotifyAnalyzer analyzer) {
        if (analyzer == null) {
            return null;
        }
        return getInfo(analyzer.getOrder(), analyzer.getOrderStep(), null);
    }

    private static StringBuilder getInfo(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, NotificationType type) {
        StringBuilder sb = new StringBuilder();
        if (type != null) {
            sb.append("[").append(getTypeAsString(type)).append("]");
        }
        sb.append("[");
        if (mo != null) {
            sb.append("controllerId=").append(mo.getControllerId());
            sb.append(",workflow=").append(mo.getWorkflowName());
            sb.append(",orderId=").append(mo.getOrderId());
        }
        if (mos != null) {
            sb.append(",job=").append(mos.getJobName());
            sb.append(",label=").append(mos.getJobLabel());
            sb.append(",position=").append(mos.getPosition());
        }
        sb.append("]");
        return sb;
    }

    // SystemNotifier
    private static StringBuilder getInfo(SystemMonitoringEvent event, NotificationType type) {
        StringBuilder sb = new StringBuilder();
        if (type != null) {
            sb.append("[").append(getTypeAsString(type)).append("]");
        }
        sb.append("[");
        sb.append("category=").append(event.getCategory());
        sb.append(",name=").append(event.getLoggerName());
        sb.append("]");
        return sb;
    }

    private void setCommonVars() {
        commonVars = new HashMap<String, String>();
        commonVars.put(COMMON_VAR_TIME_ZONE, getMonitor().getTimeZoneValue());
    }

    private void setTableFields(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, DBItemNotification mn) {
        tableFields = new HashMap<String, String>();
        // additional fields
        tableFields.put(PREFIX_TABLE_ORDERS + "_TIME_ELAPSED", "");
        tableFields.put(PREFIX_TABLE_ORDER_STEPS + "_TIME_ELAPSED", "");
        tableFields.put(PREFIX_TABLE_ORDER_STEPS + "_JOB_TAGS", "");
        tableFields.put(PREFIX_TABLE_NOTIFICATIONS + "_STATUS", status.intValue().toString());

        if (mo == null) {
            tableFields.putAll(DBItem.toEmptyValuesMap(DBItemMonitoringOrder.class, true, PREFIX_TABLE_ORDERS));
        } else {
            tableFields.putAll(mo.toMap(true, PREFIX_TABLE_ORDERS, timeZone, true));
            adjustFields(PREFIX_TABLE_ORDERS);
        }
        if (mos == null) {
            tableFields.putAll(DBItem.toEmptyValuesMap(DBItemMonitoringOrderStep.class, true, PREFIX_TABLE_ORDER_STEPS));
        } else {
            tableFields.putAll(mos.toMap(true, PREFIX_TABLE_ORDER_STEPS, timeZone, true));
            adjustFields(PREFIX_TABLE_ORDER_STEPS);
            if (mos.getTags() != null) {
                tableFields.put(PREFIX_TABLE_ORDER_STEPS + "_JOB_TAGS", mos.getTags());
            }
        }

        // TODO - to remove later
        // introduced with 2.4.1 - duplicate new MON_N_WARN/WARN_TEXT names into deprecated MON_OS_WARN/WARN_TEXT
        boolean compatibilityMode = true;
        if (mn == null) {
            tableFields.putAll(DBItem.toEmptyValuesMap(DBItemNotification.class, true, PREFIX_TABLE_NOTIFICATIONS));

            if (compatibilityMode) {
                putTableField(PREFIX_TABLE_ORDER_STEPS + "_WARN", "");
                putTableField(PREFIX_TABLE_ORDER_STEPS + "_WARN_TEXT", "");
            }
        } else {
            tableFields.putAll(mn.toMap(true, PREFIX_TABLE_NOTIFICATIONS, timeZone, true));
            adjustFields(PREFIX_TABLE_NOTIFICATIONS);

            if (compatibilityMode) {
                putTableField(PREFIX_TABLE_ORDER_STEPS + "_WARN", tableFields.get(PREFIX_TABLE_NOTIFICATIONS + "_WARN"));
                putTableField(PREFIX_TABLE_ORDER_STEPS + "_WARN_TEXT", tableFields.get(PREFIX_TABLE_NOTIFICATIONS + "_WARN_TEXT"));
            }
        }
    }

    private void adjustFields(String tablePrefix) {
        setElapsed(tablePrefix);
        setSeverity(tablePrefix);

        switch (tablePrefix) {
        case PREFIX_TABLE_ORDERS:
            String state = tableFields.get(tablePrefix + "_STATE");
            try {
                tableFields.put(tablePrefix + "_STATE", OrderStateText.fromValue(Integer.valueOf(state)).value());
            } catch (Throwable e) {
                putTableField(tablePrefix + "_STATE", state);
            }
            putTableFieldVariables(tablePrefix, "START");
            break;
        case PREFIX_TABLE_ORDER_STEPS:
            String criticality = tableFields.get(tablePrefix + "_JOB_CRITICALITY");
            try {
                tableFields.put(tablePrefix + "_JOB_CRITICALITY", JobCriticality.fromValue(Integer.valueOf(criticality)).value());
            } catch (Throwable e) {
                putTableField(tablePrefix + "_JOB_CRITICALITY", criticality);
            }
            putTableFieldVariables(tablePrefix, "START");
            putTableFieldVariables(tablePrefix, "END");
            break;
        case PREFIX_TABLE_NOTIFICATIONS:
            String type = tableFields.get(tablePrefix + "_TYPE");
            try {
                tableFields.put(tablePrefix + "_TYPE", NotificationType.fromValue(Integer.valueOf(type)).value());
            } catch (Throwable e) {
                putTableField(tablePrefix + "_TYPE", type);
            }
            String range = tableFields.get(tablePrefix + "_RANGE");
            try {
                tableFields.put(tablePrefix + "_RANGE", OrderNotificationRange.fromValue(Integer.valueOf(range)).value());
            } catch (Throwable e) {
                putTableField(tablePrefix + "_RANGE", range);
            }
            String warn = tableFields.get(tablePrefix + "_WARN");
            try {
                tableFields.put(tablePrefix + "_WARN", JobWarning.fromValue(Integer.valueOf(warn)).value());
            } catch (Throwable e) {
                putTableField(tablePrefix + "_WARN", warn);
            }
            String recoveredId = tableFields.get(tablePrefix + "_RECOVERED_ID");
            if (recoveredId.equals("0")) {
                putTableField(tablePrefix + "_RECOVERED_ID", "");
            }
            if (tableFields.containsKey(tablePrefix + "_HAS_MONITORS")) {
                tableFields.remove(tablePrefix + "_HAS_MONITORS");
            }
            break;
        }
    }

    private void setElapsed(String tablePrefix) {
        if (tablePrefix.equals(PREFIX_TABLE_NOTIFICATIONS)) {
            return;
        }
        String newField = tablePrefix + "_TIME_ELAPSED";
        tableFields.put(newField, "");

        String startTime = tableFields.get(tablePrefix + "_START_TIME");
        String endTime = tableFields.get(tablePrefix + "_END_TIME");
        if (!SOSString.isEmpty(startTime) && !SOSString.isEmpty(endTime)) {
            try {
                Date s = toDate(startTime);
                Date e = toDate(endTime);
                if (s != null && e != null) {
                    Long diffSeconds = e.getTime() / 1000 - s.getTime() / 1000;
                    tableFields.put(newField, diffSeconds.toString());
                }
            } catch (Throwable e) {
            }
        }
    }

    private Date toDate(String date) {
        try {
            return SOSDate.parse(date, SOSDate.DATETIME_FORMAT_WITH_ZONE_OFFSET);
        } catch (Throwable e) {
            try {
                return SOSDate.getDateTime(date);
            } catch (Throwable ee) {
            }
            return null;
        }
    }

    private void setSeverity(String tablePrefix) {
        if (tablePrefix.equals(PREFIX_TABLE_NOTIFICATIONS)) {
            return;
        }
        String severity = tableFields.get(tablePrefix + "_SEVERITY");
        try {
            tableFields.put(tablePrefix + "_SEVERITY", HistorySeverity.getName(Integer.valueOf(severity)));
        } catch (Throwable e) {
            putTableField(tablePrefix + "_SEVERITY", severity);
        }
    }

    private void putTableField(String fieldName, String value) {
        tableFields.put(fieldName, value == null ? "" : value);
    }

    private void putTableFieldVariables(String tablePrefix, String variablesRange) {
        String vars = tableFields.get(tablePrefix + "_" + variablesRange + "_VARIABLES");
        if (!SOSString.isEmpty(vars)) {
            try {
                JsonNode root = ANotifier.JSON_OM.readTree(vars);
                if (root != null) {
                    String varNamePrefix = tablePrefix + "_" + variablesRange + "_VARIABLE_";
                    Iterator<Entry<String, JsonNode>> nodes = root.fields();
                    while (nodes.hasNext()) {
                        Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodes.next();

                        if (entry.getValue().isObject()) {// ListValue
                            Iterator<Entry<String, JsonNode>> entryNodes = entry.getValue().fields();
                            while (entryNodes.hasNext()) {
                                Map.Entry<String, JsonNode> entryChild = (Map.Entry<String, JsonNode>) entryNodes.next();
                                tableFields.put(varNamePrefix + entry.getKey().toUpperCase() + "_" + entryChild.getKey().toUpperCase(), getValue(
                                        entryChild));
                            }
                        } else {
                            tableFields.put(varNamePrefix + entry.getKey().toUpperCase(), getValue(entry));
                        }
                    }
                }
            } catch (Throwable e) {
                LOGGER.warn(String.format("[%s_%s_VARIABLES=%s]%s", tablePrefix, variablesRange, vars, e.toString()), e);
            }
        }
    }

    private String getValue(Entry<String, JsonNode> entry) {
        try {
            return entry.getValue().asText();
        } catch (Throwable e) {
            return "";
        }
    }

    protected JocVariables getJocVariables() {
        return jocVariables;
    }

    public void setNr(int nr) {
        this.nr = nr;
    }

}
