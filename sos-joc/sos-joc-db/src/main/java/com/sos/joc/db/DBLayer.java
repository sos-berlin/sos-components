package com.sos.joc.db;

import java.io.Serializable;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSClassList;
import com.sos.joc.db.audit.DBItemAuditLog;
import com.sos.joc.db.calendar.DBItemCalendar;
import com.sos.joc.db.calendar.DBItemCalendarUsage;
import com.sos.joc.db.joc.DBItemJocCluster;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.db.configuration.DBItemJocConfiguration;
import com.sos.joc.db.deployment.DBItemDeployedConfiguration;
import com.sos.joc.db.deployment.DBItemDeployedConfigurationHistory;
import com.sos.joc.db.deployment.DBItemJoinDepCfgDepCfgHistory;
import com.sos.joc.db.deployment.DBItemJoinJSDepCfgHistory;
import com.sos.joc.db.documentation.DBItemDocumentation;
import com.sos.joc.db.documentation.DBItemDocumentationImage;
import com.sos.joc.db.documentation.DBItemDocumentationUsage;
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.db.history.DBItemHistoryAgent;
import com.sos.joc.db.history.DBItemHistoryLog;
import com.sos.joc.db.history.DBItemHistoryController;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.db.history.DBItemHistoryTempLog;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryInstance;
import com.sos.joc.db.inventory.deprecated.agent.DBItemInventoryAgentCluster;
import com.sos.joc.db.inventory.deprecated.agent.DBItemInventoryAgentClusterMember;
import com.sos.joc.db.inventory.deprecated.agent.DBItemInventoryAgentInstance;
import com.sos.joc.db.orders.DBItemDailyPlan;
import com.sos.joc.db.orders.DBItemDailyPlanVariables;
import com.sos.joc.db.orders.DBItemDailyPlannedOrders;
import com.sos.joc.db.os.DBItemOperatingSystem;
import com.sos.joc.db.pgp.DBItemDepKeys;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;

public class DBLayer implements Serializable {

    private static final long serialVersionUID = 1L;

    /** JOC Tables */
    public static final String DBITEM_JOC_VARIABLE = DBItemJocVariable.class.getSimpleName();
    public static final String TABLE_JOC_VARIABLES = "JOC_VARIABLES";

    public static final String DBITEM_JOC_INSTANCES = DBItemJocInstance.class.getSimpleName();
    public static final String TABLE_JOC_INSTANCES = "JOC_INSTANCES";
    public static final String TABLE_JOC_INSTANCES_SEQUENCE = "SEQ_JI";

    public static final String DBITEM_JOC_CLUSTER = DBItemJocCluster.class.getSimpleName();
    public static final String TABLE_JOC_CLUSTER = "JOC_CLUSTER";

    public static final String DBITEM_AUDIT_LOG = DBItemAuditLog.class.getSimpleName();
    public static final String TABLE_AUDIT_LOG = "JOC_AUDIT_LOG";
    public static final String TABLE_AUDIT_LOG_SEQUENCE = "SEQ_JAL";

    public static final String DBITEM_JOC_CONFIGURATIONS = DBItemJocConfiguration.class.getSimpleName();
    public static final String TABLE_JOC_CONFIGURATIONS = "JOC_CONFIGURATIONS";
    public static final String TABLE_JOC_CONFIGURATIONS_SEQUENCE = "SEQ_JC";

    /** HISTORY Tables */
    public static final String DBITEM_HISTORY_CONTROLLER = DBItemHistoryController.class.getSimpleName();
    public static final String TABLE_HISTORY_CONTROLLERS = "HISTORY_MASTERS";
    public static final String TABLE_HISTORY_CONTROLLERS_SEQUENCE = "SEQ_HM";

    public static final String DBITEM_HISTORY_AGENT = DBItemHistoryAgent.class.getSimpleName();
    public static final String TABLE_HISTORY_AGENTS = "HISTORY_AGENTS";
    public static final String TABLE_HISTORY_AGENTS_SEQUENCE = "SEQ_HA";

    public static final String DBITEM_HISTORY_ORDER = DBItemHistoryOrder.class.getSimpleName();
    public static final String TABLE_HISTORY_ORDERS = "HISTORY_ORDERS";
    public static final String TABLE_HISTORY_ORDERS_SEQUENCE = "SEQ_HO";

    public static final String DBITEM_HISTORY_ORDER_STEP = DBItemHistoryOrderStep.class.getSimpleName();
    public static final String TABLE_HISTORY_ORDER_STEPS = "HISTORY_ORDER_STEPS";
    public static final String TABLE_HISTORY_ORDER_STEPS_SEQUENCE = "SEQ_HOS";

    public static final String DBITEM_HISTORY_LOG = DBItemHistoryLog.class.getSimpleName();
    public static final String TABLE_HISTORY_LOGS = "HISTORY_LOGS";
    public static final String TABLE_HISTORY_LOGS_SEQUENCE = "SEQ_HL";

    public static final String DBITEM_HISTORY_TEMP_LOG = DBItemHistoryTempLog.class.getSimpleName();
    public static final String TABLE_HISTORY_TEMP_LOGS = "HISTORY_TEMP_LOGS";

    /** Daily plan tables */
    public static final String DAILY_PLANNED_ORDERS_TABLE = "SOS_JS_ORDER_PLANNED_ORDER";
    public static final String DAILY_PLANNED_ORDERS_TABLE_SEQUENCE = "SOS_JS_DPO_SEQ";
    public static final String DAILY_PLANNED_ORDERS_DBITEM = DBItemDailyPlannedOrders.class.getSimpleName();

    public static final String DAILY_PLAN_TABLE = "SOS_JS_ORDER_DAILY_PLAN";
    public static final String DAILY_PLAN_TABLE_SEQUENCE = "SOS_JS_DPL_SEQ";
    public static final String DAILY_PLAN_DBITEM = DBItemDailyPlan.class.getSimpleName();

    public static final String DAILY_PLAN_VARIABLES_TABLE = "SOS_JS_ORDER_VARIABLES";
    public static final String DAILY_PLAN_VARIABLES_TABLE_SEQUENCE = "SOS_JS_DPV_SEQ";
    public static final String DAILY_PLAN_VARIABLES_DBITEM = DBItemDailyPlanVariables.class.getSimpleName();

    /** Inventory tables */
    public static final String DBITEM_INV_JS_INSTANCES = DBItemInventoryInstance.class.getSimpleName();
    public static final String TABLE_INV_JS_INSTANCES = "INV_JS_INSTANCES";
    public static final String TABLE_INV_JS_INSTANCES_SEQUENCE = "SEQ_IJI";

    public static final String DBITEM_INV_JS_OPERATING_SYSTEMS = DBItemOperatingSystem.class.getSimpleName();
    public static final String TABLE_INV_JS_OPERATING_SYSTEMS = "INV_JS_OPERATING_SYSTEMS";
    public static final String TABLE_INV_JS_OPERATING_SYSTEMS_SEQUENCE = "SEQ_IJOS";

    public static final String DBITEM_INV_CONFIGURATIONS = DBItemInventoryConfiguration.class.getSimpleName();
    public static final String TABLE_INV_CONFIGURATIONS = "INV_CONFIGURATIONS";
    public static final String TABLE_INV_CONFIGURATIONS_SEQUENCE = "SEQ_INVC";

    public static final String DBITEM_DOCUMENTATION = DBItemDocumentation.class.getSimpleName();
    public static final String TABLE_DOCUMENTATION = "INV_DOCUMENTATIONS";
    public static final String TABLE_DOCUMENTATION_SEQUENCE = "SEQ_IDOC"; // SEQ_ID sounds like trouble

    public static final String DBITEM_DOCUMENTATION_IMAGES = DBItemDocumentationImage.class.getSimpleName();
    public static final String TABLE_DOCUMENTATION_IMAGES = "INV_DOCUMENTATION_IMAGES";
    public static final String TABLE_DOCUMENTATION_IMAGES_SEQUENCE = "SEQ_IDI";

    public static final String DBITEM_DOCUMENTATION_USAGE = DBItemDocumentationUsage.class.getSimpleName();
    public static final String TABLE_DOCUMENTATION_USAGE = "INV_DOCUMENTATION_USAGES";
    public static final String TABLE_DOCUMENTATION_USAGE_SEQUENCE = "SEQ_IDU";

    public static final String DBITEM_CALENDARS = DBItemCalendar.class.getSimpleName();
    public static final String TABLE_CALENDARS = "INV_CALENDARS";
    public static final String TABLE_CALENDARS_SEQUENCE = "SEQ_IC";

    public static final String DBITEM_CALENDAR_USAGE = DBItemCalendarUsage.class.getSimpleName();
    public static final String TABLE_CALENDAR_USAGE = "INV_CALENDAR_USAGES";
    public static final String TABLE_CALENDAR_USAGE_SEQUENCE = "SEQ_ICU";

    /** Deployment tables */
    public static final String DBITEM_DEP_CONFIGURATIONS = DBItemDeployedConfiguration.class.getSimpleName();
    public static final String TABLE_DEP_CONFIGURATIONS = "DEP_CONFIGURATIONS";
    public static final String TABLE_DEP_CONFIGURATIONS_SEQUENCE = "DEV_CFG_SEQ";

    public static final String DBITEM_DEP_CONFIGURATION_HISTORY = DBItemDeployedConfigurationHistory.class.getSimpleName();
    public static final String TABLE_DEP_CONFIGURATION_HISTORY = "DEP_CONFIGURATIONS_HISTORY";
    public static final String TABLE_DEP_CONFIGURATION_HISTORY_SEQUENCE = "DEP_CFGH_SEQ";

    public static final String DBITEM_JOIN_DEP_CFG_DEP_CFG_HISTORY = DBItemJoinDepCfgDepCfgHistory.class.getSimpleName();
    public static final String TABLE_JOIN_DEP_CFG_DEP_CFG_HISTORY = "JOIN_DC_DCH";

    public static final String DBITEM_JOIN_INV_JS_DEP_CFG_HISTORY = DBItemJoinJSDepCfgHistory.class.getSimpleName();
    public static final String TABLE_JOIN_INV_JS_DEP_CFG_HISTORY = "JOIN_IJS_DCH";

    public static final String DBITEM_DEP_KEYS = DBItemDepKeys.class.getSimpleName();
    public static final String TABLE_DEP_KEYS = "DEP_KEYS";
    public static final String TABLE_DEP_KEYS_SEQUENCE = "DEP_K_SEQ";

    /** XMLEDITOR Tables */
    public static final String DBITEM_XML_EDITOR_CONFIGURATIONS = DBItemXmlEditorConfiguration.class.getSimpleName();
    public static final String TABLE_XML_EDITOR_CONFIGURATIONS = "XMLEDITOR_CONFIGURATIONS";
    public static final String TABLE_XML_EDITOR_CONFIGURATIONS_SEQUENCE = "SEQ_XEC";

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
        cl.add(DBItemHistoryController.class);
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
        cl.add(DBItemInventoryConfiguration.class);
        cl.add(DBItemDeployedConfiguration.class);
        cl.add(DBItemDeployedConfigurationHistory.class);
        cl.add(DBItemJoinDepCfgDepCfgHistory.class);
        cl.add(DBItemJoinJSDepCfgHistory.class);
        cl.add(DBItemDepKeys.class);
        cl.merge(getHistoryClassMapping().getClasses());
        cl.merge(getOrderInitatorClassMapping().getClasses());
        cl.add(DBItemXmlEditorConfiguration.class);
        cl.add(DBItemJocInstance.class);
        cl.add(DBItemJocCluster.class);
        return cl;
    }

}