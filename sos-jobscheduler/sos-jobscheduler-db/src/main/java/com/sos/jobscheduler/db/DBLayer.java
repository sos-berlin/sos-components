package com.sos.jobscheduler.db;

import java.io.Serializable;

import com.sos.commons.util.SOSClassList;
import com.sos.jobscheduler.db.audit.DBItemAuditLog;
import com.sos.jobscheduler.db.calendar.DBItemCalendar;
import com.sos.jobscheduler.db.calendar.DBItemCalendarUsage;
import com.sos.jobscheduler.db.configuration.DBItemJocConfiguration;
import com.sos.jobscheduler.db.documentation.DBItemDocumentation;
import com.sos.jobscheduler.db.documentation.DBItemDocumentationImage;
import com.sos.jobscheduler.db.documentation.DBItemDocumentationUsage;
import com.sos.jobscheduler.db.general.DBItemVariable;
import com.sos.jobscheduler.db.history.DBItemAgent;
import com.sos.jobscheduler.db.history.DBItemLog;
import com.sos.jobscheduler.db.history.DBItemMaster;
import com.sos.jobscheduler.db.history.DBItemOrder;
import com.sos.jobscheduler.db.history.DBItemOrderStatus;
import com.sos.jobscheduler.db.history.DBItemOrderStep;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.jobscheduler.db.inventory.agent.DBItemInventoryAgentCluster;
import com.sos.jobscheduler.db.inventory.agent.DBItemInventoryAgentClusterMember;
import com.sos.jobscheduler.db.inventory.agent.DBItemInventoryAgentInstance;
import com.sos.jobscheduler.db.orders.DBItemDailyPlan;
import com.sos.jobscheduler.db.orders.DBItemDailyPlanVariables;
import com.sos.jobscheduler.db.orders.DBItemDaysPlanned;
import com.sos.jobscheduler.db.os.DBItemOperatingSystem;
 

public class DBLayer implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String GENERAL_TABLE_VARIABLES = "SOS_JS_VARIABLES";
    public static final String GENERAL_DBITEM_VARIABLE = DBItemVariable.class.getSimpleName();

    public static final String HISTORY_TABLE_ORDERS = "SOS_JS_HISTORY_ORDERS";
    public static final String HISTORY_TABLE_ORDERS_SEQUENCE = "SOS_JS_HO_SEQ";
    public static final String HISTORY_DBITEM_ORDER = DBItemOrder.class.getSimpleName();

    public static final String HISTORY_TABLE_ORDER_STATUS = "SOS_JS_HISTORY_ORDER_STATUS";
    public static final String HISTORY_TABLE_ORDER_STATUS_SEQUENCE = "SOS_JS_HOST_SEQ";
    public static final String HISTORY_DBITEM_ORDER_STATUS = DBItemOrderStatus.class.getSimpleName();

    public static final String HISTORY_TABLE_ORDER_STEPS = "SOS_JS_HISTORY_ORDER_STEPS";
    public static final String HISTORY_TABLE_ORDER_STEPS_SEQUENCE = "SOS_JS_HOS_SEQ";
    public static final String HISTORY_DBITEM_ORDER_STEP = DBItemOrderStep.class.getSimpleName();

    public static final String HISTORY_TABLE_LOGS = "SOS_JS_HISTORY_LOGS";
    public static final String HISTORY_TABLE_LOGS_SEQUENCE = "SOS_JS_HL_SEQ";
    public static final String HISTORY_DBITEM_LOG = DBItemLog.class.getSimpleName();

    public static final String HISTORY_TABLE_MASTERS = "SOS_JS_HISTORY_MASTERS";
    public static final String HISTORY_TABLE_MASTERS_SEQUENCE = "SOS_JS_HM_SEQ";
    public static final String HISTORY_DBITEM_MASTER = DBItemMaster.class.getSimpleName();

    public static final String HISTORY_TABLE_AGENTS = "SOS_JS_HISTORY_AGENTS";
    public static final String HISTORY_TABLE_AGENTS_SEQUENCE = "SOS_JS_HA_SEQ";
    public static final String HISTORY_DBITEM_AGENT = DBItemAgent.class.getSimpleName();

    public static final String DAILY_PLAN_TABLE = "SOS_JS_ORDER_DAILY_PLAN";
    public static final String DAILY_PLAN_TABLE_SEQUENCE = "SOS_JS_DP_SEQ";
    public static final String DAILY_PLAN_DBITEM = DBItemDailyPlan.class.getSimpleName();

    public static final String DAYS_PLANNED_TABLE = "SOS_JS_ORDER_DAYS_PLANNED";
    public static final String DAYS_PLANNED_TABLE_SEQUENCE = "SOS_JS_DPL_SEQ";
    public static final String DAYS_PLANNED_DBITEM = DBItemDaysPlanned.class.getSimpleName();
    
    public static final String DAILY_PLAN_VARIABLES_TABLE = "SOS_JS_ORDER_VARIABLES";
    public static final String DAILY_PLAN_VARIABLES_TABLE_SEQUENCE = "SOS_JS_DPV_SEQ";
    public static final String DAILY_PLAN_VARIABLES_DBITEM = DBItemDailyPlanVariables.class.getSimpleName();
    
    /** Table INVENTORY_SCHEDULER_INSTANCES */
    public static final String DBITEM_INVENTORY_INSTANCES = DBItemInventoryInstance.class.getSimpleName();
    public static final String TABLE_INVENTORY_INSTANCES = "SOS_JS_SCHEDULER_INSTANCES";
    public static final String TABLE_INVENTORY_INSTANCES_SEQUENCE = "SOS_JS_SI_SEQ";
    
    /** Table INVENTORY_OPERATING_SYSTEM */
    public static final String DBITEM_OPERATING_SYSTEMS = DBItemOperatingSystem.class.getSimpleName();
    public static final String TABLE_OPERATING_SYSTEMS = "SOS_JS_OPERATING_SYSTEMS";
    public static final String TABLE_OPERATING_SYSTEMS_SEQUENCE = "SOS_JS_OS_ID_SEQ";
    
    /** Table AUDIT_LOG */
    public static final String DBITEM_AUDIT_LOG = DBItemAuditLog.class.getSimpleName();
    public static final String TABLE_AUDIT_LOG = "SOS_JS_AUDIT_LOG";
    public static final String TABLE_AUDIT_LOG_SEQUENCE = "SOS_JS_AUDIT_LOG_SEQ";
    
    /** Table JOC_CONFIGURATIONS */
    public static final String DBITEM_JOC_CONFIGURATIONS = DBItemJocConfiguration.class.getSimpleName();
    public static final String TABLE_JOC_CONFIGURATIONS = "SOS_JS_CONFIGURATIONS";
    public static final String TABLE_JOC_CONFIGURATIONS_SEQUENCE = "SOS_JS_CONFIGURATIONS_SEQ";
    
    /** Table DOCUMENTATION */
    public static final String DBITEM_DOCUMENTATION = DBItemDocumentation.class.getSimpleName();
    public static final String TABLE_DOCUMENTATION = "SOS_JS_DOCUMENTATIONS";
    public static final String TABLE_DOCUMENTATION_SEQUENCE = "SOS_JS_DOC_ID_SEQ";

    /** Table DOCUMENTATION_IMAGES */
    public static final String DBITEM_DOCUMENTATION_IMAGES = DBItemDocumentationImage.class.getSimpleName();
    public static final String TABLE_DOCUMENTATION_IMAGES = "SOS_JS_DOCUMENTATION_IMAGES";
    public static final String TABLE_DOCUMENTATION_IMAGES_SEQUENCE = "SOS_JS_DOC_IMG_ID_SEQ";

    /** Table DOCUMENTATION_USAGES */
    public static final String DBITEM_DOCUMENTATION_USAGE = DBItemDocumentationUsage.class.getSimpleName();
    public static final String TABLE_DOCUMENTATION_USAGE = "SOS_JS_DOCUMENTATION_USAGES";
    public static final String TABLE_DOCUMENTATION_USAGE_SEQUENCE = "SOS_JS_DOCU_ID_SEQ";
    
    /** Table CALENDARS */
    public static final String DBITEM_CALENDARS = DBItemCalendar.class.getSimpleName();
    public static final String TABLE_CALENDARS = "SOS_JS_CALENDARS";
    public static final String TABLE_CALENDARS_SEQUENCE = "SOS_JS_C_ID_SEQ";

    /** Table CALENDAR_USAGE */
    public static final String DBITEM_CALENDAR_USAGE = DBItemCalendarUsage.class.getSimpleName();
    public static final String TABLE_CALENDAR_USAGE = "SOS_JS_CALENDAR_USAGE";
    public static final String TABLE_CALENDAR_USAGE_SEQUENCE = "SOS_JS_CU_ID_SEQ";
    
//    public static final String DEFAULT_FOLDER = "/";
//    public static final Long DEFAULT_ID = 0L;
    public static final String DEFAULT_KEY = ".";

    public static SOSClassList getYadeClassMapping() {
        SOSClassList cl = new SOSClassList();
        return cl;
    }

    public static SOSClassList getHistoryClassMapping() {
        SOSClassList cl = new SOSClassList();
        cl.add(DBItemVariable.class);
        cl.add(DBItemOrder.class);
        cl.add(DBItemOrderStatus.class);
        cl.add(DBItemOrderStep.class);
        cl.add(DBItemLog.class);
        cl.add(DBItemMaster.class);
        cl.add(DBItemAgent.class);
        return cl;
    }

    public static SOSClassList getOrderInitatorClassMapping() {
        SOSClassList cl = new SOSClassList();
        cl.add(DBItemInventoryInstance.class);
        cl.add(DBItemCalendar.class);
        cl.add(DBItemDailyPlan.class);
        cl.add(DBItemDaysPlanned.class);
        cl.add(DBItemDailyPlanVariables.class);
        return cl;
    }

    public static SOSClassList getJocClassMapping() {
        SOSClassList cl = new SOSClassList();
        cl.add(DBItemInventoryInstance.class);
        cl.add(DBItemInventoryAgentInstance.class);
        cl.add(DBItemInventoryAgentCluster.class);
        cl.add(DBItemInventoryAgentClusterMember.class);
        cl.add(DBItemOperatingSystem.class);
        cl.add(DBItemAuditLog.class);
        cl.add(DBItemCalendar.class);
        cl.add(DBItemCalendarUsage.class);
        cl.add(DBItemDocumentation.class);
        cl.add(DBItemDocumentationImage.class);
        cl.add(DBItemDocumentationUsage.class);
        cl.add(DBItemJocConfiguration.class);
        cl.merge(getHistoryClassMapping().getClasses());
        cl.merge(getOrderInitatorClassMapping().getClasses());
        return cl;
    }

}