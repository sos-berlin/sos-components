package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.exceptions.ControllerInvalidResponseDataException;
import com.sos.joc.exceptions.JocDeployException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.inventory.resource.IDeployableResource;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ResponseItemDeployment;
import com.sos.joc.model.inventory.deploy.DeployableFilter;
import com.sos.joc.model.inventory.deploy.ResponseDeployable;
import com.sos.joc.model.inventory.deploy.ResponseDeployableTreeItem;
import com.sos.joc.model.inventory.deploy.ResponseDeployableVersion;
import com.sos.joc.model.publish.OperationType;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class DeployableResourceImpl extends JOCResourceImpl implements IDeployableResource {

    @Override
    public JOCDefaultResponse deployable(final String accessToken, byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            inBytes = initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, DeployableFilter.class, true);
            DeployableFilter in = Globals.objectMapper.readValue(inBytes, DeployableFilter.class);

            JOCDefaultResponse response = initPermissions(null, getBasicJocPermissions(accessToken).getInventory().getView());

            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(deployable(in));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private ResponseDeployable deployable(DeployableFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            
            DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, in, folderPermissions);
            ConfigurationType type = config.getTypeAsEnum();
            
            if (ConfigurationType.FOLDER.equals(type)) {
                throw new JocNotImplementedException("use ./inventory/deployables for folders!");
            }
            if (!JocInventory.isDeployable(type)) {
                throw new ControllerInvalidResponseDataException("Object is not a 'Controller Object': " + type.value());
            }
            
            // get deleted folders
            List<String> deletedFolders = dbLayer.getDeletedFolders();
            // if inside deletedFolders -> setDeleted(true);
            Predicate<String> filter = f -> config.getPath().startsWith((f + "/").replaceAll("//+", "/"));
            if (deletedFolders != null && !deletedFolders.isEmpty() && deletedFolders.stream().parallel().anyMatch(filter)) {
                config.setDeleted(true);
            }
            
            ResponseDeployableTreeItem treeItem = getResponseDeployableTreeItem(config);
            
            if (!in.getWithoutDrafts() || !in.getWithoutDeployed()) {
                List<InventoryDeploymentItem> deployments = dbLayer.getDeploymentHistory(config.getId());
                if ((deployments == null || deployments.isEmpty()) && in.getOnlyValidObjects() && !config.getValid() && !config.getDeleted()) {
                    throw new JocDeployException(String.format("%s is neither valid nor already deployed: %s", type.value().toLowerCase(), config
                            .getPath()));
                }
                Set<ResponseDeployableVersion> versions = new LinkedHashSet<>();
                if (!treeItem.getDeployed() && config.getValid() && !in.getWithoutDrafts()) {
                    ResponseDeployableVersion draft = new ResponseDeployableVersion();
                    draft.setId(config.getId());
                    draft.setVersionDate(config.getModified());
                    draft.setVersions(null);
                    versions.add(draft);
                }
                versions.addAll(getVersions(config.getId(), deployments, in.getWithoutDeployed(), in.getLatest()));
//                if (versions.isEmpty()) {
//                    versions = null;
//                }
                treeItem.setDeployablesVersions(versions);
            } else {
                InventoryDeploymentItem depItem = dbLayer.getLastDeploymentHistory(config.getId());
                if (depItem == null && in.getOnlyValidObjects() && !config.getValid() && !config.getDeleted()) {
                    throw new JocDeployException(String.format("%s is neither valid nor already deployed: %s", type.value().toLowerCase(), config
                            .getPath()));
                }
            }

            ResponseDeployable result = new ResponseDeployable();
            result.setDeliveryDate(Date.from(Instant.now()));
            result.setDeployable(treeItem);
            return result;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    public static Set<ResponseDeployableVersion> getVersions(Long confId, Collection<InventoryDeploymentItem> deployments, boolean withoutDeployed,
            boolean onlyLatest) {
        if (deployments == null || withoutDeployed) {
            return Collections.emptySet();
        }

        Map<Date, Set<ResponseItemDeployment>> versions = deployments.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(
                InventoryDeploymentItem::getDeploymentDate, Collectors.mapping(deployment -> {
                    ResponseItemDeployment id = new ResponseItemDeployment();
                    id.setVersion(deployment.getVersion());
                    id.setControllerId(deployment.getControllerId());
                    return id;
                }, Collectors.toSet())));

        Stream<ResponseDeployableVersion> versionsStream = deployments.stream().filter(Objects::nonNull).distinct().sorted(Comparator.comparing(
                InventoryDeploymentItem::getDeploymentDate).reversed()).map(deployment -> {
                    ResponseDeployableVersion dv = new ResponseDeployableVersion();
                    dv.setId(confId);
                    dv.setCommitId(deployment.getCommitId());
                    dv.setVersions(versions.get(deployment.getDeploymentDate()));
                    dv.setVersionDate(deployment.getDeploymentDate());
                    dv.setDeploymentId(deployment.getId());
                    dv.setDeploymentOperation(OperationType.fromValue(deployment.getOperation()).name().toLowerCase());
                    dv.setDeploymentPath(deployment.getPath());
                    return dv;
                });
        
        if (onlyLatest) {
            Optional<ResponseDeployableVersion> opt = versionsStream.findFirst();
            if (opt.isPresent()) {
                return Collections.singleton(opt.get());
            } else {
                return Collections.emptySet();
            }
        } else {
            return versionsStream.collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }
    
    public static ResponseDeployableTreeItem getResponseDeployableTreeItem(DBItemInventoryConfiguration item) {
        ResponseDeployableTreeItem treeItem = new ResponseDeployableTreeItem();
        treeItem.setId(item.getId());
        treeItem.setFolder(item.getFolder());
        treeItem.setObjectName(item.getName());
        treeItem.setObjectType(JocInventory.getType(item.getType()));
        treeItem.setDeleted(item.getDeleted());
        treeItem.setDeployed(item.getDeployed());
        treeItem.setValid(item.getValid());
        treeItem.setDeployablesVersions(null);
        return treeItem;
    }

}
