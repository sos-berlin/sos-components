package com.sos.joc.db;

import com.sos.joc.db.audit.DBItemAuditLog;
import com.sos.joc.db.calendars.DBItemCalendar;
import com.sos.joc.db.calendars.DBItemInventoryCalendarUsage;
import com.sos.joc.db.configuration.DBItemJocConfiguration;

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
    public static final String DBITEM_CALENDARS = DBItemCalendar.class.getSimpleName();
    public static final String TABLE_CALENDARS = "INVENTORY_CALENDARS";
    public static final String TABLE_CALENDARS_SEQUENCE = "REPORTING_IC_ID_SEQ";

    /** Table CALENDAR_USAGE */
    public static final String DBITEM_INVENTORY_CALENDAR_USAGE = DBItemInventoryCalendarUsage.class.getSimpleName();
    public static final String TABLE_INVENTORY_CALENDAR_USAGE = "INVENTORY_CALENDAR_USAGE";
    public static final String TABLE_INVENTORY_CALENDAR_USAGE_SEQUENCE = "REPORTING_ICU_ID_SEQ";

}
