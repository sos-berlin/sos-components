package com.sos.joc.db;

import java.io.Serializable;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSClassList;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamAccount2Roles;
import com.sos.joc.db.authentication.DBItemIamBlockedAccount;
import com.sos.joc.db.authentication.DBItemIamFido2Devices;
import com.sos.joc.db.authentication.DBItemIamFido2Registration;
import com.sos.joc.db.authentication.DBItemIamFido2Requests;
import com.sos.joc.db.authentication.DBItemIamHistory;
import com.sos.joc.db.authentication.DBItemIamHistoryDetails;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.authentication.DBItemIamPermission;
import com.sos.joc.db.authentication.DBItemIamRole;
import com.sos.joc.db.dailyplan.DBItemDailyPlanHistory;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanProjection;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;
import com.sos.joc.db.dailyplan.DBItemDailyPlanVariable;
import com.sos.joc.db.deployment.DBItemDepCommitIds;
import com.sos.joc.db.deployment.DBItemDepConfiguration;
import com.sos.joc.db.deployment.DBItemDepKeys;
import com.sos.joc.db.deployment.DBItemDepNamePaths;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDepVersions;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.deployment.DBItemDeploymentSubmission;
import com.sos.joc.db.documentation.DBItemDocumentation;
import com.sos.joc.db.documentation.DBItemDocumentationImage;
import com.sos.joc.db.encipherment.DBItemEncAgentCertificate;
import com.sos.joc.db.encipherment.DBItemEncCertificate;
import com.sos.joc.db.history.DBItemHistoryAgent;
import com.sos.joc.db.history.DBItemHistoryController;
import com.sos.joc.db.history.DBItemHistoryLog;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.DBItemHistoryOrderState;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryAgentName;
import com.sos.joc.db.inventory.DBItemInventoryCertificate;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryConfigurationTrash;
import com.sos.joc.db.inventory.DBItemInventoryFavorite;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.DBItemInventoryOperatingSystem;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryReleasedSchedule2Calendar;
import com.sos.joc.db.inventory.DBItemInventoryReleasedSchedule2Workflow;
import com.sos.joc.db.inventory.DBItemInventorySchedule2Calendar;
import com.sos.joc.db.inventory.DBItemInventorySchedule2Workflow;
import com.sos.joc.db.inventory.DBItemInventorySubAgentCluster;
import com.sos.joc.db.inventory.DBItemInventorySubAgentClusterMember;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryTag;
import com.sos.joc.db.inventory.DBItemInventoryTagging;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.joc.DBItemJocAuditLogDetails;
import com.sos.joc.db.joc.DBItemJocCluster;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.db.joc.DBItemJocLock;
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.db.monitoring.DBItemNotification;
import com.sos.joc.db.monitoring.DBItemNotificationAcknowledgement;
import com.sos.joc.db.monitoring.DBItemNotificationMonitor;
import com.sos.joc.db.monitoring.DBItemNotificationWorkflow;
import com.sos.joc.db.monitoring.DBItemSystemNotification;
import com.sos.joc.db.reporting.DBItemReport;
import com.sos.joc.db.reporting.DBItemReportRun;
import com.sos.joc.db.reporting.DBItemReportTemplate;
import com.sos.joc.db.search.DBItemSearchWorkflow;
import com.sos.joc.db.search.DBItemSearchWorkflow2DeploymentHistory;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.db.yade.DBItemYadeFile;
import com.sos.joc.db.yade.DBItemYadeProtocol;
import com.sos.joc.db.yade.DBItemYadeTransfer;

public class DBLayer implements Serializable {

    private static final long serialVersionUID = 1L;

    /** JOC Tables */
    public static final String DBITEM_JOC_VARIABLES = DBItemJocVariable.class.getSimpleName();
    public static final String TABLE_JOC_VARIABLES = "JOC_VARIABLES";

    public static final String DBITEM_JOC_INSTANCES = DBItemJocInstance.class.getSimpleName();
    public static final String TABLE_JOC_INSTANCES = "JOC_INSTANCES";
    public static final String TABLE_JOC_INSTANCES_SEQUENCE = "SEQ_JOC_I";

    public static final String DBITEM_JOC_CLUSTER = DBItemJocCluster.class.getSimpleName();
    public static final String TABLE_JOC_CLUSTER = "JOC_CLUSTER";

    public static final String DBITEM_JOC_AUDIT_LOG = DBItemJocAuditLog.class.getSimpleName();
    public static final String TABLE_JOC_AUDIT_LOG = "JOC_AUDIT_LOG";
    public static final String TABLE_JOC_AUDIT_LOG_SEQUENCE = "SEQ_JOC_AL";

    public static final String DBITEM_JOC_AUDIT_LOG_DETAILS = DBItemJocAuditLogDetails.class.getSimpleName();
    public static final String TABLE_JOC_AUDIT_LOG_DETAILS = "JOC_AUDIT_LOG_DETAILS";
    public static final String TABLE_JOC_AUDIT_LOG_DETAILS_SEQUENCE = "SEQ_JOC_ALD";

    public static final String DBITEM_JOC_CONFIGURATIONS = DBItemJocConfiguration.class.getSimpleName();
    public static final String TABLE_JOC_CONFIGURATIONS = "JOC_CONFIGURATIONS";
    public static final String TABLE_JOC_CONFIGURATIONS_SEQUENCE = "SEQ_JOC_C";

    public static final String DBITEM_JOC_LOCKS = DBItemJocLock.class.getSimpleName();
    public static final String TABLE_JOC_LOCKS = "JOC_LOCKS";
    public static final String TABLE_JOC_LOCKS_SEQUENCE = "SEQ_JOC_L";

    /** HISTORY Tables */
    public static final String DBITEM_HISTORY_CONTROLLERS = DBItemHistoryController.class.getSimpleName();
    public static final String TABLE_HISTORY_CONTROLLERS = "HISTORY_CONTROLLERS";

    public static final String DBITEM_HISTORY_AGENTS = DBItemHistoryAgent.class.getSimpleName();
    public static final String TABLE_HISTORY_AGENTS = "HISTORY_AGENTS";

    public static final String DBITEM_HISTORY_ORDERS = DBItemHistoryOrder.class.getSimpleName();
    public static final String TABLE_HISTORY_ORDERS = "HISTORY_ORDERS";
    public static final String TABLE_HISTORY_ORDERS_SEQUENCE = "SEQ_HISTORY_O";

    public static final String DBITEM_HISTORY_ORDER_STATES = DBItemHistoryOrderState.class.getSimpleName();
    public static final String TABLE_HISTORY_ORDER_STATES = "HISTORY_ORDER_STATES";
    public static final String TABLE_HISTORY_ORDER_STATES_SEQUENCE = "SEQ_HISTORY_OSTATES";

    public static final String DBITEM_HISTORY_ORDER_STEPS = DBItemHistoryOrderStep.class.getSimpleName();
    public static final String TABLE_HISTORY_ORDER_STEPS = "HISTORY_ORDER_STEPS";
    public static final String TABLE_HISTORY_ORDER_STEPS_SEQUENCE = "SEQ_HISTORY_OSTEPS";

    public static final String DBITEM_HISTORY_LOGS = DBItemHistoryLog.class.getSimpleName();
    public static final String TABLE_HISTORY_LOGS = "HISTORY_LOGS";
    public static final String TABLE_HISTORY_LOGS_SEQUENCE = "SEQ_HISTORY_L";

//    public static final String DBITEM_HISTORY_ORDER_TAGS = DBItemHistoryOrderTag.class.getSimpleName();
//    public static final String TABLE_HISTORY_ORDER_TAGS = "HISTORY_ORDER_TAGS";
//    public static final String TABLE_HISTORY_ORDER_TAGS_SEQUENCE = "SEQ_HISTORY_OTAGS";

    /** MONITORING Tables */
    public static final String DBITEM_MON_ORDERS = DBItemMonitoringOrder.class.getSimpleName();
    public static final String TABLE_MON_ORDERS = "MON_ORDERS";

    public static final String DBITEM_MON_ORDER_STEPS = DBItemMonitoringOrderStep.class.getSimpleName();
    public static final String TABLE_MON_ORDER_STEPS = "MON_ORDER_STEPS";

    public static final String DBITEM_MON_NOTIFICATIONS = DBItemNotification.class.getSimpleName();
    public static final String TABLE_MON_NOTIFICATIONS = "MON_NOTIFICATIONS";
    public static final String TABLE_MON_NOTIFICATIONS_SEQUENCE = "SEQ_MON_N";

    public static final String DBITEM_MON_SYSNOTIFICATIONS = DBItemSystemNotification.class.getSimpleName();
    public static final String TABLE_MON_SYSNOTIFICATIONS = "MON_SYSNOTIFICATIONS";
    public static final String TABLE_MON_SYSNOTIFICATIONS_SEQUENCE = "SEQ_MON_SN";

    public static final String DBITEM_MON_NOT_MONITORS = DBItemNotificationMonitor.class.getSimpleName();
    public static final String TABLE_MON_NOT_MONITORS = "MON_NOT_MONITORS";
    public static final String TABLE_MON_NOT_MONITORS_SEQUENCE = "SEQ_MON_NM";

    public static final String DBITEM_MON_NOT_WORKFLOWS = DBItemNotificationWorkflow.class.getSimpleName();
    public static final String TABLE_MON_NOT_WORKFLOWS = "MON_NOT_WORKFLOWS";

    public static final String DBITEM_MON_NOT_ACKNOWLEDGEMENTS = DBItemNotificationAcknowledgement.class.getSimpleName();
    public static final String TABLE_MON_NOT_ACKNOWLEDGEMENTS = "MON_NOT_ACKNOWLEDGEMENTS";

    /** Daily plan tables */
    public static final String DBITEM_DPL_ORDERS = DBItemDailyPlanOrder.class.getSimpleName();
    public static final String TABLE_DPL_ORDERS = "DPL_ORDERS";
    public static final String TABLE_DPL_ORDERS_SEQUENCE = "SEQ_DPL_ORDERS";

    public static final String DBITEM_DPL_SUBMISSIONS = DBItemDailyPlanSubmission.class.getSimpleName();
    public static final String TABLE_DPL_SUBMISSIONS = "DPL_SUBMISSIONS";
    public static final String TABLE_DPL_SUBMISSIONS_SEQUENCE = "SEQ_DPL_SUBMISSIONS";

    public static final String DBITEM_DPL_HISTORY = DBItemDailyPlanHistory.class.getSimpleName();
    public static final String TABLE_DPL_HISTORY = "DPL_HISTORY";
    public static final String TABLE_DPL_HISTORY_SEQUENCE = "SEQ_DPL_HISTORY";

    public static final String DBITEM_DPL_ORDER_VARIABLES = DBItemDailyPlanVariable.class.getSimpleName();
    public static final String TABLE_DPL_ORDER_VARIABLES = "DPL_ORDER_VARIABLES";
    public static final String TABLE_DPL_ORDER_VARIABLES_SEQUENCE = "SEQ_DPL_ORDER_VARS";

    public static final String DBITEM_DPL_PROJECTIONS = DBItemDailyPlanProjection.class.getSimpleName();
    public static final String TABLE_DPL_PROJECTIONS = "DPL_PROJECTIONS";

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

    public static final String DBITEM_INV_SUBAGENT_INSTANCES = DBItemInventorySubAgentInstance.class.getSimpleName();
    public static final String TABLE_INV_SUBAGENT_INSTANCES = "INV_SUBAGENT_INSTANCES";
    public static final String TABLE_INV_SUBAGENT_INSTANCES_SEQUENCE = "SEQ_INV_SAI";

    public static final String DBITEM_INV_SUBAGENT_CLUSTERS = DBItemInventorySubAgentCluster.class.getSimpleName();
    public static final String TABLE_INV_SUBAGENT_CLUSTERS = "INV_SUBAGENT_CLUSTERS";
    public static final String TABLE_INV_SUBAGENT_CLUSTERS_SEQUENCE = "SEQ_INV_SAC";

    public static final String DBITEM_INV_SUBAGENT_CLUSTER_MEMBERS = DBItemInventorySubAgentClusterMember.class.getSimpleName();
    public static final String TABLE_INV_SUBAGENT_CLUSTER_MEMBERS = "INV_SUBAGENT_CLUSTER_MEMBERS";
    public static final String TABLE_INV_SUBAGENT_CLUSTER_MEMBERS_SEQUENCE = "SEQ_INV_SACM";

    public static final String DBITEM_INV_CONFIGURATIONS = DBItemInventoryConfiguration.class.getSimpleName();
    public static final String TABLE_INV_CONFIGURATIONS = "INV_CONFIGURATIONS";
    public static final String TABLE_INV_CONFIGURATIONS_SEQUENCE = "SEQ_INV_C";

    public static final String DBITEM_INV_CONFIGURATION_TRASH = DBItemInventoryConfigurationTrash.class.getSimpleName();
    public static final String TABLE_INV_CONFIGURATION_TRASH = "INV_CONFIGURATION_TRASH";
    public static final String TABLE_INV_CONFIGURATION_TRASH_SEQUENCE = "SEQ_INV_CT";

    public static final String DBITEM_INV_RELEASED_CONFIGURATIONS = DBItemInventoryReleasedConfiguration.class.getSimpleName();
    public static final String TABLE_INV_RELEASED_CONFIGURATIONS = "INV_RELEASED_CONFIGURATIONS";
    public static final String TABLE_INV_RELEASED_CONFIGURATIONS_SEQUENCE = "SEQ_INV_RC";

    public static final String DBITEM_INV_CERTS = DBItemInventoryCertificate.class.getSimpleName();
    public static final String TABLE_INV_CERTS = "INV_CERTS";
    public static final String TABLE_INV_CERTS_SEQUENCE = "SEQ_INV_CRTS";

    public static final String DBITEM_INV_DOCUMENTATIONS = DBItemDocumentation.class.getSimpleName();
    public static final String TABLE_INV_DOCUMENTATIONS = "INV_DOCUMENTATIONS";
    public static final String TABLE_INV_DOCUMENTATIONS_SEQUENCE = "SEQ_INV_D";

    public static final String DBITEM_INV_DOCUMENTATION_IMAGES = DBItemDocumentationImage.class.getSimpleName();
    public static final String TABLE_INV_DOCUMENTATION_IMAGES = "INV_DOCUMENTATION_IMAGES";
    public static final String TABLE_INV_DOCUMENTATION_IMAGES_SEQUENCE = "SEQ_INV_DI";

    public static final String DBITEM_INV_FAVORITES = DBItemInventoryFavorite.class.getSimpleName();
    public static final String TABLE_INV_FAVORITES = "INV_FAVORITES";
    public static final String TABLE_INV_FAVORITES_SEQUENCE = "SEQ_INV_F";
    
    public static final String DBITEM_INV_TAGS = DBItemInventoryTag.class.getSimpleName();
    public static final String TABLE_INV_TAGS = "INV_TAGS";
    public static final String TABLE_INV_TAGS_SEQUENCE = "SEQ_INV_T";

    public static final String DBITEM_INV_TAGGINGS = DBItemInventoryTagging.class.getSimpleName();
    public static final String TABLE_INV_TAGGINGS = "INV_TAGGINGS";
    public static final String TABLE_INV_TAGGINGS_SEQUENCE = "SEQ_INV_TG";

    public static final String DBITEM_INV_SCHEDULE2WORKFLOWS = DBItemInventorySchedule2Workflow.class.getSimpleName();
    public static final String VIEW_INV_SCHEDULE2WORKFLOWS = "INV_SCHEDULE2WORKFLOWS";

    public static final String DBITEM_INV_RELEASED_SCHEDULE2WORKFLOWS = DBItemInventoryReleasedSchedule2Workflow.class.getSimpleName();
    public static final String VIEW_INV_RELEASED_SCHEDULE2WORKFLOWS = "INV_REL_SCHEDULE2WORKFLOWS";

    public static final String DBITEM_INV_SCHEDULE2CALENDARS = DBItemInventorySchedule2Calendar.class.getSimpleName();
    public static final String VIEW_INV_SCHEDULE2CALENDARS = "INV_SCHEDULE2CALENDARS";

    public static final String DBITEM_INV_RELEASED_SCHEDULE2CALENDARS = DBItemInventoryReleasedSchedule2Calendar.class.getSimpleName();
    public static final String VIEW_INV_RELEASED_SCHEDULE2CALENDARS = "INV_REL_SCHEDULE2CALENDARS";

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

    public static final String DBITEM_DEP_NAMEPATHS = DBItemDepNamePaths.class.getSimpleName();
    public static final String TABLE_DEP_NAMEPATHS = "DEP_NAMEPATHS";

    /** XMLEDITOR Tables */
    public static final String DBITEM_XML_EDITOR_CONFIGURATIONS = DBItemXmlEditorConfiguration.class.getSimpleName();
    public static final String TABLE_XML_EDITOR_CONFIGURATIONS = "XMLEDITOR_CONFIGURATIONS";
    public static final String TABLE_XML_EDITOR_CONFIGURATIONS_SEQUENCE = "SEQ_XEC";

    /** YADE Tables */
    public static final String DBITEM_YADE_TRANSFERS = DBItemYadeTransfer.class.getSimpleName();
    public static final String TABLE_YADE_TRANSFERS = "YADE_TRANSFERS";
    public static final String TABLE_YADE_TRANSFERS_SEQUENCE = "SEQ_YADE_TRA";

    public static final String DBITEM_YADE_PROTOCOLS = DBItemYadeProtocol.class.getSimpleName();
    public static final String TABLE_YADE_PROTOCOLS = "YADE_PROTOCOLS";
    public static final String TABLE_YADE_PROTOCOLS_SEQUENCE = "SEQ_YADE_PRO";

    public static final String DBITEM_YADE_FILES = DBItemYadeFile.class.getSimpleName();
    public static final String TABLE_YADE_FILES = "YADE_FILES";
    public static final String TABLE_YADE_FILES_SEQUENCE = "SEQ_YADE_FIL";

    public static final String DBITEM_IAM_ACCOUNTS = DBItemIamAccount.class.getSimpleName();
    public static final String TABLE_IAM_ACCOUNTS = "IAM_ACCOUNTS";
    public static final String TABLE_IAM_ACCOUNTS_SEQUENCE = "SEQ_IAM_ACCOUNTS";

    public static final String DBITEM_IAM_FIDO2_DEVICES = DBItemIamFido2Devices.class.getSimpleName();
    public static final String TABLE_IAM_FIDO2_DEVICES = "IAM_FIDO2_DEVICES";
    public static final String TABLE_IAM_FIDO2_DEVICES_SEQUENCE = "SEQ_IAM_FIDO2_DEVICES";

    public static final String DBITEM_IAM_FIDO2_REGISTRATIONS = DBItemIamFido2Registration.class.getSimpleName();
    public static final String TABLE_IAM_FIDO2_REGISTRATIONS = "IAM_FIDO2_REGISTRATIONS";
    public static final String TABLE_IAM_FIDO2_REGISTRATIONS_SEQUENCE = "SEQ_IAM_FIDO2_REGISTRATIONS";

    public static final String DBITEM_IAM_FIDO2_REQUESTS = DBItemIamFido2Requests.class.getSimpleName();
    public static final String TABLE_IAM_FIDO2_REQUESTS = "IAM_FIDO2_REQUESTS";
    public static final String TABLE_IAM_FIDO2_REQUESTS_SEQUENCE = "SEQ_IAM_FIDO2__REQUESTS";

    public static final String DBITEM_IAM_HISTORY = DBItemIamHistory.class.getSimpleName();
    public static final String TABLE_IAM_HISTORY = "IAM_HISTORY";
    public static final String TABLE_IAM_HISTORY_SEQUENCE = "SEQ_IAM_HISTORY";

    public static final String TABLE_IAM_BLOCKLIST = "IAM_BLOCKLIST";
    public static final String TABLE_IAM_BLOCKLIST_SEQUENCE = "SEQ_IAM_BLOCKLIST";

    public static final String DBITEM_IAM_HISTORY_DETAILS = DBItemIamHistoryDetails.class.getSimpleName();
    public static final String TABLE_IAM_HISTORY_DETAILS = "IAM_HISTORY_DETAILS";
    public static final String TABLE_IAM_HISTORY_DETAILS_SEQUENCE = "SEQ_IAM_HISTORY_DETAILS";

    public static final String TABLE_IAM_ACCOUNT2ROLES = "IAM_ACCOUNT2ROLES";
    public static final String TABLE_IAM_ACCOUNT2ROLES_SEQUENCE = "SEQ_IAM_ACCOUNT2ROLES";

    public static final String TABLE_IAM_IDENTITY_SERVICES = "IAM_IDENTITY_SERVICES";
    public static final String TABLE_IAM_IDENTITY_SERVICES_SEQUENCE = "SEQ_IAM_IDENTITY_SERVICES";

    public static final String TABLE_IAM_PERMISSIONS = "IAM_PERMISSIONS";
    public static final String TABLE_IAM_PERMISSIONS_SEQUENCE = "SEQ_IAM_PERMISSIONS";

    public static final String TABLE_IAM_ROLES = "IAM_ROLES";
    public static final String TABLE_IAM_ROLES_SEQUENCE = "SEQ_IAM_ROLES";
    
    /** Reporting tables */
    public static final String DBITEM_REPORTS = DBItemReport.class.getSimpleName();
    public static final String TABLE_REPORTS = "REPORTS";
    public static final String TABLE_REPORTS_SEQUENCE = "SEQ_REP_REP";
    
    public static final String DBITEM_REPORT_RUN = DBItemReportRun.class.getSimpleName();
    public static final String TABLE_REPORT_RUNS = "REPORT_RUNS";
    public static final String TABLE_REPORT_RUNS_SEQUENCE = "SEQ_REP_RUN";
    
    public static final String DBITEM_REPORT_TEMPLATE = DBItemReportTemplate.class.getSimpleName();
    public static final String TABLE_REPORT_TEMPLATES = "REPORT_TEMPLATES";
    
    public static final String DBITEM_ENC_CERTIFICATE = DBItemEncCertificate.class.getSimpleName();
    public static final String TABLE_ENC_CERTIFICATE = "ENC_CERTIFICATES";
    
    public static final String DBITEM_ENC_AGENT_CERTIFICATES = DBItemEncAgentCertificate.class.getSimpleName();
    public static final String TABLE_ENC_AGENT_CERTIFICATES = "ENC_AGENT_CERTIFICATES";
    
    // public static final String DEFAULT_FOLDER = "/";
    // public static final Long DEFAULT_ID = 0L;
    public static final String DEFAULT_KEY = ".";

    private SOSHibernateSession session;

    public DBLayer() {
        this(null);
    }

    public DBLayer(SOSHibernateSession session) {
        this.session = session;
    }

    public SOSHibernateSession getSession() {
        return session;
    }

    public void close() {
        if (session != null) {
            session.close();
            session = null;
        }
    }

    public static void close(DBLayer dbLayer) {
        if (dbLayer != null) {
            dbLayer.close();
        }
    }

    public void setSession(SOSHibernateSession val) {
        close();
        session = val;
    }

    public void beginTransaction() throws SOSHibernateException {
        if (session != null) {
            session.beginTransaction();
        }
    }

    public void commit() throws SOSHibernateException {
        if (session != null) {
            session.commit();
        }
    }

    public void rollback() {
        if (session != null) {
            try {
                session.rollback();
            } catch (Throwable e) {
            }
        }
    }

    public static SOSClassList getYadeClassMapping() {
        SOSClassList cl = new SOSClassList();
        cl.add(DBItemYadeTransfer.class);
        cl.add(DBItemYadeProtocol.class);
        cl.add(DBItemYadeFile.class);
        return cl;
    }

    public static SOSClassList getJocClusterClassMapping() {
        SOSClassList cl = new SOSClassList();
        cl.add(DBItemInventoryOperatingSystem.class);
        cl.add(DBItemJocInstance.class);
        cl.add(DBItemJocCluster.class);
        cl.add(DBItemInventoryJSInstance.class);
        cl.add(DBItemJocConfiguration.class);
        return cl;
    }

    public static SOSClassList getHistoryClassMapping() {
        SOSClassList cl = new SOSClassList();
        cl.add(DBItemJocVariable.class);
        cl.add(DBItemHistoryOrder.class);
        cl.add(DBItemHistoryOrderState.class);
        cl.add(DBItemHistoryOrderStep.class);
        cl.add(DBItemHistoryLog.class);
        cl.add(DBItemHistoryController.class);
        cl.add(DBItemHistoryAgent.class);

        cl.add(DBItemInventoryAgentInstance.class);
        cl.add(DBItemDeploymentHistory.class);
        cl.merge(getYadeClassMapping().getClasses());
        return cl;
    }

    public static SOSClassList getMonitoringClassMapping() {
        SOSClassList cl = new SOSClassList();
        cl.add(DBItemMonitoringOrder.class);
        cl.add(DBItemMonitoringOrderStep.class);
        cl.add(DBItemNotification.class);
        cl.add(DBItemSystemNotification.class);
        cl.add(DBItemNotificationMonitor.class);
        cl.add(DBItemNotificationWorkflow.class);
        cl.add(DBItemNotificationAcknowledgement.class);

        cl.add(DBItemHistoryOrder.class);
        cl.add(DBItemHistoryOrderStep.class);
        cl.add(DBItemJocVariable.class);
        cl.add(DBItemXmlEditorConfiguration.class);
        cl.add(DBItemDepConfiguration.class);
        return cl;
    }

    public static SOSClassList getDailyPlanClassMapping() {
        SOSClassList cl = new SOSClassList();
        cl.add(DBItemInventoryJSInstance.class);
        cl.add(DBItemDailyPlanSubmission.class);
        cl.add(DBItemDailyPlanOrder.class);
        cl.add(DBItemDailyPlanVariable.class);
        cl.add(DBItemDailyPlanHistory.class);
        cl.add(DBItemDailyPlanProjection.class);
        return cl;
    }

    public static SOSClassList getJocClassMapping() {
        SOSClassList cl = new SOSClassList();

        cl.add(DBItemInventoryOperatingSystem.class);
        cl.add(DBItemInventoryJSInstance.class);
        cl.add(DBItemInventoryAgentInstance.class);
        cl.add(DBItemInventoryAgentName.class);
        cl.add(DBItemInventorySubAgentInstance.class);
        cl.add(DBItemInventorySubAgentCluster.class);
        cl.add(DBItemInventorySubAgentClusterMember.class);
        cl.add(DBItemInventoryConfiguration.class);
        cl.add(DBItemInventoryConfigurationTrash.class);
        cl.add(DBItemInventoryReleasedConfiguration.class);
        cl.add(DBItemInventoryCertificate.class);
        cl.add(DBItemInventorySchedule2Workflow.class);
        cl.add(DBItemInventoryReleasedSchedule2Workflow.class);
        cl.add(DBItemInventorySchedule2Calendar.class);
        cl.add(DBItemInventoryReleasedSchedule2Calendar.class);
        cl.add(DBItemInventoryFavorite.class);
        cl.add(DBItemInventoryTag.class);
        cl.add(DBItemInventoryTagging.class);
        cl.add(DBItemSearchWorkflow.class);
        cl.add(DBItemSearchWorkflow2DeploymentHistory.class);
        cl.add(DBItemDocumentation.class);
        cl.add(DBItemDocumentationImage.class);

        cl.add(DBItemJocConfiguration.class);
        cl.add(DBItemJocInstance.class);
        cl.add(DBItemJocCluster.class);
        cl.add(DBItemJocAuditLog.class);
        cl.add(DBItemJocAuditLogDetails.class);
        cl.add(DBItemJocLock.class);

        cl.add(DBItemDeploymentHistory.class);
        cl.add(DBItemDepKeys.class);
        cl.add(DBItemDepSignatures.class);
        cl.add(DBItemDepVersions.class);
        cl.add(DBItemDepConfiguration.class);
        cl.add(DBItemDeploymentSubmission.class);
        cl.add(DBItemDepCommitIds.class);
        cl.add(DBItemDepNamePaths.class);

        cl.add(DBItemIamBlockedAccount.class);
        cl.add(DBItemIamHistory.class);
        cl.add(DBItemIamHistoryDetails.class);
        cl.add(DBItemIamAccount.class);
        cl.add(DBItemIamIdentityService.class);
        cl.add(DBItemIamAccount2Roles.class);
        cl.add(DBItemIamPermission.class);
        cl.add(DBItemIamRole.class);
        cl.add(DBItemIamFido2Registration.class);
        cl.add(DBItemIamFido2Requests.class);
        cl.add(DBItemIamFido2Devices.class);
        
        cl.add(DBItemReport.class);
        cl.add(DBItemReportRun.class);
        cl.add(DBItemReportTemplate.class);
        
        cl.add(DBItemEncCertificate.class);
        cl.add(DBItemEncAgentCertificate.class);

        cl.merge(getHistoryClassMapping().getClasses());
        cl.merge(getDailyPlanClassMapping().getClasses());
        cl.merge(getMonitoringClassMapping().getClasses());

        cl.add(DBItemXmlEditorConfiguration.class);
        return cl;
    }

}