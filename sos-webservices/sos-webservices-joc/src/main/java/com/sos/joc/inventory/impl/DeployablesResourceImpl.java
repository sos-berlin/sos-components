package com.sos.joc.inventory.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryMeta.ConfigurationType;
import com.sos.joc.db.inventory.items.InventoryDeployablesTreeFolderItem;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IDeployablesResource;
import com.sos.joc.model.common.JobSchedulerObjectType;
import com.sos.joc.model.inventory.common.ResponseItemDeployment;
import com.sos.joc.model.inventory.deploy.RequestFilter;
import com.sos.joc.model.inventory.deploy.ResponseDeployableTreeItem;
import com.sos.joc.model.inventory.deploy.ResponseDeployableVersion;
import com.sos.joc.model.inventory.deploy.ResponseDeployables;
import com.sos.joc.model.publish.OperationType;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class DeployablesResourceImpl extends JOCResourceImpl implements IDeployablesResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployablesResourceImpl.class);

    @Override
    public JOCDefaultResponse deployables(final String accessToken, final byte[] inBytes) {
        try {
            JsonValidator.validateFailFast(inBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);
            if (in.getPath() != null) {
                in.setPath(normalizeFolder(in.getPath()));
            }
            JOCDefaultResponse response = checkPermissions(accessToken, in);
            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(deployables(in));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private ResponseDeployables deployables(RequestFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            session.beginTransaction();
            boolean addVersions = false;
            List<InventoryDeployablesTreeFolderItem> list = null;
            if (in.getId() != null) {
                list = dbLayer.getConfigurationsWithAllDeployments(in.getId());
                addVersions = true;
            } else if (in.getPath() != null) {
                if (in.getRecursive() != null && in.getRecursive()) {
                    list = dbLayer.getConfigurationsWithMaxDeployment(in.getPath().equals("/") ? null : in.getPath(), true);
                } else {
                    list = dbLayer.getConfigurationsWithAllDeployments(in.getPath(), in.getObjectType() == null ? null : JocInventory.getType(in
                            .getObjectType()));
                    addVersions = true;
                }
            } else {
                session.commit();
                throw new Exception("Missing id or path parameter");
            }
            session.commit();
            session = null;

            return getDeployables(dbLayer, list, in.getPath(), addVersions);
        } catch (Throwable e) {
            if (session != null && session.isTransactionOpened()) {
                Globals.rollback(session);
            }
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    public ResponseDeployables getDeployables(InventoryDBLayer dbLayer, List<InventoryDeployablesTreeFolderItem> list, String folder,
            boolean addVersions) throws Exception {
        ResponseDeployables result = new ResponseDeployables();
        if (list == null || list.size() == 0) {
            result.setDeliveryDate(new Date());
            result.setDeployables(new HashSet<ResponseDeployableTreeItem>());
            return result;
        }

        Long configId = 0L;
        ResponseDeployableTreeItem treeItem = null;
        for (InventoryDeployablesTreeFolderItem item : list) {
            if (configId.equals(item.getId())) {
                continue;
            }

            ConfigurationType type = ConfigurationType.fromValue(item.getType());
            if (type.equals(ConfigurationType.FOLDER)) {
                if (folder != null) {
                    if (folder.equals(item.getFolder())) {
                        continue;
                    }
                    // TODO reduce check isFolderEmpty
                    if (!item.getDeleted() && isFolderEmpty(dbLayer, item.getFolder())) {
                        continue;
                    }
                }
            } else if (type.equals(ConfigurationType.CALENDAR)) {
                continue;
            } else if (item.getDeployment() == null) {
                if (!item.getValide()) {
                    continue;
                }
            }

            JobSchedulerObjectType.fromValue(type.name());// throws exception if not exists
            if (folderPermissions != null && !folderPermissions.isPermittedForFolder(getParent(item.getPath()))) {
                LOGGER.info(String.format("[skip][%s]due to folder permissions", item.getPath()));
                continue;
            }

            treeItem = new ResponseDeployableTreeItem();
            treeItem.setId(item.getId());
            treeItem.setFolder(item.getFolder());
            treeItem.setObjectName(item.getName());
            treeItem.setObjectType(JobSchedulerObjectType.fromValue(ConfigurationType.fromValue(item.getType()).name()));
            treeItem.setDeleted(item.getDeleted());
            treeItem.setDeployed(item.getDeployed());

            if (item.getDeleted()) {
                addVersions = false;
                item.setDeployment(null);
            }

            if (item.getDeployment() != null) {
                if (addVersions) {
                    List<InventoryDeployablesTreeFolderItem> deployments = getDeployments(list, treeItem.getId());

                    Collections.sort(deployments, new Comparator<InventoryDeployablesTreeFolderItem>() {

                        public int compare(InventoryDeployablesTreeFolderItem d1, InventoryDeployablesTreeFolderItem d2) {// deploymentDate descending
                            return d2.getDeployment().getDeploymentDate().compareTo(d1.getDeployment().getDeploymentDate());
                        }
                    });

                    if (treeItem.getDeployed()) {
                        treeItem.setDeploymentId(deployments.get(0).getDeployment().getId());
                    } else {
                        if (item.getValide()) {
                            ResponseDeployableVersion draft = new ResponseDeployableVersion();
                            draft.setId(item.getId());
                            draft.setVersionDate(item.getModified());
                            draft.setVersions(null);
                            treeItem.getDeployablesVersions().add(draft);
                        }
                    }

                    Date date = null;
                    ResponseDeployableVersion dv = null;
                    for (InventoryDeployablesTreeFolderItem deployment : deployments) {
                        if (date == null || !date.equals(deployment.getDeployment().getDeploymentDate())) {
                            dv = new ResponseDeployableVersion();
                            dv.setId(deployment.getId());
                            dv.setVersionDate(deployment.getDeployment().getDeploymentDate());
                            dv.setDeploymentId(deployment.getDeployment().getId());
                            dv.setDeploymentOperation(OperationType.fromValue(deployment.getDeployment().getOperation()).name().toLowerCase());
                            if (!item.getPath().equals(deployment.getDeployment().getPath())) {
                                dv.setDeploymentPath(deployment.getDeployment().getPath());
                            }
                            treeItem.getDeployablesVersions().add(dv);
                        }
                        ResponseItemDeployment id = new ResponseItemDeployment();
                        id.setVersion(deployment.getDeployment().getVersion());
                        id.setControllerId(deployment.getDeployment().getControllerId());
                        dv.getVersions().add(id);

                        date = deployment.getDeployment().getDeploymentDate();
                    }
                } else {
                    if (treeItem.getDeployed() || !item.getValide()) {
                        treeItem.setDeploymentId(item.getDeployment().getId());
                    }
                }
            }
            if (!addVersions || item.getDeployment() == null) {
                treeItem.setDeployablesVersions(null);
            }
            result.getDeployables().add(treeItem);
            configId = item.getId();
        }
        result.setDeployables(sort(result.getDeployables()));
        result.setDeliveryDate(new Date());
        return result;
    }

    private boolean isFolderEmpty(InventoryDBLayer dbLayer, String folder) {
        Long result = null;
        try {
            dbLayer.getSession().beginTransaction();
            result = dbLayer.getCountConfigurationsByFolder(folder, true);
            dbLayer.getSession().commit();
        } catch (SOSHibernateException e) {
            try {
                dbLayer.getSession().rollback();
            } catch (SOSHibernateException e1) {
            }
        }
        return result == null || result.equals(0L);
    }

    private Set<ResponseDeployableTreeItem> sort(Set<ResponseDeployableTreeItem> set) {
        if (set == null || set.size() == 0) {
            return set;
        }
        return set.stream().sorted(Comparator.comparing(ResponseDeployableTreeItem::getObjectName)).collect(Collectors.toCollection(
                LinkedHashSet::new));
    }

    private List<InventoryDeployablesTreeFolderItem> getDeployments(List<InventoryDeployablesTreeFolderItem> items, final Long configId) {
        return items.stream().filter(config -> {
            if (config.getId().equals(configId) && config.getDeployment() != null) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, RequestFilter in) throws Exception {
        SOSPermissionJocCockpit permissions = getPermissonsJocCockpit("", accessToken);
        boolean permission = permissions.getJS7Controller().getAdministration().getConfigurations().isEdit();

        JOCDefaultResponse response = init(IMPL_PATH, in, accessToken, "", permission);
        if (response == null) {
            if (in.getPath() != null && !folderPermissions.isPermittedForFolder(in.getPath())) {
                return accessDeniedResponse();
            }
        }
        return response;
    }

}
