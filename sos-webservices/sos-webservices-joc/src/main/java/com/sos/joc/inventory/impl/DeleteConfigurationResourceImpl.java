package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryConfigurationTrash;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IDeleteConfigurationResource;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.RequestFilter;
import com.sos.joc.model.inventory.common.RequestFilters;
import com.sos.joc.model.inventory.common.RequestFolder;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.util.DeleteDeployments;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class DeleteConfigurationResourceImpl extends JOCResourceImpl implements IDeleteConfigurationResource {

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
            checkRequiredComment(in.getAuditLog());
            
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
                    ReleaseResourceImpl.delete(config, dbLayer, getJocAuditLog(), in.getAuditLog(), false, false);
                    foldersForEvent.add(config.getFolder());

                } else if (JocInventory.isDeployable(type)) {
                    List<DBItemDeploymentHistory> allDeploymentsPerObject = deployDbLayer.getDeployedConfigurations(config.getId());
                    Set<DBItemDeploymentHistory> deployments = null;
                    if (allDeploymentsPerObject != null) {
                        deployments = allDeploymentsPerObject.stream().filter(d -> OperationType.UPDATE.value() == d.getOperation()).collect(
                                Collectors.groupingBy(DBItemDeploymentHistory::getControllerId, Collectors.maxBy(Comparator.comparingLong(
                                        DBItemDeploymentHistory::getId)))).values().stream().filter(Optional::isPresent).map(Optional::get).collect(
                                                Collectors.toSet());
                    }
                    if (deployments == null || deployments.isEmpty()) {
                        JocInventory.deleteInventoryConfigurationAndPutToTrash(config, dbLayer);
                        foldersForEvent.add(config.getFolder());
                    } else {
                        allDeployments.addAll(deployments);
                    }
                }
            }
            if (allDeployments != null && !allDeployments.isEmpty()) {
                String account = JocSecurityLevel.LOW.equals(Globals.getJocSecurityLevel()) ? ClusterSettings.getDefaultProfileAccount(Globals
                        .getConfigurationGlobalsJoc()) : getAccount();
                DeleteDeployments.delete(allDeployments, deployDbLayer, account, accessToken, getJocError(), getJocAuditLog(), in.getAuditLog(),
                        true);
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
            checkRequiredComment(in.getAuditLog());
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_FOLDER_DELETE);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();

            DBItemInventoryConfiguration folder = JocInventory.getConfiguration(dbLayer, null, in.getPath(), ConfigurationType.FOLDER,
                    folderPermissions);
            ReleaseResourceImpl.delete(folder, dbLayer, getJocAuditLog(), in.getAuditLog(), false, false);

            List<DBItemInventoryConfiguration> deployables = dbLayer.getFolderContent(folder.getPath(), true, JocInventory.getDeployableTypes());
            if (deployables != null && !deployables.isEmpty()) {
                String account = JocSecurityLevel.LOW.equals(Globals.getJocSecurityLevel()) ? ClusterSettings.getDefaultProfileAccount(Globals
                        .getConfigurationGlobalsJoc()) : getAccount();
                DeleteDeployments.deleteFolder(folder.getPath(), true, Proxies.getControllerDbInstances().keySet(), new DBLayerDeploy(session),
                        account, accessToken, getJocError(), getJocAuditLog(), in.getAuditLog(), true, false);
            }
            
            JocInventory.deleteEmptyFolders(dbLayer, folder.getPath());
            
            Globals.commit(session);
            JocInventory.postEvent(folder.getFolder());
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
            checkRequiredComment(in.getAuditLog());
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_TRASH_DELETE);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();
            
            // TODO auditLog
            
            Predicate<RequestFilter> isFolder = r -> ConfigurationType.FOLDER.equals(r.getObjectType());
            if (in.getObjects().stream().parallel().anyMatch(isFolder)) {
                //throw new 
            }
            Set<String> foldersForEvent = new HashSet<>();
            for (RequestFilter r : in.getObjects().stream().filter(isFolder.negate()).collect(Collectors.toSet())) {
                DBItemInventoryConfigurationTrash config = JocInventory.getTrashConfiguration(dbLayer, r, folderPermissions);
                session.delete(config);
                foldersForEvent.add(config.getFolder());
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
            checkRequiredComment(in.getAuditLog());
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_TRASH_FOLDER_DELETE);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();

            // TODO auditLog

            DBItemInventoryConfigurationTrash config = JocInventory.getTrashConfiguration(dbLayer, null, in.getPath(), ConfigurationType.FOLDER,
                    folderPermissions);
            dbLayer.deleteTrashFolder(config.getPath());
            Globals.commit(session);
            JocInventory.postTrashEvent(config.getFolder());

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

}
