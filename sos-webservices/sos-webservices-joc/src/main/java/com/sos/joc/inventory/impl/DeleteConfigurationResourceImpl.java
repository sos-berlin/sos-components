package com.sos.joc.inventory.impl;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.dailyplan.impl.DailyPlanCancelOrderImpl;
import com.sos.joc.dailyplan.impl.DailyPlanDeleteOrdersImpl;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryConfigurationTrash;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.inventory.resource.IDeleteConfigurationResource;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilterDef;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.RequestFilter;
import com.sos.joc.model.inventory.delete.RequestFilters;
import com.sos.joc.model.inventory.delete.RequestFolder;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.util.DeleteDeployments;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.base.problem.Problem;

@Path(JocInventory.APPLICATION_PATH)
public class DeleteConfigurationResourceImpl extends JOCResourceImpl implements IDeleteConfigurationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteConfigurationResourceImpl.class);
    
    @Override
    public JOCDefaultResponse remove(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH_DELETE, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilters.class);
            RequestFilters in = Globals.objectMapper.readValue(inBytes, RequestFilters.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                response = remove(accessToken, in);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    @Override
    public JOCDefaultResponse removeFolder(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH_FOLDER_DELETE, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFolder.class);
            RequestFolder in = Globals.objectMapper.readValue(inBytes, RequestFolder.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                response = removeFolder(accessToken, in);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse delete(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH_TRASH_DELETE, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilters.class);
            RequestFilters in = Globals.objectMapper.readValue(inBytes, RequestFilters.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                response = delete(accessToken, in);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    @Override
    public JOCDefaultResponse deleteFolder(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH_TRASH_DELETE, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFolder.class);
            RequestFolder in = Globals.objectMapper.readValue(inBytes, RequestFolder.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                response = deleteFolder(accessToken, in);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private JOCDefaultResponse remove(String accessToken, RequestFilters in) throws Exception {
        SOSHibernateSession session = null;
        try {
            DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_DELETE);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();
            
            Predicate<RequestFilter> isFolder = r -> ConfigurationType.FOLDER.equals(r.getObjectType());
            if (in.getObjects().stream().parallel().anyMatch(isFolder)) {
                //throw new 
            }
            Set<DBItemDeploymentHistory> allDeployments = new HashSet<>();
            DBLayerDeploy deployDbLayer = new DBLayerDeploy(session);
            Set<String> foldersForEvent = new HashSet<>();
            for (RequestFilter r : in.getObjects().stream().filter(isFolder.negate()).collect(Collectors.toSet())) {
                DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, r, folderPermissions);
                final ConfigurationType type = config.getTypeAsEnum();

                if (JocInventory.isReleasable(type)) {
                    cancelOrders(accessToken, config, in.getCancelOrdersDateFrom(), dbLayer);
                    ReleaseResourceImpl.delete(config, dbLayer, dbAuditLog, false, false);
                    foldersForEvent.add(config.getFolder());
                    JocAuditLog.storeAuditLogDetail(new AuditLogDetail(config.getPath(), config.getType()), dbLayer.getSession(), dbAuditLog);
                    
                } else if (JocInventory.isDeployable(type)) {
                    // TODO restrict to allowed Controllers
                    List<DBItemDeploymentHistory> allDeploymentsPerObject = deployDbLayer.getDeployedConfigurations(config.getName(), config.getType());
                    Set<DBItemDeploymentHistory> deployments = null;
                    if (allDeploymentsPerObject != null) {
                        deployments = allDeploymentsPerObject.stream().filter(d -> OperationType.UPDATE.value() == d.getOperation()).collect(
                                Collectors.groupingBy(DBItemDeploymentHistory::getControllerId, Collectors.maxBy(Comparator.comparingLong(
                                        DBItemDeploymentHistory::getId)))).values().stream().filter(Optional::isPresent).map(Optional::get).collect(
                                                Collectors.toSet());
                    }
                    if (deployments == null || deployments.isEmpty()) {
                        JocInventory.deleteInventoryConfigurationAndPutToTrash(config, dbLayer);
                        JocAuditLog.storeAuditLogDetail(new AuditLogDetail(config.getPath(), config.getType()), dbLayer.getSession(), dbAuditLog);
                        foldersForEvent.add(config.getFolder());
                    } else {
                        allDeployments.addAll(deployments);
                    }
                }
            }
            if (allDeployments != null && !allDeployments.isEmpty()) {
                String account = JocSecurityLevel.LOW.equals(Globals.getJocSecurityLevel()) ? ClusterSettings.getDefaultProfileAccount(Globals
                        .getConfigurationGlobalsJoc()) : getAccount();
                DeleteDeployments.delete(allDeployments, deployDbLayer, account, accessToken, getJocError(), dbAuditLog.getId(), true, in.getCancelOrdersDateFrom());
            }
            Globals.commit(session);
            // post events
            for (String folder: foldersForEvent) {
                JocInventory.postEvent(folder);
                JocInventory.postTrashEvent(folder);
            }
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private JOCDefaultResponse removeFolder(String accessToken, RequestFolder in) throws Exception {
        SOSHibernateSession session = null;
        try {
            DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_FOLDER_DELETE);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();

            DBItemInventoryConfiguration folder = JocInventory.getConfiguration(dbLayer, null, in.getPath(), ConfigurationType.FOLDER,
                    folderPermissions);
            cancelOrders(accessToken, folder, in.getCancelOrdersDateFrom(),dbLayer);
            ReleaseResourceImpl.delete(folder, dbLayer, dbAuditLog, false, false);

            // TODO restrict to allowed Controllers
            List<DBItemInventoryConfiguration> deployables = dbLayer.getFolderContent(folder.getPath(), true, JocInventory.getDeployableTypes());
            if (deployables != null && !deployables.isEmpty()) {
                String account = JocSecurityLevel.LOW.equals(Globals.getJocSecurityLevel()) ? ClusterSettings.getDefaultProfileAccount(Globals
                        .getConfigurationGlobalsJoc()) : getAccount();
                DeleteDeployments.deleteFolder(IMPL_PATH_FOLDER_DELETE, folder.getPath(), true, Proxies.getControllerDbInstances().keySet(),
                        new DBLayerDeploy(session), account, accessToken, getJocError(), dbAuditLog.getId(), true, false, in.getCancelOrdersDateFrom());
            }
            
            JocInventory.deleteEmptyFolders(dbLayer, folder.getPath());
            
            Globals.commit(session);
            JocInventory.postFolderEvent(folder.getFolder());
            JocInventory.postTrashFolderEvent(folder.getFolder());
            JocInventory.postTrashEvent(folder.getPath());
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private JOCDefaultResponse delete(String accessToken, RequestFilters in) throws Exception {
        SOSHibernateSession session = null;
        try {
            DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_TRASH_DELETE);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();
            
            Predicate<RequestFilter> isFolder = r -> ConfigurationType.FOLDER.equals(r.getObjectType());
            if (in.getObjects().stream().parallel().anyMatch(isFolder)) {
                //throw new 
            }
            Set<String> foldersForEvent = new HashSet<>();
            for (RequestFilter r : in.getObjects().stream().filter(isFolder.negate()).collect(Collectors.toSet())) {
                DBItemInventoryConfigurationTrash config = JocInventory.getTrashConfiguration(dbLayer, r, folderPermissions);
                session.delete(config);
                foldersForEvent.add(config.getFolder());
                JocAuditLog.storeAuditLogDetail(new AuditLogDetail(config.getPath(), config.getType()), session, dbAuditLog);
            }
            
            Globals.commit(session);
            // post events
            for (String folder: foldersForEvent) {
                JocInventory.postTrashEvent(folder);
            }
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private JOCDefaultResponse deleteFolder(String accessToken, RequestFolder in) throws Exception {
        SOSHibernateSession session = null;
        try {
            //DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());
            JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_TRASH_FOLDER_DELETE);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();

            // TODO auditLogDetails

            DBItemInventoryConfigurationTrash config = JocInventory.getTrashConfiguration(dbLayer, null, in.getPath(), ConfigurationType.FOLDER,
                    folderPermissions);
            dbLayer.deleteTrashFolder(config.getPath());
            Globals.commit(session);
            JocInventory.postTrashEvent(config.getFolder());
            JocInventory.postTrashFolderEvent(config.getPath());

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private void cancelOrders(String xAccessToken, DBItemInventoryConfiguration config, String cancelOrdersDate, InventoryDBLayer dbLayer)
            throws SOSHibernateException {
        if(cancelOrdersDate != null) {
            Set<String> allowedControllerIds = Proxies.getControllerDbInstances().keySet().stream()
                    .filter(availableController -> getControllerPermissions(availableController, xAccessToken).getOrders().getCancel())
                    .collect(Collectors.toSet());
            if(ConfigurationType.FOLDER.equals(config.getTypeAsEnum())) {
                Set<Integer> confTypes = new HashSet<Integer>();
                confTypes.add(ConfigurationType.SCHEDULE.intValue());
                confTypes.add(ConfigurationType.WORKINGDAYSCALENDAR.intValue());
                confTypes.add(ConfigurationType.NONWORKINGDAYSCALENDAR.intValue());
                List<DBItemInventoryConfiguration> folderContent = 
                        dbLayer.getFolderContent(config.getPath(), true, confTypes);
                if(!folderContent.isEmpty()) {
                    folderContent.stream().forEach(configuration -> 
                        cancelOrders(xAccessToken, configuration, cancelOrdersDate, allowedControllerIds, dbLayer));
                }
            } else if (ConfigurationType.SCHEDULE.equals(config.getTypeAsEnum())
                    || ConfigurationType.WORKINGDAYSCALENDAR.equals(config.getTypeAsEnum()) 
                    || ConfigurationType.NONWORKINGDAYSCALENDAR.equals(config.getTypeAsEnum())) {
                cancelOrders(xAccessToken, config, cancelOrdersDate, allowedControllerIds, dbLayer);
            }
        }
    }

    private void cancelOrders(String xAccessToken, DBItemInventoryConfiguration config, String cancelOrdersDate, Set<String> controllerIds,
            InventoryDBLayer dbLayer) {
        Set<String> schedulePaths = new HashSet<String>();
        if(ConfigurationType.SCHEDULE.equals(config.getTypeAsEnum())) {
            schedulePaths.add(config.getPath());
        } else if (ConfigurationType.WORKINGDAYSCALENDAR.equals(config.getTypeAsEnum()) 
                || ConfigurationType.NONWORKINGDAYSCALENDAR.equals(config.getTypeAsEnum())) {
            try {
                List<DBItemInventoryConfiguration> schedules = dbLayer.getUsedSchedulesByCalendarName(config.getName());
                if(schedules != null) {
                    schedulePaths = schedules.stream().map(DBItemInventoryConfiguration::getPath).collect(Collectors.toSet());
                    for (DBItemInventoryConfiguration schedule : schedules) {
                        schedule.setReleased(false);
                        schedule.setValid(false);
                        try {
                            dbLayer.getSession().update(schedule);
                        } catch (SOSHibernateException e) {
                            throw new JocSosHibernateException(e);
                        }
                        DBItemInventoryReleasedConfiguration releasedSchedule = dbLayer.getReleasedItemByConfigurationId(schedule.getId());
                        try {
                            Schedule scheduleObject = Globals.objectMapper.readValue(releasedSchedule.getContent(), Schedule.class);
                            scheduleObject.setPlanOrderAutomatically(false);
                            scheduleObject.setSubmitOrderToControllerWhenPlanned(false);
                            releasedSchedule.setContent(Globals.objectMapper.writeValueAsString(scheduleObject));
                            dbLayer.getSession().update(releasedSchedule);
                        } catch (Exception e) {
                            LOGGER.warn(e.getMessage());
                        } 
                    }
                }
            } catch (SOSHibernateException e) {
                LOGGER.warn(e.getMessage());
            }
        }
        if(!schedulePaths.isEmpty()) {
            DailyPlanCancelOrderImpl cancelOrderImpl = new DailyPlanCancelOrderImpl();
            DailyPlanDeleteOrdersImpl deleteOrdersImpl = new DailyPlanDeleteOrdersImpl();
            DailyPlanOrderFilterDef orderFilter = new DailyPlanOrderFilterDef();
            if("now".equals(cancelOrdersDate.toLowerCase())) {
                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
                orderFilter.setDailyPlanDateFrom(sdf.format(Date.from(Instant.now())));
            } else {
                orderFilter.setDailyPlanDateFrom(cancelOrdersDate);
            }
            orderFilter.setSchedulePaths(new ArrayList<String>(schedulePaths));
            orderFilter.setControllerIds(new ArrayList<String>(controllerIds));
            try {
                Map<String, List<DBItemDailyPlanOrder>> ordersPerController = 
                        cancelOrderImpl.getSubmittedOrderIdsFromDailyplanDate(orderFilter, xAccessToken, false, false);
                Map<String, CompletableFuture<Either<Problem, Void>>> cancelOrderResponsePerController = 
                        cancelOrderImpl.cancelOrders(ordersPerController, xAccessToken, null, false, false);
                if(cancelOrderResponsePerController.isEmpty()) {
                    // No orders to cancel on the controller side
                    // Add CompletableFuture per controller to go on processing to delete planned orders
                    controllerIds.stream().forEach(controllerId -> 
                        cancelOrderResponsePerController.put(controllerId, CompletableFuture.supplyAsync(() -> Either.right(null))));
                }
                for (String controllerId : cancelOrderResponsePerController.keySet()) {
                    cancelOrderResponsePerController.get(controllerId).thenAccept(either -> {
                        if(either.isRight()) {
                            try {
                                boolean successful = deleteOrdersImpl.deleteOrders(orderFilter, xAccessToken, false, false);
                                if (!successful) {
                                    JocError je = getJocError();
                                    if (je != null && je.printMetaInfo() != null) {
                                        LOGGER.info(je.printMetaInfo());
                                    }
                                    LOGGER.warn("Order delete failed due to missing permission.");
                                }
                            } catch (SOSHibernateException e) {
                                LOGGER.warn("Order delete failed due to: ", e.getMessage());
                            }
                        } else {
                            JocError je = getJocError();
                            if (je != null && je.printMetaInfo() != null) {
                                LOGGER.info(je.printMetaInfo());
                            }
                            LOGGER.warn("Order cancel failed due to missing permission.");
                        }
                    });
                }
            } catch (Exception e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }

}
