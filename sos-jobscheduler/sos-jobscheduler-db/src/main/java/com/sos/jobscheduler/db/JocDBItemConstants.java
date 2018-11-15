package com.sos.jobscheduler.db;

import com.sos.jobscheduler.db.audit.DBItemAuditLog;
import com.sos.jobscheduler.db.calendar.DBItemInventoryClusterCalendar;
import com.sos.jobscheduler.db.calendar.DBItemInventoryClusterCalendarUsage;
import com.sos.jobscheduler.db.configuration.DBItemJocConfiguration;

public class JocDBItemConstants {

    public static final String DEFAULT_NAME = ".";
    public static final String DEFAULT_FOLDER = "/";
    public static final Long DEFAULT_ID = 0L;

	/** Table JOC_CONFIGURATIONS */
    public static final String DBITEM_JOC_CONFIGURATIONS = DBItemJocConfiguration.class.getSimpleName();
    public static final String TABLE_JOC_CONFIGURATIONS = "JOC_CONFIGURATIONS";
    public static final String TABLE_JOC_CONFIGURATIONS_SEQUENCE = "JOC_CONFIGURATIONS_SEQ";

    /** Table AUIDT_LOG */
    public static final String DBITEM_AUDIT_LOG = DBItemAuditLog.class.getSimpleName();
    public static final String TABLE_AUDIT_LOG = "AUDIT_LOG";
    public static final String TABLE_AUDIT_LOG_SEQUENCE = "AUDIT_LOG_SEQ";
    
    /** Table CALENDARS */
    public static final String DBITEM_CLUSTER_CALENDARS = DBItemInventoryClusterCalendar.class.getSimpleName();
    public static final String TABLE_CLUSTER_CALENDARS = "CLUSTER_CALENDARS";
    public static final String TABLE_CLUSTER_CALENDARS_SEQUENCE = "REPORTING_IC_ID_SEQ";

    /** Table CALENDAR_USAGE */
    public static final String DBITEM_INVENTORY_CLUSTER_CALENDAR_USAGE = DBItemInventoryClusterCalendarUsage.class.getSimpleName();
    public static final String TABLE_INVENTORY_CLUSTER_CALENDAR_USAGE = "CLUSTER_CALENDAR_USAGE";
    public static final String TABLE_INVENTORY_CLUSTER_CALENDAR_USAGE_SEQUENCE = "REPORTING_ICU_ID_SEQ";

}
