package com.sos.joc.monitoring.notification.notifier;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

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
import com.sos.joc.monitoring.configuration.monitor.AMonitor;
import com.sos.joc.monitoring.model.NotifyAnalyzer;
import com.sos.monitoring.notification.NotificationRange;
import com.sos.monitoring.notification.NotificationStatus;
import com.sos.monitoring.notification.NotificationType;

public abstract class ANotifier {

    protected static final String PREFIX_ENV_VAR = "JS7";
    protected static final String PREFIX_COMMON_VAR = "MON";

    private static final String PREFIX_TABLE_ORDERS = "MON_O";
    private static final String PREFIX_TABLE_ORDER_STEPS = "MON_OS";
    private static final String PREFIX_TABLE_NOTIFICATIONS = "MON_N";

    private static final String COMMON_VAR_TIME_ZONE = PREFIX_COMMON_VAR + "_TIME_ZONE";

    private JocHref jocHref;
    private Map<String, String> tableFields;
    private Map<String, String> commonVars;
    private NotificationStatus status;
    private TimeZone timeZone;
    private int nr;

    public abstract NotifyResult notify(NotificationType type, TimeZone timeZone, DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos,
            DBItemNotification mn);

    public abstract void close();

    public abstract StringBuilder getSendInfo();

    public abstract AMonitor getMonitor();

    protected Map<String, String> getTableFields() {
        return tableFields;
    }

    protected Map<String, String> getCommonVars() {
        return commonVars;
    }

    protected String getValue(NotificationType type) {
        return type == null ? "" : type.name();
    }

    protected void set(NotificationType type, TimeZone timeZone, DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, DBItemNotification mn) {
        this.timeZone = timeZone;

        setStatus(type);
        setCommonVars();
        setTableFields(mo, mos, mn);
        this.jocHref = new JocHref(mo, mos);
    }

    protected String resolve(String msg, boolean resolveEnv) {
        return resolve(msg, resolveEnv, null);
    }

    protected String resolve(String msg, boolean resolveEnv, Map<String, String> map) {
        SOSParameterSubstitutor ps = new SOSParameterSubstitutor(false, "${", "}");
        jocHref.addKeys(ps);

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

    protected String getInfo4execute(boolean isExecute, DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, NotificationType type,
            String addInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(nr).append("]");
        sb.append("[").append(getClass().getSimpleName()).append(" ").append(getMonitorInfo(getMonitor())).append("]");
        sb.append("[").append(isExecute ? "execute" : "successful").append("]");
        sb.append(getInfo(mo, mos, type));
        if (addInfo != null) {
            sb.append(addInfo);
        }
        return sb.toString();
    }

    protected String getInfo4executeFailed(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, NotificationType type, String addInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(nr).append("]");
        sb.append("[").append(getClass().getSimpleName()).append(" ").append(getMonitorInfo(getMonitor())).append("]");
        sb.append("[failed]");
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

    public static StringBuilder getInfo(NotifyAnalyzer analyzer, AMonitor monitor, NotificationType type) {
        if (analyzer == null || monitor == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(getMainInfo(monitor, type));
        return sb.append(getInfo(analyzer));
    }

    public static StringBuilder getInfo(NotifyAnalyzer analyzer) {
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

    private void setCommonVars() {
        commonVars = new HashMap<String, String>();
        commonVars.put(COMMON_VAR_TIME_ZONE, getMonitor().getTimeZoneValue());
    }

    private void setTableFields(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, DBItemNotification mn) {
        tableFields = new HashMap<String, String>();
        // additional fields
        tableFields.put(PREFIX_TABLE_ORDERS + "_TIME_ELAPSED", "");
        tableFields.put(PREFIX_TABLE_ORDER_STEPS + "_TIME_ELAPSED", "");
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
            break;
        case PREFIX_TABLE_ORDER_STEPS:
            String criticality = tableFields.get(tablePrefix + "_JOB_CRITICALITY");
            try {
                tableFields.put(tablePrefix + "_JOB_CRITICALITY", JobCriticality.fromValue(Integer.valueOf(criticality)).value());
            } catch (Throwable e) {
                putTableField(tablePrefix + "_JOB_CRITICALITY", criticality);
            }
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
                tableFields.put(tablePrefix + "_RANGE", NotificationRange.fromValue(Integer.valueOf(range)).value());
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
                Date s = SOSDate.getDateTime(startTime);
                Date e = SOSDate.getDateTime(endTime);
                Long diffSeconds = e.getTime() / 1000 - s.getTime() / 1000;
                tableFields.put(newField, diffSeconds.toString());
            } catch (Exception e) {
            }
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

    protected JocHref getJocHref() {
        return jocHref;
    }

    public void setNr(int nr) {
        this.nr = nr;
    }

}
