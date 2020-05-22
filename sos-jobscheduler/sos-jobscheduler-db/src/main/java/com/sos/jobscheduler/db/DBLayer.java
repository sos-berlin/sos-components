package com.sos.jobscheduler.db;

import java.io.Serializable;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSClassList;
import com.sos.jobscheduler.db.audit.DBItemAuditLog;
import com.sos.jobscheduler.db.calendar.DBItemCalendar;
import com.sos.jobscheduler.db.calendar.DBItemCalendarUsage;
import com.sos.jobscheduler.db.cluster.DBItemJocCluster;
import com.sos.jobscheduler.db.joc.DBItemJocInstance;
import com.sos.jobscheduler.db.configuration.DBItemJocConfiguration;
import com.sos.jobscheduler.db.documentation.DBItemDocumentation;
import com.sos.jobscheduler.db.documentation.DBItemDocumentationImage;
import com.sos.jobscheduler.db.documentation.DBItemDocumentationUsage;
import com.sos.jobscheduler.db.joc.DBItemJocVariable;
import com.sos.jobscheduler.db.history.DBItemHistoryAgent;
import com.sos.jobscheduler.db.history.DBItemHistoryLog;
import com.sos.jobscheduler.db.history.DBItemHistoryMaster;
import com.sos.jobscheduler.db.history.DBItemHistoryOrder;
import com.sos.jobscheduler.db.history.DBItemHistoryOrderStep;
import com.sos.jobscheduler.db.history.DBItemHistoryTempLog;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.jobscheduler.db.inventory.DBItemJSCfgToJSMapping;
import com.sos.jobscheduler.db.inventory.DBItemJSConfiguration;
import com.sos.jobscheduler.db.inventory.DBItemJSConfigurationMapping;
import com.sos.jobscheduler.db.inventory.DBItemJSDraftObject;
import com.sos.jobscheduler.db.inventory.DBItemJSObject;
import com.sos.jobscheduler.db.inventory.DBItemJSOperationHistory;
import com.sos.jobscheduler.db.inventory.agent.DBItemInventoryAgentCluster;
import com.sos.jobscheduler.db.inventory.agent.DBItemInventoryAgentClusterMember;
import com.sos.jobscheduler.db.inventory.agent.DBItemInventoryAgentInstance;
import com.sos.jobscheduler.db.orders.DBItemDailyPlan;
import com.sos.jobscheduler.db.orders.DBItemDailyPlanVariables;
import com.sos.jobscheduler.db.orders.DBItemDailyPlannedOrders;
import com.sos.jobscheduler.db.os.DBItemOperatingSystem;
import com.sos.jobscheduler.db.pgp.DBItemJSKeys;
import com.sos.jobscheduler.db.xmleditor.DBItemXmlEditorConfiguration;

public class DBLayer implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TABLE_JOC_VARIABLES = "JOC_VARIABLES";
    public static final String DBITEM_JOC_VARIABLE = DBItemJocVariable.class.getSimpleName();

    public static final String TABLE_HISTORY_ORDERS = "HISTORY_ORDERS";
    public static final String TABLE_HISTORY_ORDERS_SEQUENCE = "SEQ_HO";
    public static final String DBITEM_HISTORY_ORDER = DBItemHistoryOrder.class.getSimpleName();

    public static final String TABLE_HISTORY_ORDER_STEPS = "HISTORY_ORDER_STEPS";
    public static final String TABLE_HISTORY_ORDER_STEPS_SEQUENCE = "SEQ_HOS";
    public static final String DBITEM_HISTORY_ORDER_STEP = DBItemHistoryOrderStep.class.getSimpleName();

    public static final String TABLE_HISTORY_LOGS = "HISTORY_LOGS";
    public static final String TABLE_HISTORY_LOGS_SEQUENCE = "SEQ_HL";
    public static final String DBITEM_HISTORY_LOG = DBItemHistoryLog.class.getSimpleName();

    public static final String TABLE_HISTORY_TEMP_LOGS = "HISTORY_TEMP_LOGS";
    public static final String DBITEM_HISTORY_TEMP_LOG = DBItemHistoryTempLog.class.getSimpleName();

    public static final String TABLE_HISTORY_MASTERS = "HISTORY_MASTERS";
    public static final String TABLE_HISTORY_MASTERS_SEQUENCE = "SEQ_HM";
    public static final String DBITEM_HISTORY_MASTER = DBItemHistoryMaster.class.getSimpleName();

    public static final String TABLE_HISTORY_AGENTS = "HISTORY_AGENTS";
    public static final String TABLE_HISTORY_AGENTS_SEQUENCE = "SEQ_HA";
    public static final String DBITEM_HISTORY_AGENT = DBItemHistoryAgent.class.getSimpleName();

    public static final String DAILY_PLANNED_ORDERS_TABLE = "SOS_JS_ORDER_PLANNED_ORDER";
    public static final String DAILY_PLANNED_ORDERS_TABLE_SEQUENCE = "SOS_JS_DPO_SEQ";
    public static final String DAILY_PLANNED_ORDERS_DBITEM = DBItemDailyPlannedOrders.class.getSimpleName();

    public static final String DAILY_PLAN_TABLE = "SOS_JS_ORDER_DAILY_PLAN";
    public static final String DAILY_PLAN_TABLE_SEQUENCE = "SOS_JS_DPL_SEQ";
    public static final String DAILY_PLAN_DBITEM = DBItemDailyPlan.class.getSimpleName();

    public static final String DAILY_PLAN_VARIABLES_TABLE = "SOS_JS_ORDER_VARIABLES";
    public static final String DAILY_PLAN_VARIABLES_TABLE_SEQUENCE = "SOS_JS_DPV_SEQ";
    public static final String DAILY_PLAN_VARIABLES_DBITEM = DBItemDailyPlanVariables.class.getSimpleName();

    /** Table SOS_JS_SCHEDULER_INSTANCES */
    public static final String DBITEM_INVENTORY_INSTANCES = DBItemInventoryInstance.class.getSimpleName();
    public static final String TABLE_INVENTORY_INSTANCES = "SOS_JS_SCHEDULER_INSTANCES";
    public static final String TABLE_INVENTORY_INSTANCES_SEQUENCE = "SOS_JS_SI_SEQ";

    /** Table SOS_JS_OPERATING_SYSTEMS */
    public static final String DBITEM_OPERATING_SYSTEMS = DBItemOperatingSystem.class.getSimpleName();
    public static final String TABLE_OPERATING_SYSTEMS = "SOS_JS_OPERATING_SYSTEMS";
    public static final String TABLE_OPERATING_SYSTEMS_SEQUENCE = "SOS_JS_OS_ID_SEQ";

    /** Table SOS_JS_AUDIT_LOG */
    public static final String DBITEM_AUDIT_LOG = DBItemAuditLog.class.getSimpleName();
    public static final String TABLE_AUDIT_LOG = "SOS_JS_AUDIT_LOG";
    public static final String TABLE_AUDIT_LOG_SEQUENCE = "SOS_JS_AUDIT_LOG_SEQ";

    /** Table SOS_JS_JOC_CONFIGURATIONS */
    public static final String DBITEM_JOC_CONFIGURATIONS = DBItemJocConfiguration.class.getSimpleName();
    public static final String TABLE_JOC_CONFIGURATIONS = "SOS_JS_JOC_CONFIGURATIONS";
    public static final String TABLE_JOC_CONFIGURATIONS_SEQUENCE = "SOS_JS_JOC_CONFIGURATIONS_SEQ";

    /** Table SOS_JS_DOCUMENTATIONS */
    public static final String DBITEM_DOCUMENTATION = DBItemDocumentation.class.getSimpleName();
    public static final String TABLE_DOCUMENTATION = "SOS_JS_DOCUMENTATIONS";
    public static final String TABLE_DOCUMENTATION_SEQUENCE = "SOS_JS_DOC_ID_SEQ";

    /** Table SOS_JS_DOCUMENTATION_IMAGES */
    public static final String DBITEM_DOCUMENTATION_IMAGES = DBItemDocumentationImage.class.getSimpleName();
    public static final String TABLE_DOCUMENTATION_IMAGES = "SOS_JS_DOCUMENTATION_IMAGES";
    public static final String TABLE_DOCUMENTATION_IMAGES_SEQUENCE = "SOS_JS_DOC_IMG_ID_SEQ";

    /** Table SOS_JS_DOCUMENTATION_USAGES */
    public static final String DBITEM_DOCUMENTATION_USAGE = DBItemDocumentationUsage.class.getSimpleName();
    public static final String TABLE_DOCUMENTATION_USAGE = "SOS_JS_DOCUMENTATION_USAGES";
    public static final String TABLE_DOCUMENTATION_USAGE_SEQUENCE = "SOS_JS_DOCU_ID_SEQ";

    /** Table SOS_JS_CALENDARS */
    public static final String DBITEM_CALENDARS = DBItemCalendar.class.getSimpleName();
    public static final String TABLE_CALENDARS = "SOS_JS_CALENDARS";
    public static final String TABLE_CALENDARS_SEQUENCE = "SOS_JS_C_ID_SEQ";

    /** Table SOS_JS_CALENDAR_USAGE */
    public static final String DBITEM_CALENDAR_USAGE = DBItemCalendarUsage.class.getSimpleName();
    public static final String TABLE_CALENDAR_USAGE = "SOS_JS_CALENDAR_USAGE";
    public static final String TABLE_CALENDAR_USAGE_SEQUENCE = "SOS_JS_CU_ID_SEQ";

    /** Tables for JobScheduler Object configurations and deployment */
    /** Table SOS_JS_DRAFT_OBJECTS */
    public static final String DBITEM_JS_DRAFT_OBJECTS = DBItemJSDraftObject.class.getSimpleName();
    public static final String TABLE_JS_DRAFT_OBJECTS = "SOS_JS_DRAFT_OBJECTS";
    public static final String TABLE_JS_DRAFT_OBJECTS_SEQUENCE = "SOS_JS_DOB_SEQ";
    /** Table SOS_JS_OBJECTS */
    public static final String DBITEM_JS_OBJECTS = DBItemJSObject.class.getSimpleName();
    public static final String TABLE_JS_OBJECTS = "SOS_JS_OBJECTS";
    public static final String TABLE_JS_OBJECTS_SEQUENCE = "SOS_JS_OB_SEQ";
    /** Table SOS_JS_CONFIGURATIONS */
    public static final String DBITEM_JS_CONFIGURATION = DBItemJSConfiguration.class.getSimpleName();
    public static final String TABLE_JS_CONFIGURATION = "SOS_JS_CONFIGURATIONS";
    public static final String TABLE_JS_CONFIGURATION_SEQUENCE = "SOS_JS_C_SEQ";
    /** Table SOS_JS_CONFIGURATION_MAPPING */
    public static final String DBITEM_JS_CONFIGURATION_MAPPING = DBItemJSConfigurationMapping.class.getSimpleName();
    public static final String TABLE_JS_CONFIGURATION_MAPPING = "SOS_JS_CONFIGURATION_MAPPING";
    /** Table SOS_JS_OPERATION_HISTORY */
    public static final String DBITEM_JS_OPERATION_HISTORY = DBItemJSOperationHistory.class.getSimpleName();
    public static final String TABLE_JS_OPERATION_HISTORY = "SOS_JS_OPERATION_HISTORY";

    /** Table SOS_JS_CFG_TO_JS_MAPPING */
    public static final String DBITEM_JS_CONFIG_TO_SCHEDULER_MAPPING = DBItemJSCfgToJSMapping.class.getSimpleName();
    public static final String TABLE_JS_CONFIG_TO_SCHEDULER_MAPPING = "SOS_JS_CFG_TO_JS_MAPPING";
    /** Table SOS_JS_KEYS */
    public static final String DBITEM_JS_KEYS = DBItemJSKeys.class.getSimpleName();
    public static final String TABLE_JS_KEYS = "SOS_JS_KEYS";
    public static final String TABLE_JS_KEYS_SEQUENCE = "SOS_JS_K_SEQ";

    /** Table XML_EDITOR_CONFIGURATIONS */
    public static final String DBITEM_XML_EDITOR_CONFIGURATIONS = DBItemXmlEditorConfiguration.class.getSimpleName();
    public static final String TABLE_XML_EDITOR_CONFIGURATIONS = "XMLEDITOR_CONFIGURATIONS";
    public static final String TABLE_XML_EDITOR_CONFIGURATIONS_SEQUENCE = "SEQ_XEC";

    /** Table JOC_INSTANCES */
    public static final String DBITEM_JOC_INSTANCES = DBItemJocInstance.class.getSimpleName();
    public static final String TABLE_JOC_INSTANCES = "JOC_INSTANCES";
    public static final String TABLE_JOC_INSTANCES_SEQUENCE = "SEQ_JI";

    /** Table JOC_CLUSTER */
    public static final String DBITEM_JOC_CLUSTER = DBItemJocCluster.class.getSimpleName();
    public static final String TABLE_JOC_CLUSTER = "JOC_CLUSTER";

    // public static final String DEFAULT_FOLDER = "/";
    // public static final Long DEFAULT_ID = 0L;
    public static final String DEFAULT_KEY = ".";

    private SOSHibernateSession session;

    public DBLayer(SOSHibernateSession session) {
        this.session = session;
    }

    public SOSHibernateSession getSession() {
        return session;
    }

    public static SOSClassList getYadeClassMapping() {
        SOSClassList cl = new SOSClassList();
        return cl;
    }

    public static SOSClassList getHistoryClassMapping() {
        SOSClassList cl = new SOSClassList();
        cl.add(DBItemJocVariable.class);
        cl.add(DBItemHistoryOrder.class);
        cl.add(DBItemHistoryOrderStep.class);
        cl.add(DBItemHistoryLog.class);
        cl.add(DBItemHistoryTempLog.class);
        cl.add(DBItemHistoryMaster.class);
        cl.add(DBItemHistoryAgent.class);
        return cl;
    }

    public static SOSClassList getOrderInitatorClassMapping() {
        SOSClassList cl = new SOSClassList();
        cl.add(DBItemInventoryInstance.class);
        cl.add(DBItemCalendar.class);
        cl.add(DBItemDailyPlan.class);
        cl.add(DBItemDailyPlannedOrders.class);
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
        cl.add(DBItemJSDraftObject.class);
        cl.add(DBItemJSObject.class);
        cl.add(DBItemJSConfiguration.class);
        cl.add(DBItemJSConfigurationMapping.class);
        cl.add(DBItemJSOperationHistory.class);
        cl.add(DBItemJSCfgToJSMapping.class);
        cl.add(DBItemJSKeys.class);
        cl.merge(getHistoryClassMapping().getClasses());
        cl.merge(getOrderInitatorClassMapping().getClasses());
        cl.add(DBItemXmlEditorConfiguration.class);
        cl.add(DBItemJocInstance.class);
        return cl;
    }

}