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
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.items.InventoryReleaseItem;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocDeployException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.inventory.resource.IReleasableResource;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.deploy.DeployableFilter;
import com.sos.joc.model.inventory.release.ResponseItemRelease;
import com.sos.joc.model.inventory.release.ResponseReleasable;
import com.sos.joc.model.inventory.release.ResponseReleasableTreeItem;
import com.sos.joc.model.inventory.release.ResponseReleasableVersion;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class ReleasableResourceImpl extends JOCResourceImpl implements IReleasableResource {

    @Override
    public JOCDefaultResponse releasable(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, DeployableFilter.class);
            DeployableFilter in = Globals.objectMapper.readValue(inBytes, DeployableFilter.class);

            JOCDefaultResponse response = initPermissions(null, getPermissonsJocCockpit("", accessToken).getInventory().getConfigurations().isEdit());

            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(releasable(in));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private ResponseReleasable releasable(DeployableFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            
            DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, in, folderPermissions);
            ConfigurationType type = config.getTypeAsEnum();
            
            if (ConfigurationType.FOLDER.equals(type)) {
                throw new JocNotImplementedException("use ./inventory/releasables for folders!");
            }
            if (!JocInventory.isReleasable(type)) {
                throw new JobSchedulerInvalidResponseDataException("Object is not a 'Scheduling Object': " + type.value());
            }
            
            // get deleted folders
            List<String> deletedFolders = dbLayer.getDeletedFolders();
            // if inside deletedFolders -> setDeleted(true);
            Predicate<String> filter = f -> config.getPath().startsWith((f + "/").replaceAll("//+", "/"));
            if (deletedFolders != null && !deletedFolders.isEmpty() && deletedFolders.stream().parallel().anyMatch(filter)) {
                config.setDeleted(true);
            }
            
            ResponseReleasableTreeItem treeItem = getResponseReleasableTreeItem(config);
            
            if (in.getWithVersions()) {
                List<InventoryReleaseItem> releases = dbLayer.getReleasedConfigurations(config.getId());
                if (releases != null && !releases.isEmpty()) {
                    Set<ResponseReleasableVersion> versions = new LinkedHashSet<>();
                    if (treeItem.getReleased()) {
                        treeItem.setReleaseId(releases.iterator().next().getId());
                    } else {
                        if (config.getValid()) {
                            ResponseReleasableVersion draft = new ResponseReleasableVersion();
                            draft.setId(config.getId());
                            draft.setVersionDate(config.getModified());
                            draft.setVersions(null);
                            versions.add(draft);
                        }
                    }
                    versions.addAll(getVersions(config.getId(), releases));
                    if (versions.isEmpty()) {
                        versions = null;
                    }
                    treeItem.setReleaseVersions(versions);
                } else if (in.getOnlyValidObjects() && !config.getValid() && !config.getDeleted()) {
                    throw new JocDeployException(String.format("%s not valid: %s", type.value().toLowerCase(), config.getPath()));
                }
            } else {
                InventoryReleaseItem releaseItem = dbLayer.getLastReleasedConfiguration(config.getId());
                if (releaseItem != null) {
                    if (treeItem.getReleased() || !config.getValid()) {
                        treeItem.setReleaseId(releaseItem.getId());
                    } else if (in.getOnlyValidObjects() && !config.getValid() && !config.getDeleted()) {
                        throw new JocDeployException(String.format("%s not valid: %s", type.value().toLowerCase(), config.getPath()));
                    }
                }
            }

            ResponseReleasable result = new ResponseReleasable();
            result.setDeliveryDate(Date.from(Instant.now()));
            result.setReleasable(treeItem);
            return result;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    public static Set<ResponseReleasableVersion> getVersions(Long confId, Collection<InventoryReleaseItem> releases) {
        if (releases == null) {
            return Collections.emptySet();
        }
        
        Map<Date, Set<ResponseItemRelease>> versions = releases.stream().filter(Objects::nonNull)
                .collect(Collectors.groupingBy(InventoryReleaseItem::getReleaseDate, Collectors.mapping(release -> {
            ResponseItemRelease id = new ResponseItemRelease();
            id.setControllerId(release.getControllerId());
            return id;
        }, Collectors.toSet())));
        
        Map<Date, InventoryReleaseItem> mapDateGrouped = releases.stream().filter(Objects::nonNull)
                .collect(Collectors.toMap(InventoryReleaseItem::getReleaseDate, Function.identity()));
        
        return mapDateGrouped.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).map(e -> {
            InventoryReleaseItem release = e.getValue();
            ResponseReleasableVersion dv = new ResponseReleasableVersion();
            dv.setId(confId);
            dv.setVersions(versions.get(release.getReleaseDate()));
            dv.setVersionDate(release.getReleaseDate());
            dv.setReleaseId(release.getId());
            dv.setReleasePath(release.getPath());
            return dv;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    public static ResponseReleasableTreeItem getResponseReleasableTreeItem(DBItemInventoryConfiguration item) {
        ResponseReleasableTreeItem treeItem = new ResponseReleasableTreeItem();
        treeItem.setId(item.getId());
        treeItem.setFolder(item.getFolder());
        treeItem.setObjectName(item.getName());
        treeItem.setObjectType(JocInventory.getType(item.getType()));
        treeItem.setDeleted(item.getDeleted());
        treeItem.setReleased(item.getReleased());
        treeItem.setValid(item.getValid());
        treeItem.setReleaseVersions(null);
        return treeItem;
    }

}
