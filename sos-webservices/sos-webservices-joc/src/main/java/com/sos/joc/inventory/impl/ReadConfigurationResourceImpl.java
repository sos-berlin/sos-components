package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IReadConfigurationResource;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ItemStateEnum;
import com.sos.joc.model.inventory.common.RequestFilter;
import com.sos.joc.model.inventory.deploy.ResponseDeployableVersion;
import com.sos.joc.model.publish.OperationType;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class ReadConfigurationResourceImpl extends JOCResourceImpl implements IReadConfigurationResource {

    @Override
    public JOCDefaultResponse read(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);

            JOCDefaultResponse response = initPermissions(null, getPermissonsJocCockpit("", accessToken).getInventory().getConfigurations().isEdit());
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
            item.setConfiguration(null);
            item.setDeployed(config.getDeployed());
            item.setReleased(config.getReleased());
            item.setConfiguration(JocInventory.content2IJSObject(config.getContent(), config.getType()));
            
            if (JocInventory.isDeployable(type)) {
                
                item.setReleased(false);
                
                List<InventoryDeploymentItem> deployments = dbLayer.getDeploymentHistory(config.getId());
                InventoryDeploymentItem lastDeployment = null;
                
                if (deployments != null && !deployments.isEmpty()) {
                    Collections.sort(deployments, Comparator.comparing(InventoryDeploymentItem::getDeploymentDate).reversed());
                    lastDeployment = deployments.get(0);
                }
                
                if (config.getDeployed()) {
//                    if (lastDeployment == null) {
//                        throw new DBMissingDataException(String.format("[%s][%s] deployments not found", config.getTypeAsEnum().name(), config
//                                .getPath()));
//                    }
                    
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
                
                if (lastDeployment != null) {
                    if (item.getDeployments() == null) {
                        item.setDeployments(new HashSet<ResponseDeployableVersion>());
                    }
                    for (InventoryDeploymentItem d : deployments) {
                        ResponseDeployableVersion v = new ResponseDeployableVersion();
                        v.setId(config.getId());
                        v.setDeploymentId(d.getId());
                        v.setDeploymentOperation(OperationType.fromValue(d.getOperation()).name().toLowerCase());
                        v.setVersionDate(d.getDeploymentDate());
                        item.getDeployments().add(v);
                    }
                } else {
                    item.setDeployments(null);
                }
                
            } else if (JocInventory.isReleasable(type)) {
                
                item.setDeployed(false);
                
                List<Date> releasedModifieds = dbLayer.getReleasedConfigurationProperty(config.getId(), "modified");
                Date releasedLastModified = null;
                if (releasedModifieds != null && !releasedModifieds.isEmpty()) {
                    releasedLastModified = releasedModifieds.get(0);
                }
                
                if (config.getReleased()) {
//                    if (releasedLastModified == null) {
//                        throw new DBMissingDataException(String.format("[%s][%s] release not found", config.getTypeAsEnum().name(), config
//                                .getPath()));
//                    }

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
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

}
