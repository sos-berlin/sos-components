package com.sos.schema;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.NonValidationKeyword;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.sos.schema.exception.SOSJsonSchemaException;

public class JsonValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonValidator.class);
    private static final ObjectMapper MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true);
    private static final SpecVersion.VersionFlag JSONDRAFT = SpecVersion.VersionFlag.V4;
    private static final List<NonValidationKeyword> NON_VALIDATION_KEYS = Arrays.asList(new NonValidationKeyword("javaType"),
            new NonValidationKeyword("javaInterfaces"), new NonValidationKeyword("javaEnumNames"), new NonValidationKeyword("extends"),
            new NonValidationKeyword("additionalProperties"), new NonValidationKeyword("propertyOrder"), new NonValidationKeyword("alias"), 
            new NonValidationKeyword("schemaVersion"));
    private static final List<NonValidationKeyword> NON_VALIDATION_KEYS_STRICT = Arrays.asList(new NonValidationKeyword("javaType"),
            new NonValidationKeyword("javaInterfaces"), new NonValidationKeyword("javaEnumNames"), new NonValidationKeyword("extends"),
            new NonValidationKeyword("propertyOrder"), new NonValidationKeyword("alias"), new NonValidationKeyword("schemaVersion"));
    private static final JsonSchemaFactory FACTORY_V4 = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(JSONDRAFT)).addMetaSchema(
            JsonMetaSchema.builder(JsonMetaSchema.getV4().getUri(), JsonMetaSchema.getV4()).addKeywords(NON_VALIDATION_KEYS).build()).build();
    private static final JsonSchemaFactory FACTORY_V4_STRICT = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(JSONDRAFT)).addMetaSchema(
            JsonMetaSchema.builder(JsonMetaSchema.getV4().getUri(), JsonMetaSchema.getV4()).addKeywords(NON_VALIDATION_KEYS_STRICT).build()).build();
    private static final String BASE_URI = "classpath:/raml/api/schemas/";

    private static final Map<String, String> CLASS_URI_MAPPING = Collections.unmodifiableMap(new HashMap<String, String>() {

        private static final long serialVersionUID = 1L;

        {
            put("AuditLogFilter", "audit/auditLogFilter-schema.json");
            put("AuditLogDetailFilter", "audit/auditLogDetailFilter-schema.json");

            put("ConfigurationsFilter", "configuration/configurationsFilter-schema.json");
            put("ConfigurationsDeleteFilter", "configuration/configurationsDeleteFilter-schema.json");
            put("Configuration", "configuration/configuration-schema.json");

            put("CalendarDatesFilter", "calendar/calendarDatesFilter-schema.json");
            put("CalendarsFilter", "calendar/calendarsFilter-schema.json");

            put("ClusterRestart", "cluster/restart-schema.json");
            put("ClusterSwitchMember", "cluster/switch-schema.json");

            put("DocumentationShowFilter", "docu/documentationShow-schema.json");
            put("DocumentationFilter", "docu/documentationFilter-schema.json");
            put("DocumentationsFilter", "docu/documentationsFilter-schema.json");
            put("DocumentationsDeleteFilter", "docu/documentationsDeleteFilter-schema.json");

            put("CockpitFilter", "controller/jocFilter-schema.json");
            put("Controller", "event/controllerFilter-schema.json");

            put("com.sos.joc.model.controller.ControllerId", "controller/controllerId-optional-schema.json");
            put("com.sos.joc.model.controller.ControllerIdReq", "controller/controllerId-required-schema.json");
            put("UrlParameter", "controller/urlParam-schema.json");
            put("TestConnect", "controller/testParam-schema.json");
            put("RegisterParameters", "controller/registerParam-schema.json");

            put("AgentCommand", "agent/agentCommand-schema.json");
            put("SubAgentCommand", "agent/subagentCommand-schema.json");
            put("SubAgentsCommand", "agent/subagentsCommand-schema.json");
            put("StoreAgents", "agent/storeParam-schema.json");
            put("DeployAgents", "agent/deployAgents-schema.json");
            put("StoreSubAgents", "agent/storeSubagentsParam-schema.json");
            put("StoreClusterAgents", "agent/storeClusterParam-schema.json");
            put("DeployClusterAgents", "agent/deployClusterAgents-schema.json");
            put("DeploySubAgentClusters", "agent/deploySubagentClusters-schema.json");
            put("ReadAgents", "agent/readAgents-schema.json");
            put("ReadAgentsV", "agent/readAgents_v-schema.json");
            put("AgentReportFilter", "agent/agentReportFilter-schema.json");
            put("StoreSubagentClusters", "agent/storeSubagentClusters-schema.json");
            put("ReadSubagentClusters", "agent/readSubagentClusters-schema.json");
            put("OrderingAgents", "agent/orderingAgent-schema.json");
            put("OrderingSubagents", "agent/orderingSubagent-schema.json");
            put("OrderingSubagentClusters", "agent/orderingSubagentCluster-schema.json");
            put("AgentExportFilter", "agent/transfer/agentExportFilter-schema.json");
            put("AgentImportFilter", "agent/transfer/agentImportFilter-schema.json");

            put("VersionsFilter", "joc/versionsFilter-schema.json");

            put("JobTemplateFilter", "jobTemplate/jobTemplateFilter-schema.json");
            put("JobTemplateStateFilter", "jobTemplate/jobTemplateStateFilter-schema.json");
            put("JobTemplatesFilter", "jobTemplate/jobTemplatesFilter-schema.json");
            put("JobTemplatesPropagateFilter", "jobTemplate/propagate/jobTemplatesPropagateFilter-schema.json");
            put("WorkflowPropagateFilter", "jobTemplate/propagate/workflowPropagateFilter-schema.json");
            put("JobPropagateFilter", "jobTemplate/propagate/jobPropagateFilter-schema.json");

            put("LockFilter", "lock/lockFilter-schema.json");
            put("LocksFilter", "lock/locksFilter-schema.json");

            put("BoardFilter", "board/boardFilter-schema.json");
            put("BoardsFilter", "board/boardsFilter-schema.json");
            put("ModifyNotice", "board/modifyNotice-schema.json");
            put("ModifyNotices", "board/modifyNotices-schema.json");

            put("WorkflowFilter", "workflow/workflowFilter-schema.json");
            put("WorkflowPathFilter", "workflow/workflowPathFilter-schema.json");
            put("WorkflowsFilter", "workflow/workflowsFilter-schema.json");
            put("ModifyWorkflow", "workflow/modifyWorkflow-schema.json");
            put("ModifyWorkflows", "workflow/modifyWorkflows-schema.json");
            put("WorkflowOrderCountFilter", "workflow/workflowOrderCountFilter-schema.json");
            put("ModifyWorkflowLabels", "workflow/modifyWorkflowLabels-schema.json");
            put("ModifyWorkflowPositions", "workflow/modifyWorkflowPositions-schema.json");
            put("WorkflowSearchFilter", "workflow/search/workflowBaseSearchFilter-schema.json");
            put("DeployedObjectQuickSearchFilter", "common/quickSearchFilter-schema.json");


            put("OrderFilter", "order/orderFilter-schema.json");
            put("OrdersFilter", "order/ordersFilter-schema.json");
            put("OrdersFilterV", "order/ordersFilterV-schema.json");
            put("OrderVariablesFilter", "order/orderVariablesFilter-schema.json");
            put("OrderHistoryFilter", "order/orderHistoryFilter-schema.json");
            put("OrderRunningLogFilter", "order/orderRunningLogFilter-schema.json");
            put("AddOrders", "order/addOrders-schema.json");
            put("ModifyOrders", "order/modifyOrders-schema.json");

            put("ScheduleDatesFilter", "orderManagement/scheduleDatesFilter-schema.json");

            put("JobsFilter", "job/jobsFilter-schema.json");
            put("TaskFilter", "job/taskFilter-schema.json");
            put("RunningTaskLogFilter", "job/runningTaskLogFilter-schema.json");
            
            put("ApplyConfiguration", "xmleditor/apply/apply-configuration-schema.json");
            put("SchemaAssignConfiguration", "xmleditor/schema/assign/schema-assign-configuration-schema.json");
            put("DeleteAll", "xmleditor/delete/all/delete-all-schema.json");
            put("DeleteConfiguration", "xmleditor/delete/delete-configuration-schema.json");
            put("RemoveAll", "xmleditor/remove/all/remove-all-schema.json");
            put("RemoveConfiguration", "xmleditor/remove/remove-configuration-schema.json");
            put("ReleaseConfiguration", "xmleditor/release/release-configuration-schema.json");
            put("ReadConfiguration", "xmleditor/read/read-configuration-schema.json");
            put("SchemaReassignConfiguration", "xmleditor/schema/reassign/schema-reassign-configuration-schema.json");
            put("RenameConfiguration", "xmleditor/rename/rename-configuration-schema.json");
            put("StoreConfiguration", "xmleditor/store/store-configuration-schema.json");
            put("ValidateConfiguration", "xmleditor/validate/validate-configuration-schema.json");
            put("Xml2JsonConfiguration", "xmleditor/xml2json/xml2json-configuration-schema.json");

            put("TreeFilter", "tree/treeFilter-schema.json");

            put("com.sos.joc.model.favorite.FavoriteIdentifiers", "favorite/identifiers-schema.json");
            put("com.sos.joc.model.favorite.FavoriteSharedIdentifiers", "favorite/sharedIdentifiers-schema.json");
            put("com.sos.joc.model.favorite.OrderingFavorites", "favorite/orderingFavorite-schema.json");
            put("com.sos.joc.model.favorite.StoreFavorites", "favorite/storeFavorites-schema.json");
            put("com.sos.joc.model.favorite.ReadFavoritesFilter", "favorite/readFavoritesFilter-schema.json");
            put("com.sos.joc.model.favorite.RenameFavorites", "favorite/renameFavorites-schema.json");

            put("com.sos.joc.model.inventory.common.RequestFolder", "inventory/common/request-folder-schema.json");
            put("com.sos.joc.model.inventory.common.RequestFilter", "inventory/common/request-filter-schema.json");
            put("com.sos.joc.model.inventory.common.RequestFilters", "inventory/common/request-filters-schema.json");
            put("com.sos.joc.model.inventory.read.RequestFilter", "inventory/read/request-filter-schema.json");
            put("com.sos.joc.model.inventory.read.RequestWorkflowFilter", "inventory/read/request-workflow-filter-schema.json");
            put("com.sos.joc.model.inventory.delete.RequestFilters", "inventory/delete/request-filters-schema.json");
            put("com.sos.joc.model.inventory.delete.RequestFolder", "inventory/delete/request-folder-schema.json");
            put("com.sos.joc.model.inventory.deploy.DeployableFilter", "inventory/deploy/request-deployable-schema.json");
            put("com.sos.joc.model.inventory.deploy.DeployablesFilter", "inventory/deploy/request-deployables-schema.json");
            put("com.sos.joc.model.inventory.release.ReleaseFilter", "inventory/release/release-schema.json");
            put("com.sos.joc.model.inventory.release.ReleasableFilter", "inventory/release/request-releasable-schema.json");
            put("com.sos.joc.model.inventory.release.ReleasablesFilter", "inventory/release/request-releasables-schema.json");
            put("com.sos.joc.model.inventory.release.ReleasableRecallFilter", "inventory/release/releasableRecallFilter-schema.json");
            put("com.sos.joc.model.inventory.rename.RequestFilter", "inventory/rename/request-filter-schema.json");
            put("com.sos.joc.model.inventory.replace.RequestFilters", "inventory/replace/request-filters-schema.json");
            put("com.sos.joc.model.inventory.replace.RequestFolder", "inventory/replace/request-folder-schema.json");
            put("com.sos.joc.model.inventory.copy.RequestFilter", "inventory/copy/request-filter-schema.json");
            put("com.sos.joc.model.inventory.restore.RequestFilter", "inventory/restore/request-filter-schema.json");
            put("com.sos.joc.model.inventory.references.RequestFilter", "inventory/references/request-filter-schema.json");
            put("com.sos.joc.model.inventory.ConfigurationObject", "inventory/configurationObject-schema.json");
            put("com.sos.joc.model.inventory.path.PathFilter", "inventory/path/pathFilter-schema.json");
            put("com.sos.joc.model.inventory.search.RequestBaseSearchFilter", "inventory/search/request-base-search-filter-schema.json");
            put("com.sos.joc.model.inventory.search.RequestDeployedSearchFilter", "inventory/search/request-deployed-search-filter-schema.json");
            put("com.sos.joc.model.inventory.search.RequestSearchFilter", "inventory/search/request-search-filter-schema.json");
            put("com.sos.joc.model.inventory.search.RequestQuickSearchFilter", "inventory/search/request-quick-search-filter-schema.json");
            put("com.sos.joc.model.inventory.convert.ConvertCronFilter", "inventory/convert/convertCronFilter-schema.json");
            put("com.sos.joc.model.inventory.validate.RequestFolder", "inventory/revalidate/request-folder-schema.json");
            put("JobWizardFilter", "wizard/wizard-job-filter-schema.json");
            
            put("com.sos.joc.model.tag.common.RequestFilters", "tag/common/request-filters-schema.json");
            put("com.sos.joc.model.tag.common.RequestFolder", "tag/common/request-folder-schema.json");
            put("com.sos.joc.model.tag.tagging.RequestFilter", "tag/tagging/request-filter-schema.json");
            put("com.sos.joc.model.tag.rename.RequestFilter", "tag/rename/request-filter-schema.json");
            put("com.sos.joc.model.tag.tagging.RequestModifyFilter", "tag/tagging/request-modify-filter-schema.json");
            put("com.sos.joc.model.inventory.common.RequestTag", "inventory/common/request-tag-schema.json");
            
            put("com.sos.joc.model.descriptor.common.RequestFilter", "descriptor/common/request-filter-schema.json");
            put("com.sos.joc.model.descriptor.common.RequestFolder", "descriptor/common/request-folder-schema.json");
            put("com.sos.joc.model.descriptor.common.ResponseFolder", "descriptor/common/response-folder-schema.json");
            put("com.sos.joc.model.descriptor.common.ResponseFolderItem", "descriptor/common/response-folderItem-schema.json");
            put("com.sos.joc.model.descriptor.common.ResponseRenamed", "descriptor/common/response-newPath-schema.json");
            put("com.sos.joc.model.descriptor.copy.RequestFilter", "descriptor/copy/request-filter-schema.json");
            put("com.sos.joc.model.descriptor.remove.RequestFilters", "descriptor/remove/request-filters-schema.json");
            put("com.sos.joc.model.descriptor.remove.RequestFilter", "descriptor/remove/request-filter-schema.json");
            put("com.sos.joc.model.descriptor.remove.ResponseItem", "descriptor/remove/response-item-schema.json");
            put("com.sos.joc.model.descriptor.rename.RequestFilter", "descriptor/rename/request-filter-schema.json");
            put("com.sos.joc.model.descriptor.restore.RequestFilter", "descriptor/restore/request-filter-schema.json");

            put("com.sos.joc.model.publish.DeleteKeyFilter", "publish/deleteKey-schema.json");
            put("com.sos.joc.model.publish.SetKeyFilter", "publish/setKey-schema.json");
            put("com.sos.joc.model.publish.GenerateKeyFilter", "publish/generateKey-schema.json");
            put("com.sos.joc.model.publish.ExportFilter", "publish/exportFilter-schema.json");
            put("com.sos.joc.model.publish.folder.ExportFolderFilter", "publish/folder/exportFolderFilter-schema.json");
            put("com.sos.joc.model.publish.ImportFilter", "publish/importFilter-schema.json");
            put("com.sos.joc.model.publish.DeployFilter", "publish/deploy-schema.json");
            put("com.sos.joc.model.publish.ImportDeployFilter", "publish/importDeployFilter-schema.json");
            put("com.sos.joc.model.publish.SetVersionFilter", "publish/setVersion-schema.json");
            put("com.sos.joc.model.publish.SetVersionsFilter", "publish/setVersions-schema.json");
            put("com.sos.joc.model.publish.ShowDepHistoryFilter", "publish/showDepHistoryFilter-schema.json");
            put("com.sos.joc.model.publish.ShowDepHistoryFilter", "publish/showDepHistoryFilter-schema.json");
            put("com.sos.joc.model.publish.RedeployFilter", "publish/redeployFilter-schema.json");
            put("com.sos.joc.model.publish.DeleteCaFilter", "publish/deleteCaFilter-schema.json");
            put("com.sos.joc.model.publish.GenerateCaFilter", "publish/generateCaFilter-schema.json");
            put("com.sos.joc.model.publish.ImportRootCaFilter", "publish/importRootCaFilter-schema.json");
            put("com.sos.joc.model.publish.SetRootCaFilter", "publish/setRootCa-schema.json");
            put("com.sos.joc.model.publish.SetRootCaForSigningFilter", "publish/setRootCaForSigning-schema.json");
            put("com.sos.joc.model.publish.CreateCSRFilter", "publish/createCSRFilter-schema.json");
            put("com.sos.joc.model.auth.CreateOnetimeTokenFilter", "auth/createOnetimeTokenFilter-schema.json");
            put("com.sos.joc.model.auth.ShowOnetimeTokenFilter", "auth/showOnetimeTokenFilter-schema.json");
            put("com.sos.joc.model.publish.RevokeFilter", "publish/revoke-schema.json");
            put("com.sos.joc.model.publish.git.GitCredentials", "publish/git/gitCredentials-schema.json");
            put("com.sos.joc.model.publish.git.AddCredentialsFilter", "publish/git/addCredentialsFilter-schema.json");
            put("com.sos.joc.model.publish.git.RemoveCredentials", "publish/git/removeCredentials-schema.json");
            put("com.sos.joc.model.publish.git.RemoveCredentialsFilter", "publish/git/removeCredentialsFilter-schema.json");
            put("com.sos.joc.model.publish.git.commands.CheckoutFilter", "publish/git/commands/checkoutFilter-schema.json");
            put("com.sos.joc.model.publish.git.commands.CloneFilter", "publish/git/commands/cloneFilter-schema.json");
            put("com.sos.joc.model.publish.git.commands.CommitFilter", "publish/git/commands/commitFilter-schema.json");
            put("com.sos.joc.model.publish.git.commands.CommonFilter", "publish/git/commands/commonFilter-schema.json");
            put("com.sos.joc.model.publish.git.commands.TagFilter", "publish/git/commands/tagFilter-schema.json");
            put("com.sos.joc.model.publish.repository.CopyToFilter", "publish/repository/copyToRepositoryFilter-schema.json");
            put("com.sos.joc.model.publish.repository.DeleteFromFilter", "publish/repository/deleteFromRepositoryFilter-schema.json");
            put("com.sos.joc.model.publish.repository.ReadFromFilter", "publish/repository/readFromRepositoryFilter-schema.json");
            put("com.sos.joc.model.publish.repository.UpdateFromFilter", "publish/repository/updateFromRepositoryFilter-schema.json");

            // schedules
            put("com.sos.webservices.order.initiator.model.ScheduleSelector", "orderManagement/orders/schedulesSelector-schema.json");

            put("com.sos.joc.model.dailyplan.DailyPlanOrderFilterDef", "orderManagement/dailyplan/dailyPlanOrdersFilterDefRequired-schema.json");
            put("com.sos.joc.model.dailyplan.DailyPlanModifyOrder", "orderManagement/dailyplan/dailyPlanModifyOrder-schema.json");
            put("com.sos.joc.model.dailyplan.generate.GenerateRequest", "dailyplan/generate/generate-request-schema.json");
            put("com.sos.joc.model.dailyplan.history.MainRequest", "dailyplan/history/main-request-schema.json");
            put("com.sos.joc.model.dailyplan.history.SubmissionsRequest", "dailyplan/history/submissions-request-schema.json");
            put("com.sos.joc.model.dailyplan.history.SubmissionsOrdersRequest", "dailyplan/history/submissions-orders-request-schema.json");
            put("com.sos.joc.model.dailyplan.submissions.SubmissionsDeleteRequest", "dailyplan/submissions/submissions-delete-request-schema.json");
            put("com.sos.joc.model.dailyplan.submissions.SubmissionsRequest", "dailyplan/submissions/submissions-request-schema.json");
            put("com.sos.joc.model.dailyplan.projections.ProjectionsRequest", "dailyplan/projections/projections-request-schema.json");
            put("com.sos.joc.model.dailyplan.projections.ProjectionsDayRequest", "dailyplan/projections/projections-day-request-schema.json");
            put("com.sos.joc.model.dailyplan.projections.ProjectionsScheduleRequest", "dailyplan/projections/projections-schedule-request-schema.json");

            put("com.sos.joc.model.yade.TransferFilter", "yade/transferFilter-schema.json");
            put("com.sos.joc.model.yade.TransferId", "yade/transferId-schema.json");
            put("com.sos.joc.model.yade.FileFilter", "yade/fileFilter-schema.json");
            put("com.sos.joc.model.yade.FilesFilter", "yade/filesFilter-schema.json");

            // monitor
            put("com.sos.joc.model.monitoring.ControllersFilter", "monitoring/controllers-filter-schema.json");
            put("com.sos.joc.model.monitoring.AgentsFilter", "monitoring/agents-filter-schema.json");
            // monitor order notifications
            put("com.sos.joc.model.monitoring.notification.order.OrderNotificationAcknowledgeFilter",
                    "monitoring/notification/order/order-notification-acknowledge-filter-schema.json");
            put("com.sos.joc.model.monitoring.notification.order.OrderNotificationFilter",
                    "monitoring/notification/order/order-notification-filter-schema.json");
            put("com.sos.joc.model.monitoring.notification.order.OrderNotificationsFilter",
                    "monitoring/notification/order/order-notifications-filter-schema.json");
            // monitor system notifications
            put("com.sos.joc.model.monitoring.notification.system.SystemNotificationAcknowledgeFilter",
                    "monitoring/notification/system/system-notification-acknowledge-filter-schema.json");
            put("com.sos.joc.model.monitoring.notification.system.SystemNotificationFilter",
                    "monitoring/notification/system/system-notification-filter-schema.json");
            put("com.sos.joc.model.monitoring.notification.system.SystemNotificationsFilter",
                    "monitoring/notification/system/system-notifications-filter-schema.json");

            // utilities
            put("com.sos.joc.model.dailyplan.RelativeDatesConverter", "orderManagement/dailyplan/relativeDatesConverter-schema.json");

            // auth
            put("SecurityConfiguration", "security/securityConfiguration/security-configuration-schema.json");
            put("SecurityConfigurationRole", "security/securityConfiguration/role-schema.json");
            put("Roles", "security/securityConfiguration/roles-schema.json");

            // iam
            put("com.sos.joc.model.security.identityservice.IdentityService", "security/identityServices/identityService-schema.json");
            put("com.sos.joc.model.security.identityservice.IdentityServiceRename", "security/identityServices/identityServiceRename-schema.json");
            put("com.sos.joc.model.security.identityservice.IdentityServiceFilter", "security/identityServices/identityServiceFilter-schema.json");
            put("com.sos.joc.model.security.identityservice.IdentityServicesFilter", "security/identityServices/identityServicesFilter-schema.json");

            put("com.sos.joc.model.security.folders.FolderFilter", "security/folders/folderFilter-schema.json");
            put("com.sos.joc.model.security.folders.FoldersFilter", "security/folders/foldersFilter-schema.json");
            put("com.sos.joc.model.security.folders.Folders", "security/folders/folders-schema.json");
            put("com.sos.joc.model.security.folders.FolderRename", "security/folders/folderRename-schema.json");
            put("com.sos.joc.model.security.folders.FolderListFilter", "security/folders/folderListFilter-schema.json");

            put("com.sos.joc.model.security.permissions.PermissionFilter", "security/permissions/permissionFilter-schema.json");
            put("com.sos.joc.model.security.permissions.PermissionsFilter", "security/permissions/permissionsFilter-schema.json");
            put("com.sos.joc.model.security.permissions.Permissions", "security/permissions/permissions-schema.json");
            put("com.sos.joc.model.security.permissions.PermissionRename", "security/permissions/permissionRename-schema.json");
            put("com.sos.joc.model.security.permissions.PermissionListFilter", "security/permissions/permissionListFilter-schema.json");

            put("com.sos.joc.model.security.accounts.AccountListFilter", "security/accounts/accountListFilter-schema.json");
            put("com.sos.joc.model.security.accounts.AccountNamesFilter", "security/accounts/accountNamesFilter-schema.json");
            put("com.sos.joc.model.security.accounts.AccountChangePassword", "security/accounts/accountChangePassword-schema.json");
            put("com.sos.joc.model.security.accounts.AccountFilter", "security/accounts/accountFilter-schema.json");
            put("com.sos.joc.model.security.accounts.AccountsFilter", "security/accounts/accountsFilter-schema.json");
            put("com.sos.joc.model.security.accounts.AccountRename", "security/accounts/accountRename-schema.json");
            put("com.sos.joc.model.security.accounts.Account", "security/accounts/account-schema.json");

            put("com.sos.joc.model.security.fido.FidoRegistrationListFilter", "security/fido/fidoRegistrationListFilter-schema.json");
            put("com.sos.joc.model.security.fido.FidoRegistrationsFilter", "security/fido/fidoRegistrationsFilter-schema.json");
            put("com.sos.joc.model.security.fido.FidoRegistrationFilter", "security/fido/fidoRegistrationFilter-schema.json");
            put("com.sos.joc.model.security.fido.FidoConfirmationFilter", "security/fido/fidoConfirmationFilter-schema.json");
            put("com.sos.joc.model.security.fido.FidoRegistration", "security/fido/fidoRegistration-schema.json");
            put("com.sos.joc.model.security.fido.FidoRegistrations", "security/fido/fidoRegistrations-schema.json");
            put("com.sos.joc.model.security.fido.FidoRequestAuthentication", "security/fido/fidoRequestAuthentication-schema.json");
            put("com.sos.joc.model.security.fido.FidoAddDevice", "security/fido/fidoAddDevice-schema.json");
            put("com.sos.joc.model.security.fido.FidoRemoveDevices", "security/fido/fidoRemoveDevices-schema.json");

            put("com.sos.joc.model.security.roles.RoleListFilter", "security/roles/roleListFilter-schema.json");
            put("com.sos.joc.model.security.roles.RolesFilter", "security/roles/rolesFilter-schema.json");
            put("com.sos.joc.model.security.roles.RoleFilter", "security/roles/roleFilter-schema.json");
            put("com.sos.joc.model.security.roles.RoleRename", "security/roles/roleRename-schema.json");
            put("com.sos.joc.model.security.roles.Role", "security/roles/role-schema.json");
            put("com.sos.joc.model.security.roles.RoleStore", "security/roles/roleStore-schema.json");

            put("com.sos.joc.model.security.blocklist.BlockedAccountsDeleteFilter", "security/blocklist/blockedAccountsDeleteFilter-schema.json");
            put("com.sos.joc.model.security.blocklist.BlockedAccountsFilter", "security/blocklist/blockedAccountsFilter-schema.json");
            put("com.sos.joc.model.security.blocklist.BlockedAccount", "security/blocklist/blockedAccount-schema.json");

            put("com.sos.joc.model.security.sessions.ActiveSessionsCancelFilter", "security/sessions/sessionsCancelFilter-schema.json");
            put("com.sos.joc.model.security.sessions.ActiveSessionsFilter", "security/sessions/sessionsFilter-schema.json");

            put("com.sos.joc.model.security.history.LoginHistoryFilter", "security/history/loginHistoryFilter-schema.json");

            put("com.sos.joc.model.security.locker.LockerFilter", "security/locker/lockerFilter-schema.json");
            put("com.sos.joc.model.security.locker.Locker", "security/locker/locker-schema.json");

            // Profiles
            put("com.sos.joc.model.profile.Profile", "profile/profile-schema.json");
            put("com.sos.joc.model.profile.ProfileFilter", "profile/profileFilter-schema.json");
            put("com.sos.joc.model.profile.ProfilesFilter", "profile/profilesFilter-schema.json");

            // notifications
            put("com.sos.joc.model.notification.ReadNotificationFilter", "notification/readNotificationFilter-schema.json");
            put("com.sos.joc.model.notification.DeleteNotificationFilter", "notification/deleteNotificationFilter-schema.json");
            put("com.sos.joc.model.notification.StoreNotificationFilter", "notification/storeNotificationFilter-schema.json");
            put("com.sos.joc.model.notification.ReleaseNotificationFilter", "notification/releaseNotificationFilter-schema.json");
            
            // utilities
            put("com.sos.joc.model.utilities.SendMail", "utilities/sendMail-schema.json");
            
            // reporting
            // obsolete put("com.sos.joc.model.reporting.OrderSteps", "reporting/orderSteps-schema.json");
            put("com.sos.joc.model.reporting.LoadFilter", "reporting/load-schema.json");
            put("com.sos.joc.model.reporting.RunFilter", "reporting/run-report-schema.json");


            // TODO complete the map
        }
    });

    /** Validation which raises all errors
     * 
     * @param json
     * @param schemaPath - path relative to ./resources/raml/schemas directory
     * @throws IOException
     * @throws SOSJsonSchemaException */
    public static void validate(byte[] json, String schemaPath) throws IOException, SOSJsonSchemaException {
        if (schemaPath != null) {
            validate(json, URI.create(BASE_URI + schemaPath), false, false, false);
        }
    }
    
    public static void validate(byte[] json, String schemaPath, boolean onlyFirstError) throws IOException, SOSJsonSchemaException {
        if (schemaPath != null) {
            validate(json, URI.create(BASE_URI + schemaPath), false, false, onlyFirstError);
        }
    }

    /** Validation which raises all errors
     * 
     * @param json
     * @param clazz
     * @throws IOException
     * @throws SOSJsonSchemaException */
    public static void validate(byte[] json, Class<?> clazz) throws IOException, SOSJsonSchemaException {
        validate(json, getSchemaPath(clazz), false);
    }
    
    public static void validate(byte[] json, Class<?> clazz, boolean onlyFirstError) throws IOException, SOSJsonSchemaException {
        validate(json, getSchemaPath(clazz), onlyFirstError);
    }

    /** Validation which raises all errors
     * 
     * @param json
     * @param schemaUri
     * @throws IOException
     * @throws SOSJsonSchemaException */
    public static void validate(byte[] json, URI schemaUri) throws IOException, SOSJsonSchemaException {
        if (schemaUri != null) {
            validate(json, schemaUri, false, false, false);
        }
    }
    
    public static void validate(byte[] json, URI schemaUri, boolean onlyFirstError) throws IOException, SOSJsonSchemaException {
        if (schemaUri != null) {
            validate(json, schemaUri, false, false, onlyFirstError);
        }
    }

    /** Fast validation which stops after first error
     * 
     * @param json
     * @param schemaPath - path relative to ./resources/raml/schemas directory
     * @throws IOException
     * @throws SOSJsonSchemaException */
    public static void validateFailFast(byte[] json, String schemaPath) throws IOException, SOSJsonSchemaException {
        if (schemaPath != null) {
            validate(json, URI.create(BASE_URI + schemaPath), true, false, true);
        }
    }

    /** Fast validation which stops after first error
     * 
     * @param json
     * @param clazz
     * @throws IOException
     * @throws SOSJsonSchemaException */
    public static void validateFailFast(byte[] json, Class<?> clazz) throws IOException, SOSJsonSchemaException {
        validateFailFast(json, getSchemaPath(clazz));
    }

    /** Fast validation which stops after first error
     * 
     * @param json
     * @param schemaUri
     * @throws IOException
     * @throws SOSJsonSchemaException */
    public static void validateFailFast(byte[] json, URI schemaUri) throws IOException, SOSJsonSchemaException {
        if (schemaUri != null) {
            validate(json, schemaUri, true, false, true);
        }
    }

    public static void validateStrict(byte[] json, String schemaPath) throws IOException, SOSJsonSchemaException {
        if (schemaPath != null) {
            validate(json, URI.create(BASE_URI + schemaPath), false, true, false);
        }
    }
    
    public static void validateStrict(byte[] json, String schemaPath, boolean onlyFirstError) throws IOException, SOSJsonSchemaException {
        if (schemaPath != null) {
            validate(json, URI.create(BASE_URI + schemaPath), false, true, onlyFirstError);
        }
    }

    public static void validateStrict(byte[] json, Class<?> clazz) throws IOException, SOSJsonSchemaException {
        validateStrict(json, getSchemaPath(clazz), false);
    }
    
    public static void validateStrict(byte[] json, Class<?> clazz, boolean onlyFirstError) throws IOException, SOSJsonSchemaException {
        validateStrict(json, getSchemaPath(clazz), onlyFirstError);
    }

    public static void validateStrict(byte[] json, URI schemaUri) throws IOException, SOSJsonSchemaException {
        if (schemaUri != null) {
            validate(json, schemaUri, false, true, false);
        }
    }
    
    public static void validateStrict(byte[] json, URI schemaUri, boolean onlyFirstError) throws IOException, SOSJsonSchemaException {
        if (schemaUri != null) {
            validate(json, schemaUri, false, true, onlyFirstError);
        }
    }

    // for testing
    protected static Map<String, String> getClassUriMap() {
        return CLASS_URI_MAPPING;
    }

    // protected for testing
    protected static JsonSchema getSchema(URI schemaUri, boolean failFast, boolean strict) {
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setTypeLoose(true);
        if (failFast) {
            config.setFailFast(true);
        }
        if (strict) {
            return FACTORY_V4_STRICT.getSchema(schemaUri, config);
        } else {
            return FACTORY_V4.getSchema(schemaUri, config);
        }
    }

    private static String getSchemaPath(Class<?> clazz) {
        String schemaPath = CLASS_URI_MAPPING.get(clazz.getSimpleName());
        if (schemaPath == null) {
            schemaPath = CLASS_URI_MAPPING.get(clazz.getName());
        }
        if (schemaPath == null) {
            LOGGER.warn("JSON Validation impossible: no schema specified for " + clazz.getName());
            return null;
        } else {
            return schemaPath;
        }
    }

    private static void validate(byte[] json, URI schemaUri, boolean failFast, boolean strict, boolean onlyFirstError) throws IOException,
            SOSJsonSchemaException {
        JsonSchema schema = getSchema(schemaUri, failFast, strict);
        Set<ValidationMessage> errors;
        try {
            errors = schema.validate(MAPPER.readTree(json));
            if (errors != null && !errors.isEmpty()) {
                if (onlyFirstError) {
                    throw new SOSJsonSchemaException(errors.iterator().next().toString());
                } else {
                    throw new SOSJsonSchemaException(errors.stream().map(ValidationMessage::toString).collect(Collectors.joining(" or ")));
                }
            }
        } catch (JsonParseException e) {
            throw e;
        } catch (JsonMappingException | JsonSchemaException e) {
            if (e.getCause() == null || e.getCause().getClass().isInstance(e)) {
                throw new SOSJsonSchemaException(e.getMessage());
            }
            LOGGER.warn("JSON Validation impossible: " + e.toString());
        }
    }

    /** Checks if object is valid without raising any exceptions.
     * 
     * @param json
     * @param schemaPath - path relative to ./resources/raml/schemas directory
     * 
     * @return true/false */
    public static boolean isValid(byte[] json, String schemaPath) {
        return isValid(json, URI.create(BASE_URI + schemaPath));
    }

    /** Checks if object is valid without raising any exceptions.
     * 
     * @param json
     * @param clazz
     * 
     * @return true/false */
    public static boolean isValid(byte[] json, Class<?> clazz) {
        return isValid(json, getSchemaPath(clazz));
    }

    /** Checks if object is valid without raising any exceptions.
     * 
     * @param json
     * @param schemaUri
     * 
     * @return true/false */
    public static boolean isValid(byte[] json, URI schemaUri) {
        if (schemaUri != null) {
            return isValid(json, schemaUri, true, false);
        } else {
            return false;
        }
    }

    public static boolean isValid(byte[] json, URI schemaUri, boolean failFast, boolean strict) {
        try {
            validate(json, schemaUri, failFast, strict, true);
            return true;
        } catch (IOException | SOSJsonSchemaException e) {
            return false;
        }
    }

}
