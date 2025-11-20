package com.sos.joc.inventory.impl.common;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.fileordersource.FileOrderSource;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.common.SyncStateHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.WorkflowConverter;
import com.sos.joc.classes.order.OrderTags;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryConfigurationTrash;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryNotesDBLayer;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.IsReferencedBy;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ItemStateEnum;
import com.sos.joc.model.inventory.read.RequestFilter;
import com.sos.joc.model.note.common.Severity;

import io.vavr.control.Either;
import js7.data_for_java.controller.JControllerState;

public abstract class AReadConfiguration extends JOCResourceImpl {

    public JOCDefaultResponse read(RequestFilter in, String request) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(request);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            
            DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, in, folderPermissions);
            ConfigurationType type = config.getTypeAsEnum();
            
            ConfigurationObject item = new ConfigurationObject();
            item.setId(config.getId());
            item.setDeliveryDate(Date.from(Instant.now()));
            item.setPath(config.getPath());
            item.setObjectType(type);
            item.setValid(config.getValid());
            item.setDeleted(config.getDeleted());
            item.setState(ItemStateEnum.NO_CONFIGURATION_EXIST);
            item.setConfigurationDate(config.getModified());
            item.setDeployed(config.getDeployed());
            item.setReleased(config.getReleased());
            item.setDeployments(null);
            item.setHasDeployments(false);
            item.setHasReleases(false);
            item.setIsReferencedBy(null);
            item.setHasNote(Severity.fromValueOrNull(new InventoryNotesDBLayer(session).hasNote(config.getId())));
            
            if (in.getCommitId() != null && !in.getCommitId().isEmpty() && JocInventory.isDeployable(type)) {
                String invContentFromDepHistory = dbLayer.getDeployedInventoryContent(config.getId(), in.getCommitId());
                if (invContentFromDepHistory != null) {
                    config.setContent(invContentFromDepHistory);
                } else {
                    throw new DBMissingDataException(String.format("Couldn't find deployed configuration: %s:%s (%s)", type.value().toLowerCase(), config
                            .getPath(), in.getCommitId()));
                }
            }
            
            if (config.getType().equals(ConfigurationType.WORKFLOW.intValue())) {
                Workflow w = WorkflowConverter.convertInventoryWorkflow(config.getContent());
                if (in.getWithPositions() == Boolean.TRUE) {
                    w = WorkflowsHelper.addWorkflowPositions(w);
                }
                w = OrderTags.addGroupsToInstructions(w, session);
                item.setConfiguration(w);
                
            } else if (config.getType().equals(ConfigurationType.FILEORDERSOURCE.intValue())) {
                FileOrderSource fos = JocInventory.convertFileOrderSource(config.getContent(), FileOrderSource.class);
                fos = OrderTags.addGroupsToFileOrderSource(fos, session);
                item.setConfiguration(fos);
                
            } else if (config.getType().equals(ConfigurationType.SCHEDULE.intValue())) {
                Schedule schedule = JocInventory.convertSchedule(config.getContent(), Schedule.class);
                schedule = OrderTags.addGroupsToOrderPreparation(schedule, session);
                item.setConfiguration(schedule);
                
            } else {
                item.setConfiguration(JocInventory.content2IJSObject(config.getContent(), config.getType()));
            }
            
            if (JocInventory.isDeployable(type)) {
                
                if (in.getControllerId() != null && !in.getControllerId().isEmpty()) {
                    DeployedConfigurationDBLayer deployedDbLayer = new DeployedConfigurationDBLayer(session);
                    JControllerState currentstate = null;
                    try {
                        currentstate = Proxy.of(in.getControllerId()).currentState();
                    } catch (Exception e) {
                        ProblemHelper.postExceptionEventIfExist(Either.left(e), null, getJocError(), null);
                    }
                    item.setSyncState(SyncStateHelper.getState(currentstate, config.getId(), type, deployedDbLayer.getDeployedName(in
                            .getControllerId(), config.getId(), config.getType())));
                }
                
                item.setReleased(false);
                
                InventoryDeploymentItem lastDeployment = dbLayer.getLastDeploymentHistory(config.getId());
                item.setHasDeployments(lastDeployment != null);
                
                if (config.getDeployed()) {
                    if (item.getConfiguration() != null) {
                        item.setState(ItemStateEnum.DRAFT_NOT_EXIST);
                    }
                } else {

                    if (item.getConfiguration() != null) {
                        if (lastDeployment == null) {
                            item.setState(ItemStateEnum.DEPLOYMENT_NOT_EXIST);
                        } else {
                            if (lastDeployment.getDeploymentDate().after(config.getModified())) {
                                item.setState(ItemStateEnum.DEPLOYMENT_IS_NEWER);
                            } else {
                                item.setState(ItemStateEnum.DRAFT_IS_NEWER);
                            }
                        }
                    } 
                }
                
                // JOC-1498 - IsReferencedBy
                if (ConfigurationType.WORKFLOW.equals(type)) {
                    IsReferencedBy isRef = new IsReferencedBy();
                    isRef.setAdditionalProperty("fileOrderSources", dbLayer.getNumOfUsedFileOrderSourcesByWorkflowName(config.getName()).intValue());
                    isRef.setAdditionalProperty("schedules", dbLayer.getNumOfUsedSchedulesByWorkflowName(config.getName()).intValue());
                    isRef.setAdditionalProperty("workflows", dbLayer.getNumOfAddOrderWorkflowsByWorkflowName(config.getName()).intValue());
                    item.setIsReferencedBy(isRef);
                }
                
            } else if (JocInventory.isReleasable(type)) {
                
                item.setDeployed(false);
                
                List<Date> releasedModifieds = dbLayer.getReleasedItemPropertyByConfigurationId(config.getId(), "modified");
                Date releasedLastModified = null;
                if (releasedModifieds != null && !releasedModifieds.isEmpty()) {
                    releasedLastModified = releasedModifieds.get(0);
                    item.setHasReleases(true);
                }
                
                if (config.getReleased()) {
                    if (item.getConfiguration() != null) {
                        item.setState(ItemStateEnum.DRAFT_NOT_EXIST);
                    }
                    
                } else {

                    if (item.getConfiguration() != null) {
                        if (releasedLastModified == null) {
                            item.setState(ItemStateEnum.RELEASE_NOT_EXIST);
                        } else {
                            if (releasedLastModified.after(config.getModified())) {
                                item.setState(ItemStateEnum.RELEASE_IS_NEWER);
                            } else {
                                item.setState(ItemStateEnum.DRAFT_IS_NEWER);
                            }
                        }
                    }
                }
            }

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(item));
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    public JOCDefaultResponse readTrash(RequestFilter in, String request) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(request);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            
            DBItemInventoryConfigurationTrash config = JocInventory.getTrashConfiguration(dbLayer, in, folderPermissions);
            ConfigurationType type = config.getTypeAsEnum();
            
            ConfigurationObject item = new ConfigurationObject();
            item.setId(config.getId());
            item.setDeliveryDate(Date.from(Instant.now()));
            item.setPath(config.getPath());
            item.setObjectType(type);
            item.setValid(config.getValid());
            item.setState(null);
            item.setConfigurationDate(config.getModified());
            item.setDeployments(null);
            item.setHasDeployments(null);
            item.setHasReleases(null);
            item.setHasNote(null);
            item.setIsReferencedBy(null);
            item.setConfiguration(JocInventory.content2IJSObject(config.getContent(), config.getType()));
            
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(item));
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

}
