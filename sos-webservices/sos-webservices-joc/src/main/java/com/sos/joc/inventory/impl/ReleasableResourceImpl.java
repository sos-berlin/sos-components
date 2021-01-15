package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocDeployException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.inventory.resource.IReleasableResource;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.release.ReleasableFilter;
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
            JsonValidator.validate(inBytes, ReleasableFilter.class);
            ReleasableFilter in = Globals.objectMapper.readValue(inBytes, ReleasableFilter.class);

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

    private ResponseReleasable releasable(ReleasableFilter in) throws Exception {
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
            
            DBItemInventoryReleasedConfiguration releasedItem = dbLayer.getReleasedItemByConfigurationId(config.getId());
            
            if (in.getWithoutDrafts() && releasedItem != null) {  // contains only drafts which are already released
                throw new JocDeployException(String.format("%s is a draft without a released version: %s", type.value().toLowerCase(), config
                        .getPath()));
            }
            if (in.getWithoutReleased() && config.getReleased()) {
                throw new JocDeployException(String.format("%s is already released: %s", type.value().toLowerCase(), config
                        .getPath()));
            }
            if (in.getWithoutReleased() && in.getOnlyValidObjects() && !config.getValid() && !config.getDeleted()) {
                throw new JocDeployException(String.format("%s is not valid: %s", type.value().toLowerCase(), config
                        .getPath()));
            }
            if (!in.getWithoutReleased() && releasedItem != null && in.getOnlyValidObjects() && !config.getValid() && !config.getDeleted()) {
                throw new JocDeployException(String.format("%s is not valid: %s", type.value().toLowerCase(), config
                        .getPath()));
            }
            
            ResponseReleasableTreeItem treeItem = getResponseReleasableTreeItem(config);
            
            if (!in.getWithoutDrafts() || !in.getWithoutReleased()) {
                Set<ResponseReleasableVersion> versions = new LinkedHashSet<>();
                if (!treeItem.getReleased() && config.getValid() && !in.getWithoutDrafts()) {
                    ResponseReleasableVersion draft = new ResponseReleasableVersion();
                    draft.setId(config.getId());
                    draft.setVersionDate(config.getModified());
                    versions.add(draft);
                }
                versions.addAll(getVersion(config.getId(), releasedItem, in.getWithoutReleased()));
//                if (versions.isEmpty()) {
//                    versions = null;
//                }
                treeItem.setReleasableVersions(versions);
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
    
    public static Set<ResponseReleasableVersion> getVersion(Long confId, DBItemInventoryReleasedConfiguration release, boolean withoutReleased) {
        if (release == null || withoutReleased) {
            return Collections.emptySet();
        }

        ResponseReleasableVersion rv = new ResponseReleasableVersion();
        rv.setId(confId);
        rv.setVersionDate(release.getModified());
        rv.setReleaseId(release.getId());
        rv.setReleasePath(release.getPath());
        
        return Collections.singleton(rv);
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
        treeItem.setReleasableVersions(null);
        return treeItem;
    }

}
