package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.InventoryAudit;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.inventory.resource.IDeleteConfigurationResource;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.RequestFilter;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.impl.DeleteDeployments;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class DeleteConfigurationResourceImpl extends JOCResourceImpl implements IDeleteConfigurationResource {

    @Override
    public JOCDefaultResponse delete(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH_DELETE, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);

            JOCDefaultResponse response = initPermissions(null, getPermissonsJocCockpit("", accessToken).getInventory().getConfigurations().isEdit());
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
    public JOCDefaultResponse undelete(final String accessToken, final byte[] inBytes) {
        throw new JocNotImplementedException();
        //return undelete(IMPL_PATH_UNDELETE, accessToken, inBytes);
    }

    private JOCDefaultResponse delete(String accessToken, RequestFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_DELETE);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();

            DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, in, folderPermissions);
            final ConfigurationType type = config.getTypeAsEnum();
            if (ConfigurationType.FOLDER.equals(type)) {
                ReleaseResourceImpl.delete(config, dbLayer, getJocAuditLog(), true);
                // TODO delete deployments
                //List<DBItemInventoryConfiguration> deployables = dbLayer.getFolderContent(config.getPath(), true, JocInventory.getDeployableTypes());
                
            } else {
                if (JocInventory.isReleasable(type)) {
                    ReleaseResourceImpl.delete(config, dbLayer, getJocAuditLog(), false);
                } else if (JocInventory.isDeployable(type)) { // TODO delete deployments
                    List<InventoryDeploymentItem> deployments = dbLayer.getDeploymentHistory(config.getId());
                    if (deployments == null || deployments.isEmpty()) {
                        JocInventory.deleteInventoryConfigurationAndPutToTrash(config, dbLayer);
                        JocInventory.postEvent(config.getFolder());
                    } else {
                        
                        // controllerids filter by existing controllerids
                        Map<String, Optional<InventoryDeploymentItem>> deploymentsMap = deployments.stream().filter(item -> OperationType.UPDATE
                                .equals(item.getOperation())).collect(Collectors.groupingBy(InventoryDeploymentItem::getControllerId, Collectors
                                        .maxBy(Comparator.comparingLong(InventoryDeploymentItem::getId))));

                        List<Configuration> configs = deploymentsMap.values().stream().filter(Optional::isPresent).map(Optional::get).map(item -> {
                            Configuration conf = new Configuration();
                            conf.setPath(item.getPath());
                            conf.setObjectType(type);
                            conf.setCommitId(item.getCommitId());
                            return conf;
                        }).collect(Collectors.toList());
                        JocSecurityLevel secLevel = Globals.getJocSecurityLevel();
                        String account = JocSecurityLevel.LOW.equals(secLevel) ? Globals.getDefaultProfileUserAccount() : getAccount();
                        DeleteDeployments.delete(configs, deploymentsMap.keySet(), new DBLayerDeploy(session), account, accessToken, getJocError(),
                                secLevel, true);
                    }
                }
            }
            Globals.commit(session);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private void storeAuditLog(ConfigurationType objectType, String path, String folder) {
        InventoryAudit audit = new InventoryAudit(objectType, path, folder);
        logAuditMessage(audit);
        storeAuditLogEntry(audit);
    }

}
