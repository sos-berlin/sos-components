package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.fileordersource.FileOrderSource;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.common.SyncStateHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JsonSerializer;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryConfigurationTrash;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IReadConfigurationResource;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ItemStateEnum;
import com.sos.joc.model.inventory.read.RequestFilter;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.data_for_java.controller.JControllerState;

@Path(JocInventory.APPLICATION_PATH)
public class ReadConfigurationResourceImpl extends JOCResourceImpl implements IReadConfigurationResource {

    @Override
    public JOCDefaultResponse read(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());
            if (response == null) {
                response = read(in);
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
    public JOCDefaultResponse readTrash(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(TRASH_IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());
            if (response == null) {
                response = readTrash(in);
            }
            return response;

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private JOCDefaultResponse read(RequestFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
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
            
            if (in.getCommitId() != null && !in.getCommitId().isEmpty() && JocInventory.isDeployable(type)) {
                String invContentFromDepHistory = dbLayer.getDeployedInventoryContent(config.getId(), in.getCommitId());
                if (invContentFromDepHistory != null) {
                    config.setContent(invContentFromDepHistory);
                } else {
                    throw new DBMissingDataException(String.format("deployed configuration not found: %s:%s (%s)", type.value().toLowerCase(), config
                            .getPath(), in.getCommitId()));
                }
            }
            
            if (config.getType().equals(ConfigurationType.FILEORDERSOURCE.intValue())) {
                // temp. for compatibility directory -> directoryExpr
                FileOrderSource fos = (FileOrderSource) JocInventory.content2IJSObject(config.getContent(), config.getType());
                if (fos.getDirectoryExpr() == null) {
                    fos.setDirectoryExpr(JsonSerializer.quoteString(fos.getDirectory())); 
                }
                item.setConfiguration(fos);
            } else {
                if (in.getWithPositions() == Boolean.TRUE && config.getType().equals(ConfigurationType.WORKFLOW.intValue())) {
                    item.setConfiguration(WorkflowsHelper.addWorkflowPositions((Workflow) JocInventory.content2IJSObject(config.getContent(), config
                            .getType())));
                } else {
                    item.setConfiguration(JocInventory.content2IJSObject(config.getContent(), config.getType()));
                }
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

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(item));
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private JOCDefaultResponse readTrash(RequestFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(TRASH_IMPL_PATH);
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
            item.setConfiguration(JocInventory.content2IJSObject(config.getContent(), config.getType()));
            
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(item));
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

}
