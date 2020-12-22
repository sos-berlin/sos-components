package com.sos.joc.db;

import java.io.Serializable;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSClassList;
import com.sos.joc.db.deployment.DBItemDepCommitIds;
import com.sos.joc.db.deployment.DBItemDepConfiguration;
import com.sos.joc.db.deployment.DBItemDepKeys;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDepVersions;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.deployment.DBItemDeploymentSubmission;
import com.sos.joc.db.history.DBItemHistoryAgent;
import com.sos.joc.db.history.DBItemHistoryController;
import com.sos.joc.db.history.DBItemHistoryLog;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.DBItemHistoryOrderState;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.db.history.DBItemHistoryTempLog;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryAgentName;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.DBItemInventoryOperatingSystem;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.joc.DBItemJocCluster;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.db.joc.DBItemJocLock;
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.db.orders.DBItemDailyPlanSubmissionHistory;
import com.sos.joc.db.orders.DBItemDailyPlanVariables;
import com.sos.joc.db.search.DBItemSearchWorkflow;
import com.sos.joc.db.search.DBItemSearchWorkflow2DeploymentHistory;
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

    public static final String DBITEM_JOC_LOCKS = DBItemJocLock.class.getSimpleName();
    public static final String TABLE_JOC_LOCKS = "JOC_LOCKS";
    public static final String TABLE_JOC_LOCKS_SEQUENCE = "SEQ_JOC_L";

    /** HISTORY Tables */
    public static final String DBITEM_HISTORY_CONTROLLER = DBItemHistoryController.class.getSimpleName();
    public static final String TABLE_HISTORY_CONTROLLERS = "HISTORY_CONTROLLERS";
    public static final String TABLE_HISTORY_CONTROLLERS_SEQUENCE = "SEQ_HISTORY_C";

    public static final String DBITEM_HISTORY_AGENT = DBItemHistoryAgent.class.getSimpleName();
    public static final String TABLE_HISTORY_AGENTS = "HISTORY_AGENTS";
    public static final String TABLE_HISTORY_AGENTS_SEQUENCE = "SEQ_HISTORY_A";

    public static final String DBITEM_HISTORY_ORDER = DBItemHistoryOrder.class.getSimpleName();
    public static final String TABLE_HISTORY_ORDERS = "HISTORY_ORDERS";
    public static final String TABLE_HISTORY_ORDERS_SEQUENCE = "SEQ_HISTORY_O";

    public static final String DBITEM_HISTORY_ORDER_STATE = DBItemHistoryOrderState.class.getSimpleName();
    public static final String TABLE_HISTORY_ORDER_STATES = "HISTORY_ORDER_STATES";
    public static final String TABLE_HISTORY_ORDER_STATES_SEQUENCE = "SEQ_HISTORY_OSTATES";

    public static final String DBITEM_HISTORY_ORDER_STEP = DBItemHistoryOrderStep.class.getSimpleName();
    public static final String TABLE_HISTORY_ORDER_STEPS = "HISTORY_ORDER_STEPS";
    public static final String TABLE_HISTORY_ORDER_STEPS_SEQUENCE = "SEQ_HISTORY_OSTEPS";

    public static final String DBITEM_HISTORY_LOG = DBItemHistoryLog.class.getSimpleName();
    public static final String TABLE_HISTORY_LOGS = "HISTORY_LOGS";
    public static final String TABLE_HISTORY_LOGS_SEQUENCE = "SEQ_HISTORY_L";

    public static final String DBITEM_HISTORY_TEMP_LOG = DBItemHistoryTempLog.class.getSimpleName();
    public static final String TABLE_HISTORY_TEMP_LOGS = "HISTORY_TEMP_LOGS";

    /** Daily plan tables */
    public static final String DAILY_PLAN_ORDERS_TABLE = "DPL_ORDERS";
    public static final String DAILY_PLAN_ORDERS_TABLE_SEQUENCE = "SEQ_DPL_ORDERS";
    public static final String DAILY_PLAN_ORDERS_DBITEM = DBItemDailyPlanOrders.class.getSimpleName();

    public static final String DAILY_PLAN_SUBMISSION_HISTORY_TABLE = "DPL_SUBMISSION_HISTORY";
    public static final String DAILY_PLAN_SUBMISSION_HISTORY_SEQUENCE = "DPL_SUBHISTORY_SEQ";
    public static final String DAILY_PLAN_SUBMISSION_HISTORY_DBITEM = DBItemDailyPlanSubmissionHistory.class.getSimpleName();

    public static final String DAILY_PLAN_VARIABLES_TABLE = "DPL_ORDER_VARIABLES";
    public static final String DAILY_PLAN_VARIABLES_TABLE_SEQUENCE = "DPL_ORDER_VARIABLES_SEQ";
    public static final String DAILY_PLAN_VARIABLES_DBITEM = DBItemDailyPlanVariables.class.getSimpleName();

    /** Inventory tables */
    public static final String DBITEM_INV_OPERATING_SYSTEMS = DBItemInventoryOperatingSystem.class.getSimpleName();
    public static final String TABLE_INV_OPERATING_SYSTEMS = "INV_OPERATING_SYSTEMS";
    public static final String TABLE_INV_OPERATING_SYSTEMS_SEQUENCE = "SEQ_INV_OS";

    public static final String DBITEM_INV_JS_INSTANCES = DBItemInventoryJSInstance.class.getSimpleName();
    public static final String TABLE_INV_JS_INSTANCES = "INV_JS_INSTANCES";
    public static final String TABLE_INV_JS_INSTANCES_SEQUENCE = "SEQ_INV_JI";

    public static final String DBITEM_INV_AGENT_INSTANCES = DBItemInventoryAgentInstance.class.getSimpleName();
    public static final String TABLE_INV_AGENT_INSTANCES = "INV_AGENT_INSTANCES";
    public static final String TABLE_INV_AGENT_INSTANCES_SEQUENCE = "SEQ_INV_AI";

    public static final String DBITEM_INV_AGENT_NAMES = DBItemInventoryAgentName.class.getSimpleName();
    public static final String TABLE_INV_AGENT_NAMES = "INV_AGENT_NAME_ALIASES";

    public static final String DBITEM_INV_CONFIGURATIONS = DBItemInventoryConfiguration.class.getSimpleName();
    public static final String TABLE_INV_CONFIGURATIONS = "INV_CONFIGURATIONS";
    public static final String TABLE_INV_CONFIGURATIONS_SEQUENCE = "SEQ_INV_C";

    public static final String DBITEM_INV_RELEASED_CONFIGURATIONS = DBItemInventoryReleasedConfiguration.class.getSimpleName();
    public static final String TABLE_INV_RELEASED_CONFIGURATIONS = "INV_RELEASED_CONFIGURATIONS";
    public static final String TABLE_INV_RELEASED_CONFIGURATIONS_SEQUENCE = "SEQ_INV_RC";

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

    /** Search (helper) tables */
    public static final String DBITEM_SEARCH_WORKFLOWS = DBItemSearchWorkflow.class.getSimpleName();
    public static final String TABLE_SEARCH_WORKFLOWS = "SEARCH_WORKFLOWS";
    public static final String TABLE_SEARCH_WORKFLOWS_SEQUENCE = "SEQ_SEARCH_W";

    public static final String DBITEM_SEARCH_WORKFLOWS_DEPLOYMENT_HISTORY = DBItemSearchWorkflow2DeploymentHistory.class.getSimpleName();
    public static final String TABLE_SEARCH_WORKFLOWS_DEPLOYMENT_HISTORY = "SEARCH_WORKFLOWS_DEP_H";

    /** Deployment tables */
    public static final String DBITEM_DEP_HISTORY = DBItemDeploymentHistory.class.getSimpleName();
    public static final String TABLE_DEP_HISTORY = "DEP_HISTORY";
    public static final String TABLE_DEP_HISTORY_SEQUENCE = "SEQ_DEP_HIS";

    public static final String DBITEM_DEP_SUBMISSIONS = DBItemDeploymentSubmission.class.getSimpleName();
    public static final String TABLE_DEP_SUBMISSIONS = "DEP_SUBMISSIONS";
    public static final String TABLE_DEP_SUBMISSIONS_SEQUENCE = "SEQ_DEP_SUB";

    public static final String DBITEM_DEP_SIGNATURES = DBItemDepSignatures.class.getSimpleName();
    public static final String TABLE_DEP_SIGNATURES = "DEP_SIGNATURES";
    public static final String TABLE_DEP_SIGNATURES_SEQUENCE = "SEQ_DEP_SIG";

    public static final String DBITEM_DEP_VERSIONS = DBItemDepVersions.class.getSimpleName();
    public static final String TABLE_DEP_VERSIONS = "DEP_VERSIONS";
    public static final String TABLE_DEP_VERSIONS_SEQUENCE = "SEQ_DEP_VER";

    public static final String DBITEM_DEP_COMMIT_IDS = DBItemDepCommitIds.class.getSimpleName();
    public static final String TABLE_DEP_COMMIT_IDS = "DEP_COMMIT_IDS";
    public static final String TABLE_DEP_COMMIT_IDS_SEQUENCE = "SEQ_DEP_COM";

    public static final String DBITEM_DEP_KEYS = DBItemDepKeys.class.getSimpleName();
    public static final String TABLE_DEP_KEYS = "DEP_KEYS";
    public static final String TABLE_DEP_KEYS_SEQUENCE = "SEQ_DEP_K";

    public static final String DBITEM_DEP_CONFIGURATIONS = DBItemDepConfiguration.class.getSimpleName();
    public static final String TABLE_DEP_CONFIGURATIONS = "DEP_CONFIGURATIONS";

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
        cl.add(DBItemHistoryOrderState.class);
        cl.add(DBItemHistoryOrderStep.class);
        cl.add(DBItemHistoryLog.class);
        cl.add(DBItemHistoryTempLog.class);
        cl.add(DBItemHistoryController.class);
        cl.add(DBItemHistoryAgent.class);

        cl.add(DBItemInventoryAgentInstance.class);
        return cl;
    }

    public static SOSClassList getOrderInitatorClassMapping() {
        SOSClassList cl = new SOSClassList();
        cl.add(DBItemInventoryJSInstance.class);
        cl.add(DBItemDailyPlanSubmissionHistory.class);
        cl.add(DBItemDailyPlanOrders.class);
        cl.add(DBItemDailyPlanVariables.class);
        return cl;
    }

    public static SOSClassList getJocClassMapping() {
        SOSClassList cl = new SOSClassList();

        cl.add(DBItemInventoryOperatingSystem.class);
        cl.add(DBItemInventoryJSInstance.class);
        cl.add(DBItemInventoryAgentInstance.class);
        cl.add(DBItemInventoryAgentName.class);
        cl.add(DBItemInventoryConfiguration.class);
        cl.add(DBItemInventoryReleasedConfiguration.class);
        cl.add(DBItemSearchWorkflow.class);
        cl.add(DBItemSearchWorkflow2DeploymentHistory.class);
        cl.add(com.sos.joc.db.inventory.deprecated.documentation.DBItemDocumentation.class);
        cl.add(com.sos.joc.db.inventory.deprecated.documentation.DBItemDocumentationImage.class);
        cl.add(com.sos.joc.db.inventory.deprecated.documentation.DBItemDocumentationUsage.class);

        cl.add(DBItemJocConfiguration.class);
        cl.add(DBItemJocInstance.class);
        cl.add(DBItemJocCluster.class);
        cl.add(DBItemJocAuditLog.class);
        cl.add(DBItemJocLock.class);

        cl.add(DBItemDeploymentHistory.class);
        cl.add(DBItemDepKeys.class);
        cl.add(DBItemDepSignatures.class);
        cl.add(DBItemDepVersions.class);
        cl.add(DBItemDepConfiguration.class);
        cl.add(DBItemDeploymentSubmission.class);
        cl.add(DBItemDepCommitIds.class);

        cl.merge(getHistoryClassMapping().getClasses());
        cl.merge(getOrderInitatorClassMapping().getClasses());

        cl.add(DBItemXmlEditorConfiguration.class);
        return cl;
    }

}