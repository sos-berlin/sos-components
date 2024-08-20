package com.sos.joc.inventory.impl;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.jobtemplate.JobTemplate;
import com.sos.inventory.model.schedule.OrderParameterisation;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.workflow.ListParameter;
import com.sos.inventory.model.workflow.ParameterType;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.audit.JocAuditObjectsLog;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JsonSerializer;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.classes.inventory.WorkflowConverter;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.dailyplan.impl.DailyPlanCancelOrderImpl;
import com.sos.joc.dailyplan.impl.DailyPlanDeleteOrdersImpl;
import com.sos.joc.dailyplan.impl.DailyPlanOrdersGenerateImpl;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.ControllerInvalidResponseDataException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocReleaseException;
import com.sos.joc.inventory.resource.IReleaseResource;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilterDef;
import com.sos.joc.model.dailyplan.DailyPlanOrderStateText;
import com.sos.joc.model.dailyplan.generate.GenerateRequest;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.RequestFilter;
import com.sos.joc.model.inventory.release.ReleaseFilter;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.base.problem.Problem;

@Path(JocInventory.APPLICATION_PATH)
public class ReleaseResourceImpl extends JOCResourceImpl implements IReleaseResource {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseResourceImpl.class);

    @Override
    public JOCDefaultResponse release(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, ReleaseFilter.class, true);
            ReleaseFilter in = Globals.objectMapper.readValue(inBytes, ReleaseFilter.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());
            if (response == null) {
                response = getReleaseResponse(in, true, accessToken);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private JOCDefaultResponse getReleaseResponse(ReleaseFilter in, boolean withDeletionOfEmptyFolders, String accessToken) throws Exception {
        List<Err419> errors = release(in, getJocError(), withDeletionOfEmptyFolders, accessToken);
        if (errors != null && !errors.isEmpty()) {
            return JOCDefaultResponse.responseStatus419(errors);
        }
        return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
    }

    private List<Err419> release(ReleaseFilter in, JocError jocError, boolean withDeletionOfEmptyFolders, String accessToken) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            session.setAutoCommit(false);

            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            Globals.beginTransaction(session);
            // released schedules with referenced workflows
            Map<String, List<String>> renamedOldSchedulePathsWithWorkflowNames = getReleasedSchedulePathsWithWorkflowNames(in, dbLayer);
            // schedules from the request
            Set<String> inSchedulesPaths = in.getUpdate().stream().filter(r -> r.getObjectType().equals(ConfigurationType.SCHEDULE)).map(
                    RequestFilter::getPath).collect(Collectors.toSet());
            // already releasedschedules with no renaming
            Set<String> keys = renamedOldSchedulePathsWithWorkflowNames.keySet().stream().filter(path -> inSchedulesPaths.contains(path))
                .collect(Collectors.toSet());
            // remove schedules that are not renamed
            keys.stream().forEach(key -> renamedOldSchedulePathsWithWorkflowNames.remove(key));
            // cancel based on the old name of renamed schedules 
            if(!renamedOldSchedulePathsWithWorkflowNames.isEmpty()) {
              cancelOrdersForRenamedSchedules(in.getAddOrdersDateFrom(), renamedOldSchedulePathsWithWorkflowNames, dbLayer, accessToken);
            }
            Globals.commit(session);
            Globals.beginTransaction(session);
            List<Err419> errors = new ArrayList<>();

            DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());
            JocAuditObjectsLog auditLogObjectsLogging = new JocAuditObjectsLog(dbAuditLog.getId());

            if (in.getDelete() != null && !in.getDelete().isEmpty()) {
                errors.addAll(delete(in.getDelete(), dbLayer, folderPermissions, getJocError(), dbAuditLog, auditLogObjectsLogging,
                        withDeletionOfEmptyFolders));
            }

            if (in.getUpdate() != null && !in.getUpdate().isEmpty()) {
                errors.addAll(update(in.getUpdate(), dbLayer, folderPermissions, getJocError(), dbAuditLog, auditLogObjectsLogging,
                        withDeletionOfEmptyFolders));
            }

            if (errors != null && !errors.isEmpty()) {
                Globals.rollback(session);
                return errors;
            }
            Globals.commit(session);
            auditLogObjectsLogging.log();
            Globals.beginTransaction(session);
            Map<String, List<String>> schedulePathsWithWorkflowNames = getSchedulePathsWithWorkflowNames(in, dbLayer);
            cancelAndRecreateOrders(in, schedulePathsWithWorkflowNames,  accessToken);
            Globals.commit(session);
            return Collections.emptyList();
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private static List<Err419> delete(List<RequestFilter> toDelete, InventoryDBLayer dbLayer, SOSAuthFolderPermissions folderPermissions,
            JocError jocError, DBItemJocAuditLog dbAuditLog, JocAuditObjectsLog auditLogObjectsLogging, boolean withDeletionOfEmptyFolders) {
        List<Err419> bulkErrors = new ArrayList<>();
        for (RequestFilter requestFilter : toDelete) {
            if (requestFilter == null) {
                continue;
            }
            try {
                DBItemInventoryConfiguration conf = JocInventory.getConfiguration(dbLayer, requestFilter, folderPermissions);
                delete(conf, dbLayer, dbAuditLog, withDeletionOfEmptyFolders, auditLogObjectsLogging, true);
            } catch (DBMissingDataException ex) {
                // ignore missing objects at deletion
            } catch (Exception ex) {
                if (requestFilter.getPath() != null) {
                    bulkErrors.add(new BulkError(LOGGER).get(ex, jocError, requestFilter.getPath()));
                } else {
                    bulkErrors.add(new BulkError(LOGGER).get(ex, jocError, "Id: " + requestFilter.getId()));
                }
            }
        }
        return bulkErrors;

        // Less memory and performance but sometimes SOSHibernateObjectOperationStaleStateException:
        // Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect)
        // so better a serial processing
        // return toDelete.stream().filter(Objects::nonNull).map(requestFilter -> {
        // Either<Err419, Void> either = null;
        // try {
        // DBItemInventoryConfiguration conf = JocInventory.getConfiguration(dbLayer, requestFilter, folderPermissions);
        // delete(conf, dbLayer, dbAuditLog, withDeletionOfEmptyFolders, true);
        // either = Either.right(null);
        // } catch (DBMissingDataException ex) {
        // // ignore missing objects at deletion
        // either = Either.right(null);
        // } catch (Exception ex) {
        // if (requestFilter.getPath() != null) {
        // either = Either.left(new BulkError(LOGGER).get(ex, jocError, requestFilter.getPath()));
        // } else {
        // either = Either.left(new BulkError(LOGGER).get(ex, jocError, "Id: " + requestFilter.getId()));
        // }
        // }
        // return either;
        // }).filter(Either::isLeft).map(Either::getLeft).collect(Collectors.toList());
    }

    public static void delete(DBItemInventoryConfiguration conf, InventoryDBLayer dbLayer, DBItemJocAuditLog dbAuditLog,
            boolean withDeletionOfEmptyFolders, JocAuditObjectsLog auditLogObjectsLogging, boolean withEvent) throws SOSHibernateException {

        if (ConfigurationType.FOLDER.intValue() == conf.getType()) {
            deleteReleasedFolder(conf, dbLayer, dbAuditLog, withDeletionOfEmptyFolders, auditLogObjectsLogging);
            if (withEvent) {
                JocInventory.postEvent(conf.getFolder());
            }
        } else if (!JocInventory.isReleasable(conf.getTypeAsEnum())) {
            throw new ControllerInvalidResponseDataException(String.format("%s is not a 'Scheduling Object': %s", conf.getPath(), conf
                    .getTypeAsEnum()));
        } else {
            deleteReleasedObject(conf, dbAuditLog, dbLayer, auditLogObjectsLogging);
            if (withEvent) {
                JocInventory.postEvent(conf.getFolder());
            }
        }
        // TODO post event: InventoryTaggingUpdated ??
    }

    private List<Err419> update(List<RequestFilter> toUpdate, InventoryDBLayer dbLayer, SOSAuthFolderPermissions folderPermissions, JocError jocError,
            DBItemJocAuditLog dbAuditLog, JocAuditObjectsLog auditLogObjectsLogging, boolean withDeletionOfEmptyFolders) {

        List<Err419> bulkErrors = new ArrayList<>();
        Map<String, Workflow> cachedWorkflows = new HashMap<>();
        for (RequestFilter requestFilter : toUpdate) {
            if (requestFilter == null) {
                continue;
            }
            try {
                DBItemInventoryConfiguration conf = JocInventory.getConfiguration(dbLayer, requestFilter, folderPermissions);
                if (ConfigurationType.FOLDER.intValue() == conf.getType()) {
                    bulkErrors.addAll(updateReleasedFolder(conf, dbLayer, cachedWorkflows, dbAuditLog, auditLogObjectsLogging));
                    JocInventory.postEvent(conf.getFolder());
                } else if (!JocInventory.isReleasable(conf.getTypeAsEnum())) {
                    throw new ControllerInvalidResponseDataException(String.format("%s is not a 'Realeasable Object': %s", conf.getPath(), conf
                            .getTypeAsEnum()));
                } else if (!conf.getValid()) {
                    throw new ControllerInvalidResponseDataException(String.format("%s is not valid", conf.getPath()));
                } else {
                    bulkErrors.addAll(updateReleasedObject(conf, dbLayer, cachedWorkflows, dbAuditLog, auditLogObjectsLogging));
                    JocInventory.postEvent(conf.getFolder());
                }
            } catch (Exception ex) {
                if (requestFilter.getPath() != null) {
                    bulkErrors.add(new BulkError(LOGGER).get(ex, jocError, requestFilter.getPath()));
                } else {
                    bulkErrors.add(new BulkError(LOGGER).get(ex, jocError, "Id: " + requestFilter.getId()));
                }
            }
        }
        return bulkErrors;

        // Less memory and performance but sometimes SOSHibernateObjectOperationStaleStateException:
        // Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect)
        // so better a serial processing
        // return toUpdate.stream().filter(Objects::nonNull).map(requestFilter -> {
        // Either<Err419, Void> either = null;
        // try {
        // DBItemInventoryConfiguration conf = JocInventory.getConfiguration(dbLayer, requestFilter, folderPermissions);
        // if (ConfigurationType.FOLDER.intValue() == conf.getType()) {
        // updateReleasedFolder(conf, dbLayer, dbAuditLog);
        // JocInventory.postEvent(conf.getFolder());
        // } else if (!JocInventory.isReleasable(conf.getTypeAsEnum())) {
        // throw new ControllerInvalidResponseDataException(String.format("%s is not a 'Scheduling Object': %s", conf.getPath(), conf
        // .getTypeAsEnum()));
        // } else {
        // updateReleasedObject(conf, dbLayer, dbAuditLog);
        // JocInventory.postEvent(conf.getFolder());
        // }
        // either = Either.right(null);
        // } catch (Exception ex) {
        // if (requestFilter.getPath() != null) {
        // either = Either.left(new BulkError(LOGGER).get(ex, jocError, requestFilter.getPath()));
        // } else {
        // either = Either.left(new BulkError(LOGGER).get(ex, jocError, "Id: " + requestFilter.getId()));
        // }
        // }
        // return either;
        //
        // }).filter(Either::isLeft).map(Either::getLeft).collect(Collectors.toList());
    }

    private List<Err419> updateReleasedFolder(DBItemInventoryConfiguration conf, InventoryDBLayer dbLayer, Map<String, Workflow> cachedWorkflows,
            DBItemJocAuditLog dbAuditLog, JocAuditObjectsLog auditLogObjectsLogging) throws SOSHibernateException, JsonParseException,
            JsonMappingException, IOException {
        List<Err419> errors = new ArrayList<>();
        List<DBItemInventoryConfiguration> folderContent = dbLayer.getFolderContent(conf.getPath(), true, JocInventory.getReleasableTypes(), false);

        // quick and dirty TODO version with more performance
        if (folderContent != null && !folderContent.isEmpty()) {
            // createAuditLog(conf, conf.getTypeAsEnum(), auditLogger, auditParams);
            conf.setAuditLogId(dbAuditLog.getId());
            for (DBItemInventoryConfiguration item : folderContent) {
                if (item.getReleased() || !item.getValid()) {
                    continue;
                }
                errors.addAll(updateReleasedObject(item, dbLayer, cachedWorkflows, dbAuditLog, auditLogObjectsLogging));
            }
        }
        return errors;
    }

    private List<Err419> updateReleasedObject(DBItemInventoryConfiguration conf, InventoryDBLayer dbLayer, Map<String, Workflow> cachedWorkflows,
            DBItemJocAuditLog dbAuditLog, JocAuditObjectsLog auditLogObjectsLogging) throws SOSHibernateException, JsonParseException,
            JsonMappingException, IOException {

        List<Err419> errors = checkConfiguration(dbLayer, conf, cachedWorkflows);
        if (errors.size() > 0) {
            return errors;
        }

        conf.setAuditLogId(dbAuditLog.getId());

        DBItemInventoryReleasedConfiguration releaseItem = dbLayer.getReleasedItemByConfigurationId(conf.getId());
        // Less memory and performance but sometimes SOSHibernateObjectOperationStaleStateException:
        // Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect)
        // DBItemInventoryReleasedConfiguration contraintReleaseItem = dbLayer.getReleasedConfiguration(conf.getName(), conf.getType());

        if (releaseItem == null) {
            // delete all other db items with same objectType and name
            dbLayer.deleteContraintViolatedReleasedConfigurations(null, conf.getName(), conf.getType());
            // if (contraintReleaseItem != null) {
            // dbLayer.getSession().delete(contraintReleaseItem);
            // }
            dbLayer.getSession().save(setReleaseItem(null, conf, dbAuditLog.getCreated()));
        } else {
            // delete all other db items with same objectType and name but different id
            dbLayer.deleteContraintViolatedReleasedConfigurations(releaseItem.getId(), conf.getName(), conf.getType());
            // if (contraintReleaseItem != null && contraintReleaseItem.getId() != releaseItem.getId()) {
            // dbLayer.getSession().delete(contraintReleaseItem);
            // }
            dbLayer.getSession().update(setReleaseItem(releaseItem, conf, dbAuditLog.getCreated()));
        }
        conf.setReleased(true);
        conf.setModified(dbAuditLog.getCreated());
        dbLayer.getSession().update(conf);
        auditLogObjectsLogging.addDetail(JocAuditLog.storeAuditLogDetail(new AuditLogDetail(conf.getPath(), conf.getType()), dbLayer.getSession(),
                dbAuditLog));

        return errors;
    }

    private DBItemInventoryReleasedConfiguration setReleaseItem(DBItemInventoryReleasedConfiguration item, DBItemInventoryConfiguration conf,
            Date now) {
        if (item == null) {
            item = new DBItemInventoryReleasedConfiguration();
            item.setId(null);
            item.setCreated(now);
            item.setCid(conf.getId());
        }
        item.setAuditLogId(conf.getAuditLogId());
        item.setContent(conf.getContent());
        item.setFolder(conf.getFolder());
        item.setModified(now);
        item.setName(conf.getName());
        item.setPath(conf.getPath());
        item.setTitle(conf.getTitle());
        item.setType(conf.getType());
        return item;
    }

    private List<Err419> checkConfiguration(InventoryDBLayer dbLayer, DBItemInventoryConfiguration item, Map<String, Workflow> cachedWorkflows) {
        List<Err419> errors = new ArrayList<>();
        switch (item.getTypeAsEnum()) {
        case SCHEDULE:
            errors.addAll(checkScheduleConfiguration(dbLayer, item, cachedWorkflows));
            break;
        case JOBTEMPLATE:
            try {
                addHash(item);
            } catch (Exception e) {
                errors.add(new BulkError(LOGGER).get(new JocReleaseException(ConfigurationType.JOBTEMPLATE, item.getPath(), e), getJocError(), item
                        .getPath()));
            }
        default:
            break;
        }
        return errors;
    }

    private List<Err419> checkScheduleConfiguration(InventoryDBLayer dbLayer, DBItemInventoryConfiguration item,
            Map<String, Workflow> cachedWorkflows) {
        List<Err419> errors = new ArrayList<>();
        try {
            Schedule s = Globals.objectMapper.readValue(item.getContent(), Schedule.class);
            s = JocInventory.setWorkflowNames(s);
            if (s.getWorkflowNames().size() >= JocInventory.SCHEDULE_MIN_MULTIPLE_WORKFLOWS_SIZE) {// check only multiple workflows
                for (String name : s.getWorkflowNames()) {
                    String workflowMsg = "[workflow=" + name + "]";
                    try {
                        Workflow w = cachedWorkflows.get(name);
                        if (w == null) {
                            String wj = dbLayer.getDeployedJsonByConfigurationName(ConfigurationType.WORKFLOW, name);
                            if (wj == null) {
                                throw new Exception(workflowMsg + " couldn't find workflow deployment");
                            }
                            w = WorkflowConverter.convertInventoryWorkflow(wj);
                            cachedWorkflows.put(name, w);
                        }
                        // find required param in orderPreparation
                        if (Validator.orderPreparationHasRequiredParameters(w.getOrderPreparation())) {
                            throw new Exception(workflowMsg + "release of multiple workflows with required order variables are not allowed");
                        }
                    } catch (Throwable e) {
                        errors.add(new BulkError(LOGGER).get(new JocReleaseException(ConfigurationType.SCHEDULE, item.getPath(), e), getJocError(),
                                item.getPath()));
                    }
                }
            } else if (s.getWorkflowNames().size() == 1 && s.getOrderParameterisations() != null) {
                // Schedule's OrderParameterisations contains Listvariable
                if (s.getOrderParameterisations().stream().filter(op -> op.getVariables() != null).filter(op -> op.getVariables()
                        .getAdditionalProperties() != null).anyMatch(op -> op.getVariables().getAdditionalProperties().values().stream().anyMatch(
                                o -> (o instanceof List<?>)))) {
                    
                    String name = s.getWorkflowNames().get(0);
                    String workflowMsg = "[workflow=" + name + "]";
                    try {
                        Workflow w = cachedWorkflows.get(name);
                        if (w == null) {
                            String wj = dbLayer.getDeployedJsonByConfigurationName(ConfigurationType.WORKFLOW, name);
                            if (wj == null) {
                                wj = dbLayer.getConfigurationProperty(name, ConfigurationType.WORKFLOW.intValue(), "content");
                            }
                            if (wj == null) {
                                throw new DBMissingDataException(workflowMsg + " couldn't find workflow deployment");
                            }
                            w = WorkflowConverter.convertInventoryWorkflow(wj);
                            cachedWorkflows.put(name, w);
                        }
                        Requirements r = w.getOrderPreparation();
                        if (r != null && r.getParameters() != null && r.getParameters().getAdditionalProperties() != null) {
                            for (OrderParameterisation op : s.getOrderParameterisations()) {
                                if (op.getVariables() != null && op.getVariables().getAdditionalProperties() != null) {
                                    // find list params in orderPreparation
                                    r.getParameters().getAdditionalProperties().forEach((k, v) -> {
                                        if (ParameterType.List.equals(v.getType())) {
                                            if (v.getListParameters() != null && v.getListParameters().getAdditionalProperties() != null) {
                                                Map<String, ListParameter> declaration = v.getListParameters().getAdditionalProperties();

                                                @SuppressWarnings("unchecked")
                                                List<Map<String, Object>> listParams = (List<Map<String, Object>>) op.getVariables()
                                                        .getAdditionalProperties().get(k);
                                                listParams.forEach(p -> {
                                                    declaration.forEach((k1, v1) -> {
                                                        if (p.get(k1) == null && v1.getDefault() != null) {
                                                            p.put(k1, v1.getDefault());
                                                        }
                                                    });
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                            item.setContent(Globals.objectMapper.writeValueAsString(s));
                        }
                    } catch (Throwable e) {
                        errors.add(new BulkError(LOGGER).get(new JocReleaseException(ConfigurationType.SCHEDULE, item.getPath(), e), getJocError(),
                                item.getPath()));
                    }
                }
            }
        } catch (Throwable e) {
            errors.add(new BulkError(LOGGER).get(new JocReleaseException(ConfigurationType.SCHEDULE, item.getPath(), e), getJocError(), item.getPath()));
        }
        return errors;
    }

    private static void deleteReleasedFolder(DBItemInventoryConfiguration conf, InventoryDBLayer dbLayer, DBItemJocAuditLog dbAuditLog,
            boolean withDeletionOfEmptyFolders, JocAuditObjectsLog auditLogObjectsLogging) throws SOSHibernateException {
        List<DBItemInventoryConfiguration> folderContent = dbLayer.getFolderContent(conf.getPath(), true, JocInventory.getReleasableTypes(), false);

        if (folderContent != null && !folderContent.isEmpty()) {
            dbLayer.deleteReleasedItemsByConfigurationIds(folderContent.stream().map(DBItemInventoryConfiguration::getId).distinct().collect(
                    Collectors.toList()));
            for (DBItemInventoryConfiguration item : folderContent) {
                auditLogObjectsLogging.addDetail(JocAuditLog.storeAuditLogDetail(new AuditLogDetail(item.getPath(), item.getType()), dbLayer
                        .getSession(), dbAuditLog));
                // delete releasable objects in INV_CONFIGURATION
                JocInventory.deleteInventoryConfigurationAndPutToTrash(item, dbLayer, ConfigurationType.FOLDER);
            }
        }
        if (withDeletionOfEmptyFolders) {
            JocInventory.deleteEmptyFolders(dbLayer, conf);
        }

    }

    private static void deleteReleasedObject(DBItemInventoryConfiguration conf, DBItemJocAuditLog dbAuditLog, InventoryDBLayer dbLayer,
            JocAuditObjectsLog auditLogObjectsLogging) throws SOSHibernateException {
        conf.setAuditLogId(dbAuditLog.getId());
        dbLayer.deleteReleasedItemsByConfigurationIds(Collections.singletonList(conf.getId()));
        JocInventory.deleteInventoryConfigurationAndPutToTrash(conf, dbLayer, ConfigurationType.FOLDER);
        auditLogObjectsLogging.addDetail(JocAuditLog.storeAuditLogDetail(new AuditLogDetail(conf.getPath(), conf.getType()), dbLayer.getSession(),
                dbAuditLog));
    }
    
    private static void addHash(DBItemInventoryConfiguration conf) throws IOException {
        if (ConfigurationType.JOBTEMPLATE.intValue() == conf.getType()) {
            JobTemplate jt = JsonSerializer.emptyValuesToNull((JobTemplate) JocInventory.content2IJSObject(conf.getContent(),
                    ConfigurationType.JOBTEMPLATE));
            jt.setHash(JocInventory.hash(jt));
            conf.setContent(Globals.objectMapper.writeValueAsString(jt));
        }
    }
    
    private Map<String, List<String>> getReleasedSchedulePathsWithWorkflowNames (ReleaseFilter in, InventoryDBLayer dbLayer) {
        // in case a schedule has been renamed, this collects the schedules with their old names to be able to delete orders referencing this
        Map<String, List<String>> schedulePathsWithWorkflowNames = new HashMap<String, List<String>>();
        List<RequestFilter> all = Stream.concat(in.getDelete().stream(), in.getUpdate().stream()).collect(Collectors.toList()); 
        for (RequestFilter requestFilter : all) {
            if (requestFilter == null) {
                continue;
            }
            try {
                DBItemInventoryConfiguration conf = JocInventory.getConfiguration(dbLayer, requestFilter, folderPermissions);
                if (conf != null) {
                    DBItemInventoryReleasedConfiguration releasedConf = dbLayer.getReleasedConfigurationByInvId(conf.getId());
                    if (releasedConf != null) {
                        if (ConfigurationType.SCHEDULE.equals(conf.getTypeAsEnum())) {
                            // only add if planOrderAutomatically = true
                            Schedule schedule = Globals.objectMapper.readValue(releasedConf.getContent(), Schedule.class);
                            if (schedule.getPlanOrderAutomatically()) {
                                if (releasedConf.getContent().contains("workflowNames")) {
                                    List<String> workflowNames = JocInventory.getWorkflowNamesFromScheduleJson(releasedConf.getContent());
                                    if (schedulePathsWithWorkflowNames.containsKey(releasedConf.getPath())) {
                                        schedulePathsWithWorkflowNames.get(releasedConf.getPath()).addAll(workflowNames);
                                    } else {
                                        schedulePathsWithWorkflowNames.put(releasedConf.getPath(), workflowNames);
                                    }
                                }
                            }
                        } else if (ConfigurationType.WORKINGDAYSCALENDAR.equals(releasedConf.getTypeAsEnum()) 
                                || ConfigurationType.NONWORKINGDAYSCALENDAR.equals(releasedConf.getTypeAsEnum())) {
                            // only add if planOrderAutomatically = true
                            List<DBItemInventoryReleasedConfiguration> schedules = dbLayer.getUsedReleasedSchedulesByReleasedCalendarName(releasedConf.getName());
                            if (schedules != null) {
                                for (DBItemInventoryReleasedConfiguration scheduleDbItem : schedules) {
                                    Schedule schedule = Globals.objectMapper.readValue(scheduleDbItem.getContent(), Schedule.class);
                                    if (schedule.getPlanOrderAutomatically()) {
                                        List<String> workflowNames = JocInventory.getWorkflowNamesFromScheduleJson(scheduleDbItem.getContent());
                                        if (schedulePathsWithWorkflowNames.containsKey(scheduleDbItem.getPath())) {
                                            schedulePathsWithWorkflowNames.get(scheduleDbItem.getPath()).addAll(workflowNames);
                                        } else {
                                            schedulePathsWithWorkflowNames.put(scheduleDbItem.getPath(), workflowNames);
                                        }
                                    }
                                }
                            }
                        } 
                    }
                }
            } catch (Exception ex) {
                // ignore missing objects as it should not break the general release process
            }
        }
        return schedulePathsWithWorkflowNames;
    }
    
    private Map<String, List<String>> getSchedulePathsWithWorkflowNames (ReleaseFilter in, InventoryDBLayer dbLayer) {
        Map<String, List<String>> schedulePathsWithWorkflowNames = new HashMap<String, List<String>>();
        List<RequestFilter> all = Stream.concat(in.getDelete().stream(), in.getUpdate().stream()).collect(Collectors.toList()); 
        for (RequestFilter requestFilter : all) {
            if (requestFilter == null) {
                continue;
            }
            try {
                DBItemInventoryConfiguration conf = JocInventory.getConfiguration(dbLayer, requestFilter, folderPermissions);
                if (conf != null) {
                    if (ConfigurationType.SCHEDULE.equals(conf.getTypeAsEnum())) {
                        // only add if planOrderAutomatically = true
                        Schedule schedule = Globals.objectMapper.readValue(conf.getContent(), Schedule.class);
                        if (schedule.getPlanOrderAutomatically()) {
                            if (conf.getContent().contains("workflowNames")) {
                                List<String> workflowNames = JocInventory.getWorkflowNamesFromScheduleJson(conf.getContent());
                                if (schedulePathsWithWorkflowNames.containsKey(conf.getPath())) {
                                    schedulePathsWithWorkflowNames.get(conf.getPath()).addAll(workflowNames);
                                } else {
                                    schedulePathsWithWorkflowNames.put(conf.getPath(), workflowNames);
                                }
                            }
                        }
                    } else if (ConfigurationType.WORKINGDAYSCALENDAR.equals(conf.getTypeAsEnum()) 
                            || ConfigurationType.NONWORKINGDAYSCALENDAR.equals(conf.getTypeAsEnum())) {
                        // only add if planOrderAutomatically = true
                        List<DBItemInventoryConfiguration> schedules = dbLayer.getUsedSchedulesByCalendarName(conf.getName());
                        if (schedules != null) {
                            for (DBItemInventoryConfiguration scheduleDbItem : schedules) {
                                Schedule schedule = Globals.objectMapper.readValue(scheduleDbItem.getContent(), Schedule.class);
                                if (schedule.getPlanOrderAutomatically()) {
                                    List<String> workflowNames = JocInventory.getWorkflowNamesFromScheduleJson(scheduleDbItem.getContent());
                                    if (schedulePathsWithWorkflowNames.containsKey(scheduleDbItem.getPath())) {
                                        schedulePathsWithWorkflowNames.get(scheduleDbItem.getPath()).addAll(workflowNames);
                                    } else {
                                        schedulePathsWithWorkflowNames.put(scheduleDbItem.getPath(), workflowNames);
                                    }
                                }
                            }
                        }
                    } 
                }
            } catch (Exception ex) {
                // ignore missing objects as it should not break the general release process
            }
        }
        return schedulePathsWithWorkflowNames;
    }
    
    private void cancelAndRecreateOrders (ReleaseFilter filter, Map<String, List<String>> schedulePathsWithWorkflowNames, String xAccessToken) {
        if(filter.getAddOrdersDateFrom() != null && !filter.getAddOrdersDateFrom().isEmpty()) {
            DailyPlanCancelOrderImpl cancelOrderImpl = new DailyPlanCancelOrderImpl();
            DailyPlanDeleteOrdersImpl deleteOrdersImpl = new DailyPlanDeleteOrdersImpl();
            DailyPlanOrdersGenerateImpl ordersGenerate = new DailyPlanOrdersGenerateImpl();
            DailyPlanOrderFilterDef orderFilter = new DailyPlanOrderFilterDef();
            if("now".equals(filter.getAddOrdersDateFrom().toLowerCase())) {
                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
                orderFilter.setDailyPlanDateFrom(sdf.format(Date.from(Instant.now())));
            } else {
                orderFilter.setDailyPlanDateFrom(filter.getAddOrdersDateFrom());
            }
            orderFilter.setSchedulePaths(new ArrayList<String>(schedulePathsWithWorkflowNames.keySet()));
            if(filter.getIncludeLate()) {
                orderFilter.setLate(true);
                orderFilter.setStates(getOrderStatesForFilter());
            }
            if(orderFilter.getSchedulePaths() != null && !orderFilter.getSchedulePaths().isEmpty()) {
                try {
                    Map<String, List<DBItemDailyPlanOrder>> ordersPerController = 
                            cancelOrderImpl.getSubmittedOrderIdsFromDailyplanDate(orderFilter, xAccessToken, false, false);
                    Map<String, CompletableFuture<Either<Problem, Void>>> cancelOrderResponsePerController = 
                            cancelOrderImpl.cancelOrders(ordersPerController, xAccessToken, null, false, false);
                    for (String controllerId : Proxies.getControllerDbInstances().keySet()) {
                        if(!cancelOrderResponsePerController.containsKey(controllerId)) {
                            cancelOrderResponsePerController.put(controllerId, CompletableFuture.supplyAsync(() -> Either.right(null)));
                        }
                        cancelOrderResponsePerController.get(controllerId).thenAccept(
                                either -> {
                            if(either.isRight()) {
                                DailyPlanOrderFilterDef localOrderFilter = new DailyPlanOrderFilterDef();
                                localOrderFilter.setControllerIds(Collections.singletonList(controllerId));
                                localOrderFilter.setDailyPlanDateFrom(orderFilter.getDailyPlanDateFrom());
                                localOrderFilter.setSchedulePaths(orderFilter.getSchedulePaths());
                                SOSHibernateSession session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
                                try {
                                    session.setAutoCommit(false);
                                    boolean successful = deleteOrdersImpl.deleteOrders(localOrderFilter, xAccessToken, false, false);
                                    if (!successful) {
                                        JocError je = getJocError();
                                        if (je != null && je.printMetaInfo() != null) {
                                            LOGGER.warn(je.printMetaInfo());
                                        }
                                        LOGGER.warn("Order delete failed due to missing permission.");
                                    }
                                    Function<DBItemInventoryConfiguration, Boolean> grouper = dbSchedule -> {
                                        try {
                                            Schedule schedule = Globals.objectMapper.readValue(dbSchedule.getContent(), Schedule.class);
                                            return schedule.getSubmitOrderToControllerWhenPlanned();
                                        } catch (JsonProcessingException e) {
                                            return null;
                                        }
                                    };
                                    InventoryDBLayer dbLayerForCompleteableFuture = new InventoryDBLayer(session);
                                    List<String> schedulePaths = Collections.emptyList();
                                    if(ordersPerController.isEmpty()) {
                                        schedulePaths = new ArrayList<String>(schedulePathsWithWorkflowNames.keySet());
                                    } else {
                                        schedulePaths = ordersPerController.get(controllerId).stream().map(order -> order.getSchedulePath())
                                                .collect(Collectors.toList());
                                    }
                                    // get all schedules from database
                                    List<DBItemInventoryConfiguration> allSchedules = 
                                            dbLayerForCompleteableFuture.getConfigurationByNames(
                                                    schedulePaths.stream().map(schedulePath -> Paths.get(schedulePath).getFileName().toString())
                                                    .collect(Collectors.toList()), ConfigurationType.SCHEDULE.intValue());
                                    // map all schedule path for submitWhenPlanned = true and submitWhenPlanned = false
                                    Map<Boolean, List<String>> schedules = 
                                            allSchedules.stream().collect(Collectors.groupingBy(grouper, 
                                                    Collectors.mapping(DBItemInventoryConfiguration::getPath, Collectors.toList())));
                                    // create all GenerateRequest 
                                    List<String> allowedDailyPlanDates = ordersGenerate.getAllowedDailyPlanDates(session, controllerId);
                                    List<GenerateRequest> requests = schedules.entrySet().stream().filter(entry -> entry.getKey() != null)
                                        .map(entry -> {
                                            try {
                                                return ordersGenerate.getGenerateRequestsForReleaseDeploy(filter.getAddOrdersDateFrom(), null, 
                                                                entry.getValue(), controllerId, entry.getKey(), allowedDailyPlanDates);
                                            } catch(Exception ex) {
                                                return null;
                                            }
                                        }).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
                                    if (!requests.isEmpty()) {
                                        successful = ordersGenerate.generateOrders(requests, xAccessToken, false, filter.getIncludeLate());
                                    }
                                    if (!successful) {
                                        LOGGER.warn("generate orders failed due to missing permission.");
                                    }
                                } catch (SOSHibernateException | ParseException | DBMissingDataException | DBConnectionRefusedException 
                                        | DBInvalidDataException | JocConfigurationException | DBOpenSessionException 
                                        | ControllerConnectionResetException | ControllerConnectionRefusedException | SOSInvalidDataException 
                                        | SOSMissingDataException | IOException | ExecutionException e) {
                                    LOGGER.warn("generation of new  orders failed.", e.getMessage());
                                } finally {
                                    Globals.disconnect(session);
                                }
                            } else {
                              LOGGER.warn(either.getLeft().messageWithCause());
                            }
                        });
                    }
                    
                } catch (Exception e) {
                    LOGGER.warn(e.getMessage());
                }
            }
        }
    }
    
    private void cancelOrdersForRenamedSchedules (String addOrdersDateFrom, Map<String, List<String>> schedulePathsWithWorkflowNames,
            InventoryDBLayer dbLayer, String xAccessToken) {
        if(addOrdersDateFrom != null && !addOrdersDateFrom.isEmpty()) {
            DailyPlanCancelOrderImpl cancelOrderImpl = new DailyPlanCancelOrderImpl();
            DailyPlanDeleteOrdersImpl deleteOrdersImpl = new DailyPlanDeleteOrdersImpl();
            DailyPlanOrderFilterDef orderFilter = new DailyPlanOrderFilterDef();
            if("now".equals(addOrdersDateFrom.toLowerCase())) {
                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
                orderFilter.setDailyPlanDateFrom(sdf.format(Date.from(Instant.now())));
            } else {
                orderFilter.setDailyPlanDateFrom(addOrdersDateFrom);
            }
            orderFilter.setSchedulePaths(new ArrayList<String>(schedulePathsWithWorkflowNames.keySet()));
            if(orderFilter.getSchedulePaths() != null && !orderFilter.getSchedulePaths().isEmpty()) {
                try {
                    Map<String, List<DBItemDailyPlanOrder>> ordersPerController = 
                            cancelOrderImpl.getSubmittedOrderIdsFromDailyplanDate(orderFilter, xAccessToken, false, false);
                    Map<String, CompletableFuture<Either<Problem, Void>>> cancelOrderResponsePerController = 
                            cancelOrderImpl.cancelOrders(ordersPerController, xAccessToken, null, false, false);
                    for (String controllerId : Proxies.getControllerDbInstances().keySet()) {
                        cancelOrderResponsePerController.getOrDefault(controllerId, CompletableFuture.supplyAsync(() -> Either.right(null))).thenAccept(
                                either -> {
                            if(either.isRight()) {
                                DailyPlanOrderFilterDef localOrderFilter = new DailyPlanOrderFilterDef();
                                localOrderFilter.setControllerIds(Collections.singletonList(controllerId));
                                localOrderFilter.setDailyPlanDateFrom(orderFilter.getDailyPlanDateFrom());
                                localOrderFilter.setSchedulePaths(orderFilter.getSchedulePaths());
                                try {
                                    boolean successful = deleteOrdersImpl.deleteOrders(localOrderFilter, xAccessToken, false, false, false);
                                    if (!successful) {
                                        JocError je = getJocError();
                                        if (je != null && je.printMetaInfo() != null) {
                                            LOGGER.info(je.printMetaInfo());
                                        }
                                        LOGGER.warn("Order delete failed due to missing permission.");
                                    }
                                } catch (SOSHibernateException e) {
                                    getJocErrorWithPrintMetaInfoAndClear(LOGGER);
                                    LOGGER.warn("Order delete failed due to: ", e.getMessage());
                                }
                            } else {
                                JocError je = getJocError();
                                if (je != null && je.printMetaInfo() != null) {
                                    LOGGER.info(je.printMetaInfo());
                                }
                                LOGGER.warn(either.getLeft().messageWithCause());
                            }
                        });
                    }
                    
                } catch (Exception e) {
                    LOGGER.warn(e.getMessage());
                }
            }
        }
    }
    
    private static List<DailyPlanOrderStateText> getOrderStatesForFilter() {
        List<DailyPlanOrderStateText> states = new ArrayList<DailyPlanOrderStateText>();
        states.add(DailyPlanOrderStateText.PLANNED);
        states.add(DailyPlanOrderStateText.SUBMITTED);
        return states;
    }
}
