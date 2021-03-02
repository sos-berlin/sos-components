package com.sos.schema;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
    private static final ObjectMapper MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final SpecVersion.VersionFlag JSONDRAFT = SpecVersion.VersionFlag.V4;
    private static final List<NonValidationKeyword> NON_VALIDATION_KEYS = Arrays.asList(new NonValidationKeyword("javaType"),
            new NonValidationKeyword("javaInterfaces"), new NonValidationKeyword("javaEnumNames"), new NonValidationKeyword("extends"),
            new NonValidationKeyword("additionalProperties"), new NonValidationKeyword("propertyOrder"));
    private static final List<NonValidationKeyword> NON_VALIDATION_KEYS_STRICT = Arrays.asList(new NonValidationKeyword("javaType"),
            new NonValidationKeyword("javaInterfaces"), new NonValidationKeyword("javaEnumNames"), new NonValidationKeyword("extends"),
            new NonValidationKeyword("propertyOrder"));
    private static final JsonSchemaFactory FACTORY_V4 = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(JSONDRAFT)).addMetaSchema(
            JsonMetaSchema.builder(JsonMetaSchema.getV4().getUri(), JsonMetaSchema.getV4()).addKeywords(NON_VALIDATION_KEYS).build()).build();
    private static final JsonSchemaFactory FACTORY_V4_STRICT = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(JSONDRAFT)).addMetaSchema(
            JsonMetaSchema.builder(JsonMetaSchema.getV4().getUri(), JsonMetaSchema.getV4()).addKeywords(NON_VALIDATION_KEYS_STRICT).build()).build();

    private static final Map<String, String> CLASS_URI_MAPPING = Collections.unmodifiableMap(new HashMap<String, String>() {

        private static final long serialVersionUID = 1L;

        {   
            put("CalendarDatesFilter", "calendar/calendarDatesFilter-schema.json");
            put("CalendarsFilter", "calendar/calendarsFilter-schema.json");
            
            put("ClusterRestart", "cluster/restart-schema.json");
            put("ClusterSwitchMember", "cluster/switch-schema.json");

            put("DocumentationShowFilter", "docu/documentationShow-schema.json");
            put("DocumentationFilter", "docu/documentationFilter-schema.json");
            put("DocumentationsFilter", "docu/documentationsFilter-schema.json");
            put("DocumentationImport", "docu/documentationImport-schema.json");
            
            put("RegisterEvent", "event/register-schema.json"); //obsolete
            put("Controller", "event/controllerFilter-schema.json");
            

            put("ControllerId", "common/controllerId-schema.json");
            put("UrlParameter", "jobscheduler/urlParam-schema.json");
            put("RegisterParameters", "jobscheduler/registerParam-schema.json");
            put("StoreAgents", "agent/storeParam-schema.json");
            put("ReadAgents", "agent/readAgents-schema.json");
            put("ReadAgentsV", "agent/readAgents_v-schema.json");

            put("LockFilter", "lock/lockFilter-schema.json");
            put("LocksFilter", "lock/locksFilter-schema.json");
            
            put("WorkflowFilter", "workflow/workflowFilter-schema.json");
            put("WorkflowsFilter", "workflow/workflowsFilter-schema.json");

            put("OrderFilter", "order/orderFilter-schema.json");
            put("OrdersFilter", "order/ordersFilter-schema.json");
            put("OrdersFilterV", "order/ordersFilterV-schema.json");
            put("OrderHistoryFilter", "order/orderHistoryFilter-schema.json");
            put("OrderRunningLogFilter", "order/orderRunningLogFilter-schema.json");
            put("AddOrders", "order/addOrders-schema.json");
            put("ModifyOrders", "order/modifyOrders-schema.json");
            
            put("ScheduleDatesFilter", "orderManagement/scheduleDatesFilter-schema.json");
            
            
            put("JobsFilter", "job/jobsFilter-schema.json");
            put("TaskFilter", "job/taskFilter-schema.json");
            put("RunningTaskLogsFilter", "job/runningTaskLogsFilter-schema.json");

            put("ApplyConfiguration", "xmleditor/apply/apply-configuration-schema.json");
            put("SchemaAssignConfiguration", "xmleditor/schema/assign/schema-assign-configuration-schema.json");
            put("DeleteAll", "xmleditor/delete/all/delete-all-schema.json");
            put("DeleteDraft", "xmleditor/delete/delete-draft-schema.json");
            put("DeployConfiguration", "xmleditor/deploy/deploy-configuration-schema.json");
            put("ReadConfiguration", "xmleditor/read/read-configuration-schema.json");
            put("SchemaReassignConfiguration", "xmleditor/schema/reassign/schema-reassign-configuration-schema.json");
            put("RenameConfiguration", "xmleditor/rename/rename-configuration-schema.json");
            put("StoreConfiguration", "xmleditor/store/store-configuration-schema.json");
            put("ValidateConfiguration", "xmleditor/validate/validate-configuration-schema.json");
            put("Xml2JsonConfiguration", "xmleditor/xml2json/xml2json-configuration-schema.json");

            put("TreeFilter", "tree/treeFilter-schema.json");

            put("ReleaseFilter", "inventory/release/release-schema.json");
            put("RequestFolder", "inventory/common/request-folder-schema.json");
            put("com.sos.joc.model.inventory.common.RequestFilter", "inventory/common/request-filter-schema.json");
            put("com.sos.joc.model.inventory.common.RequestFilters", "inventory/common/request-filters-schema.json");
            put("com.sos.joc.model.inventory.deploy.DeployableFilter", "inventory/deploy/request-deployable-schema.json");
            put("com.sos.joc.model.inventory.deploy.DeployablesFilter", "inventory/deploy/request-deployables-schema.json");
            put("com.sos.joc.model.inventory.release.ReleasableFilter", "inventory/release/request-releasable-schema.json");
            put("com.sos.joc.model.inventory.release.ReleasablesFilter", "inventory/release/request-releasables-schema.json");
            put("com.sos.joc.model.inventory.rename.RequestFilter", "inventory/rename/request-filter-schema.json");
            put("com.sos.joc.model.inventory.copy.RequestFilter", "inventory/copy/request-filter-schema.json");
            put("com.sos.joc.model.inventory.restore.RequestFilter", "inventory/restore/request-filter-schema.json");
            put("com.sos.joc.model.inventory.ConfigurationObject", "inventory/configurationObject-schema.json");
            put("com.sos.joc.model.inventory.path.PathFilter","inventory/path/pathFilter-schema.json");

            put("com.sos.joc.model.publish.SetKeyFilter", "publish/setKey-schema.json");
            put("com.sos.joc.model.publish.GenerateKeyFilter", "publish/generateKey-schema.json");
            put("com.sos.joc.model.publish.ExportFilter", "publish/exportFilter-schema.json");
            put("com.sos.joc.model.publish.ImportFilter", "publish/importFilter-schema.json");
            put("com.sos.joc.model.publish.DeployFilter", "publish/deploy-schema.json");
            put("com.sos.joc.model.publish.ImportDeployFilter", "publish/importDeployFilter-schema.json");
            put("com.sos.joc.model.publish.SetVersionFilter", "publish/setVersion-schema.json");
            put("com.sos.joc.model.publish.SetVersionsFilter", "publish/setVersions-schema.json");
            put("com.sos.joc.model.publish.ShowDepHistoryFilter", "publish/showDepHistoryFilter-schema.json");
            put("com.sos.joc.model.publish.ShowDepHistoryFilter", "publish/showDepHistoryFilter-schema.json");
            put("com.sos.joc.model.publish.RedeployFilter", "publish/redeployFilter-schema.json");
           
            put("com.sos.joc.model.dailyplan.DailyPlanSubmissionsFilter", "orderManagement/dailyplan/dailyplanSubmissionsFilter-schema.json");
            put("com.sos.joc.model.dailyplan.DailyPlanOrderFilter", "orderManagement/dailyplan/dailyPlanOrdersFilter-schema.json");
            put("com.sos.joc.model.dailyplan.DailyPlanModifyOrder", "orderManagement/dailyplan/dailyPlanModifyOrder-schema.json");
            put("com.sos.joc.model.dailyplan.DailyPlanOrderSelector", "orderManagement/dailyplan/dailyPlanOrdersSelector-schema.json");
            put("com.sos.joc.model.dailyplan.DailyPlanSubmissionsFilter", "orderManagement/dailyplan/dailyplanSubmissionsFilter-schema.json");
            put("com.sos.webservices.order.initiator.model.ScheduleSelector", "orderManagement/orders/schedulesSelector-schema.json");
            put("com.sos.joc.model.dailyplan.RelativeDatesConverter", "orderManagement/dailyplan/relativeDatesConverter-schema.json");
            
            //put("com.sos.joc.model.publish.RedeployFilter", "publish/redeploy-schema.json");

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
            validate(json, URI.create("classpath:/raml/api/schemas/" + schemaPath), false, false);
        }
    }
    
    /** Validation which raises all errors
     * 
     * @param json
     * @param clazz
     * @throws IOException
     * @throws SOSJsonSchemaException */
    public static void validate(byte[] json, Class<?> clazz) throws IOException, SOSJsonSchemaException {
        validate(json, getSchemaPath(clazz));
    }
    /** Validation which raises all errors
     * 
     * @param json
     * @param schemaUri
     * @throws IOException
     * @throws SOSJsonSchemaException */
    public static void validate(byte[] json, URI schemaUri) throws IOException, SOSJsonSchemaException {
        if (schemaUri != null) {
            validate(json, schemaUri, false, false);
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
            validate(json, URI.create("classpath:/raml/api/schemas/" + schemaPath), true, false);
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
            validate(json, schemaUri, true, false);
        }
    }
    
    public static void validateStrict(byte[] json, String schemaPath) throws IOException, SOSJsonSchemaException {
        if (schemaPath != null) {
            validate(json, URI.create("classpath:/raml/api/schemas/" + schemaPath), false, true);
        }
    }
    
    public static void validateStrict(byte[] json, Class<?> clazz) throws IOException, SOSJsonSchemaException {
        validateStrict(json, getSchemaPath(clazz));
    }
    
    public static void validateStrict(byte[] json, URI schemaUri) throws IOException, SOSJsonSchemaException {
        if (schemaUri != null) {
            validate(json, schemaUri, false, true);
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

    private static void validate(byte[] json, URI schemaUri, boolean failFast, boolean strict) throws IOException, SOSJsonSchemaException {
        JsonSchema schema = getSchema(schemaUri, failFast, strict);
        Set<ValidationMessage> errors;
        try {
            errors = schema.validate(MAPPER.readTree(json));
            if (errors != null && !errors.isEmpty()) {
                throw new SOSJsonSchemaException(errors.toString());
            }
        } catch (JsonParseException e) {
            throw e;
        } catch (JsonSchemaException e) {
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
     * @return true/false
     *
     **/
    public static boolean isValid(byte[] json, String schemaPath) {
        return isValid(json, URI.create("classpath:/raml/api/schemas/" + schemaPath));
    }
    
    /** Checks if object is valid without raising any exceptions.
     * 
     * @param json
     * @param clazz
     * 
     * @return true/false
     *
     **/
   public static boolean isValid(byte[] json, Class<?> clazz) {
        return isValid(json, getSchemaPath(clazz));
    }

   /** Checks if object is valid without raising any exceptions.
    * 
    * @param json
    * @param schemaUri
    * 
    * @return true/false
    *
    **/
    public static boolean isValid(byte[] json, URI schemaUri) {
        if (schemaUri != null) {
            return isValid(json, schemaUri, true, false);
        } else {
            return false;
        }
    }

    public static boolean isValid(byte[] json, URI schemaUri, boolean failFast, boolean strict) {
        try {
            validate(json, schemaUri, failFast, strict);
            return true;
        } catch (IOException | SOSJsonSchemaException e) {
            return false;
        }
    }

}
