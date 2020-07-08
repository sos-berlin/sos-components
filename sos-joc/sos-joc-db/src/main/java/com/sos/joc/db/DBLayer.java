package com.sos.joc.db;

import java.io.Serializable;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSClassList;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.joc.DBItemJocCluster;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.db.deployment.DBItemDepKeys;
import com.sos.joc.db.deployment.DBItemDeployedConfiguration;
import com.sos.joc.db.deployment.DBItemDeployedConfigurationHistory;
import com.sos.joc.db.deployment.DBItemJoinDepCfgDepCfgHistory;
import com.sos.joc.db.deployment.DBItemJoinJSDepCfgHistory;
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.db.history.DBItemHistoryAgent;
import com.sos.joc.db.history.DBItemHistoryLog;
import com.sos.joc.db.history.DBItemHistoryController;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.db.history.DBItemHistoryTempLog;
import com.sos.joc.db.inventory.DBItemInventoryAgentCluster;
import com.sos.joc.db.inventory.DBItemInventoryAgentClusterMember;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.DBItemInventoryJobClass;
import com.sos.joc.db.inventory.DBItemInventoryJunction;
import com.sos.joc.db.inventory.DBItemInventoryLock;
import com.sos.joc.db.inventory.DBItemInventoryOperatingSystem;
import com.sos.joc.db.inventory.DBItemInventoryWorkflow;
import com.sos.joc.db.inventory.DBItemInventoryWorkflowJob;
import com.sos.joc.db.inventory.DBItemInventoryWorkflowJobArgument;
import com.sos.joc.db.inventory.DBItemInventoryWorkflowJobNode;
import com.sos.joc.db.inventory.DBItemInventoryWorkflowJobNodeArgument;
import com.sos.joc.db.inventory.DBItemInventoryWorkflowJunction;
import com.sos.joc.db.orders.DBItemDailyPlan;
import com.sos.joc.db.orders.DBItemDailyPlanVariables;
import com.sos.joc.db.orders.DBItemDailyPlannedOrders;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;

public class DBLayer implements Serializable {

    private static final long serialVersionUID = 1L;

    /** JOC Tables */
    public static final String DBITEM_JOC_VARIABLE = DBItemJocVariable.class.getSimpleName();
    public static final String TABLE_JOC_VARIABLES = "JOC_VARIABLES";

    public static final String DBITEM_JOC_INSTANCES = DBItemJocInstance.class.getSimpleName();
    public static final String TABLE_JOC_INSTANCES = "JOC_INSTANCES";
    public static final String TABLE_JOC_INSTANCES_SEQUENCE = "SEQ_JOC_I";

    public static final String DBITEM_JOC_CLUSTER = DBItemJocCluster.class.getSimpleName();
    public static final String TABLE_JOC_CLUSTER = "JOC_CLUSTER";

    public static final String DBITEM_JOC_AUDIT_LOG = DBItemJocAuditLog.class.getSimpleName();
    public static final String TABLE_JOC_AUDIT_LOG = "JOC_AUDIT_LOG";
    public static final String TABLE_JOC_AUDIT_LOG_SEQUENCE = "SEQ_JOC_AL";

    public static final String DBITEM_JOC_CONFIGURATIONS = DBItemJocConfiguration.class.getSimpleName();
    public static final String TABLE_JOC_CONFIGURATIONS = "JOC_CONFIGURATIONS";
    public static final String TABLE_JOC_CONFIGURATIONS_SEQUENCE = "SEQ_JOC_C";

    /** HISTORY Tables */
    public static final String DBITEM_HISTORY_CONTROLLER = DBItemHistoryController.class.getSimpleName();
    public static final String TABLE_HISTORY_CONTROLLERS = "HISTORY_MASTERS";
    public static final String TABLE_HISTORY_CONTROLLERS_SEQUENCE = "SEQ_HISTORY_M";

    public static final String DBITEM_HISTORY_AGENT = DBItemHistoryAgent.class.getSimpleName();
    public static final String TABLE_HISTORY_AGENTS = "HISTORY_AGENTS";
    public static final String TABLE_HISTORY_AGENTS_SEQUENCE = "SEQ_HISTORY_A";

    public static final String DBITEM_HISTORY_ORDER = DBItemHistoryOrder.class.getSimpleName();
    public static final String TABLE_HISTORY_ORDERS = "HISTORY_ORDERS";
    public static final String TABLE_HISTORY_ORDERS_SEQUENCE = "SEQ_HISTORY_O";

    public static final String DBITEM_HISTORY_ORDER_STEP = DBItemHistoryOrderStep.class.getSimpleName();
    public static final String TABLE_HISTORY_ORDER_STEPS = "HISTORY_ORDER_STEPS";
    public static final String TABLE_HISTORY_ORDER_STEPS_SEQUENCE = "SEQ_HISTORY_OS";

    public static final String DBITEM_HISTORY_LOG = DBItemHistoryLog.class.getSimpleName();
    public static final String TABLE_HISTORY_LOGS = "HISTORY_LOGS";
    public static final String TABLE_HISTORY_LOGS_SEQUENCE = "SEQ_HISTORY_L";

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
    public static final String DBITEM_INV_OPERATING_SYSTEMS = DBItemInventoryOperatingSystem.class.getSimpleName();
    public static final String TABLE_INV_OPERATING_SYSTEMS = "INV_OPERATING_SYSTEMS";
    public static final String TABLE_INV_OPERATING_SYSTEMS_SEQUENCE = "SEQ_INV_OS";

    public static final String DBITEM_INV_JS_INSTANCES = DBItemInventoryJSInstance.class.getSimpleName();
    public static final String TABLE_INV_JS_INSTANCES = "INV_JS_INSTANCES";
    public static final String TABLE_INV_JS_INSTANCES_SEQUENCE = "SEQ_INV_JI";

    public static final String DBITEM_INV_CONFIGURATIONS = DBItemInventoryConfiguration.class.getSimpleName();
    public static final String TABLE_INV_CONFIGURATIONS = "INV_CONFIGURATIONS";
    public static final String TABLE_INV_CONFIGURATIONS_SEQUENCE = "SEQ_INV_C";

    public static final String DBITEM_INV_AGENT_CLUSTERS = DBItemInventoryAgentCluster.class.getSimpleName();
    public static final String TABLE_INV_AGENT_CLUSTERS = "INV_AGENT_CLUSTERS";

    public static final String DBITEM_INV_AGENT_CLUSTER_MEMBERS = DBItemInventoryAgentClusterMember.class.getSimpleName();
    public static final String TABLE_INV_AGENT_CLUSTER_MEMBERS = "INV_AGENT_CLUSTER_MEMBERS";
    public static final String TABLE_INV_AGENT_CLUSTER_MEMBERS_SEQUENCE = "SEQ_INV_ACM";

    public static final String DBITEM_INV_JOB_CLASSES = DBItemInventoryJobClass.class.getSimpleName();
    public static final String TABLE_INV_JOB_CLASSES = "INV_JOB_CLASSES";

    public static final String DBITEM_INV_JUNCTIONS = DBItemInventoryJunction.class.getSimpleName();
    public static final String TABLE_INV_JUNCTIONS = "INV_JUNCTIONS";

    public static final String DBITEM_INV_LOCKS = DBItemInventoryLock.class.getSimpleName();
    public static final String TABLE_INV_LOCKS = "INV_LOCKS";

    public static final String DBITEM_INV_WORKFLOWS = DBItemInventoryWorkflow.class.getSimpleName();
    public static final String TABLE_INV_WORKFLOWS = "INV_WORKFLOWS";

    public static final String DBITEM_INV_WORKFLOW_JOBS = DBItemInventoryWorkflowJob.class.getSimpleName();
    public static final String TABLE_INV_WORKFLOW_JOBS = "INV_WORKFLOW_JOBS";

    public static final String DBITEM_INV_WORKFLOW_JOB_ARGUMENTS = DBItemInventoryWorkflowJobArgument.class.getSimpleName();
    public static final String TABLE_INV_WORKFLOW_JOB_ARGUMENTS = "INV_WORKFLOW_JOB_ARGS";
    public static final String TABLE_INV_WORKFLOW_JOB_ARGUMENTS_SEQUENCE = "SEQ_INV_WJA";

    public static final String DBITEM_INV_WORKFLOW_JOB_NODES = DBItemInventoryWorkflowJobNode.class.getSimpleName();
    public static final String TABLE_INV_WORKFLOW_JOB_NODES = "INV_WORKFLOW_JOB_NODES";
    public static final String TABLE_INV_WORKFLOW_JOB_NODES_SEQUENCE = "SEQ_INV_WJN";

    public static final String DBITEM_INV_WORKFLOW_JOB_NODE_ARGUMENTS = DBItemInventoryWorkflowJobNodeArgument.class.getSimpleName();
    public static final String TABLE_INV_WORKFLOW_JOB_NODE_ARGUMENTS = "INV_WORKFLOW_JOB_NODE_ARGS";
    public static final String TABLE_INV_WORKFLOW_JOB_NODE_ARGUMENTS_SEQUENCE = "SEQ_INV_WJNA";

    public static final String DBITEM_INV_WORKFLOW_JUNCTIONS = DBItemInventoryWorkflowJunction.class.getSimpleName();
    public static final String TABLE_INV_WORKFLOW_JUNCTIONS = "INV_WORKFLOW_JUNCTIONS";
    public static final String TABLE_INV_WORKFLOW_JUNCTIONS_SEQUENCE = "SEQ_INV_WJ";

    public static final String DBITEM_DOCUMENTATION = com.sos.joc.db.inventory.deprecated.documentation.DBItemDocumentation.class.getSimpleName();
    public static final String TABLE_DOCUMENTATION = "INV_DOCUMENTATIONS";
    public static final String TABLE_DOCUMENTATION_SEQUENCE = "SEQ_INV_D";

    public static final String DBITEM_DOCUMENTATION_IMAGES = com.sos.joc.db.inventory.deprecated.documentation.DBItemDocumentationImage.class
            .getSimpleName();
    public static final String TABLE_DOCUMENTATION_IMAGES = "INV_DOCUMENTATION_IMAGES";
    public static final String TABLE_DOCUMENTATION_IMAGES_SEQUENCE = "SEQ_INV_DI";

    public static final String DBITEM_DOCUMENTATION_USAGE = com.sos.joc.db.inventory.deprecated.documentation.DBItemDocumentationUsage.class
            .getSimpleName();
    public static final String TABLE_DOCUMENTATION_USAGE = "INV_DOCUMENTATION_USAGES";
    public static final String TABLE_DOCUMENTATION_USAGE_SEQUENCE = "SEQ_INV_DU";

    public static final String DBITEM_CALENDARS = com.sos.joc.db.inventory.deprecated.calendar.DBItemCalendar.class.getSimpleName();
    public static final String TABLE_CALENDARS = "INV_CALENDARS";
    public static final String TABLE_CALENDARS_SEQUENCE = "SEQ_INV_C";

    public static final String DBITEM_CALENDAR_USAGE = com.sos.joc.db.inventory.deprecated.calendar.DBItemCalendarUsage.class.getSimpleName();
    public static final String TABLE_CALENDAR_USAGE = "INV_CALENDAR_USAGES";
    public static final String TABLE_CALENDAR_USAGE_SEQUENCE = "SEQ_INV_CU";

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
        cl.add(DBItemInventoryJSInstance.class);
        cl.add(com.sos.joc.db.inventory.deprecated.calendar.DBItemCalendar.class);
        cl.add(DBItemDailyPlan.class);
        cl.add(DBItemDailyPlannedOrders.class);
        cl.add(DBItemDailyPlanVariables.class);
        return cl;
    }

    public static SOSClassList getJocClassMapping() {
        SOSClassList cl = new SOSClassList();

        cl.add(DBItemInventoryOperatingSystem.class);
        cl.add(DBItemInventoryJSInstance.class);
        cl.add(DBItemInventoryConfiguration.class);
        cl.add(DBItemInventoryAgentCluster.class);
        cl.add(DBItemInventoryAgentClusterMember.class);
        cl.add(DBItemInventoryJobClass.class);
        cl.add(DBItemInventoryJunction.class);
        cl.add(DBItemInventoryLock.class);
        cl.add(DBItemInventoryWorkflow.class);
        cl.add(DBItemInventoryWorkflowJob.class);
        cl.add(DBItemInventoryWorkflowJobArgument.class);
        cl.add(DBItemInventoryWorkflowJobNode.class);
        cl.add(DBItemInventoryWorkflowJobNodeArgument.class);
        cl.add(DBItemInventoryWorkflowJunction.class);
        cl.add(com.sos.joc.db.inventory.deprecated.agent.DBItemInventoryAgentInstance.class);
        cl.add(com.sos.joc.db.inventory.deprecated.agent.DBItemInventoryAgentCluster.class);
        cl.add(com.sos.joc.db.inventory.deprecated.agent.DBItemInventoryAgentClusterMember.class);
        cl.add(com.sos.joc.db.inventory.deprecated.calendar.DBItemCalendar.class);
        cl.add(com.sos.joc.db.inventory.deprecated.calendar.DBItemCalendarUsage.class);
        cl.add(com.sos.joc.db.inventory.deprecated.documentation.DBItemDocumentation.class);
        cl.add(com.sos.joc.db.inventory.deprecated.documentation.DBItemDocumentationImage.class);
        cl.add(com.sos.joc.db.inventory.deprecated.documentation.DBItemDocumentationUsage.class);

        cl.add(DBItemJocConfiguration.class);
        cl.add(DBItemJocInstance.class);
        cl.add(DBItemJocCluster.class);
        cl.add(DBItemJocAuditLog.class);

        cl.add(DBItemDeployedConfiguration.class);
        cl.add(DBItemDeployedConfigurationHistory.class);
        cl.add(DBItemJoinDepCfgDepCfgHistory.class);
        cl.add(DBItemJoinJSDepCfgHistory.class);
        cl.add(DBItemDepKeys.class);

        cl.merge(getHistoryClassMapping().getClasses());
        cl.merge(getOrderInitatorClassMapping().getClasses());

        cl.add(DBItemXmlEditorConfiguration.class);
        return cl;
    }

}