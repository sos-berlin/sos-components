package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IDeleteConfigurationResource;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.RequestFilter;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class DeleteConfigurationResourceImpl extends JOCResourceImpl implements IDeleteConfigurationResource {

    @Override
    public JOCDefaultResponse delete(final String accessToken, final byte[] inBytes) {
        return deleteOrUndelete(IMPL_PATH_DELETE, accessToken, inBytes);
    }
    
    @Override
    public JOCDefaultResponse undelete(final String accessToken, final byte[] inBytes) {
        return deleteOrUndelete(IMPL_PATH_UNDELETE, accessToken, inBytes);
    }
    
    private JOCDefaultResponse deleteOrUndelete(final String action, final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(action, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);

            JOCDefaultResponse response = initPermissions(null, getPermissonsJocCockpit("", accessToken).getInventory().getConfigurations().isEdit());
            if (response == null) {
                response = deleteOrUndelete(action, in);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private JOCDefaultResponse deleteOrUndelete(String action, RequestFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(action);
            session.setAutoCommit(false);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, in, folderPermissions);
            ConfigurationType type = config.getTypeAsEnum();
            if (ConfigurationType.FOLDER.equals(type)) {
                // deleteOrUndeleteFolder(dbLayer, config.getPath(), IMPL_PATH_DELETE.equals(action));
                // no recursive action otherwise information get lost after delete AND undelete. Deleted folder has to be consider at the deploy/release
                if (!JocInventory.ROOT_FOLDER.equals(config.getPath())) {
                    if (isFolderEmptyOrHasOnlyEmptySubFolders(dbLayer, config.getPath())) {
                        List<DBItemInventoryConfiguration> emptyFolders = dbLayer.getFolderContent(config.getPath(), true, Arrays.asList(
                                ConfigurationType.FOLDER.intValue()));
                        for (DBItemInventoryConfiguration emptyFolder : emptyFolders) {
                            dbLayer.getSession().delete(emptyFolder);
                        }
                        dbLayer.getSession().delete(config);
                    } else {
                        deleteOrUndeleteSingle(dbLayer, config, IMPL_PATH_DELETE.equals(action));
                        storeAuditLog(type, config.getPath(), config.getFolder());
                    }
                }
            } else {
                deleteOrUndeleteSingle(dbLayer, config, IMPL_PATH_DELETE.equals(action));
                storeAuditLog(type, config.getPath(), config.getFolder());
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private void deleteOrUndeleteSingle(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config, boolean deleteFlag) throws Exception {
        try {
            dbLayer.getSession().beginTransaction();
            dbLayer.markConfigurationAsDeleted(config.getId(), deleteFlag);
//            if (!deleteFlag) {
//                handleParentFolders(dbLayer, config.getFolder());
//            }
            Globals.commit(dbLayer.getSession());
        } catch (Exception e) {
            Globals.rollback(dbLayer.getSession());
            throw e;
        }
    }
    
//    private void handleParentFolders(InventoryDBLayer dbLayer, final String folder) throws Exception {
//        if (folder != null && !folder.equals(JocInventory.ROOT_FOLDER)) {
//            java.nio.file.Path p = Paths.get(folder);
//            List<String> parentFolders = new ArrayList<>();
//            for (int i = 0; i < p.getNameCount(); i++) {
//                parentFolders.add(("/" + p.subpath(0, i+1)).replace('\\', '/'));
//            }
//            if (!parentFolders.isEmpty()) {
//                dbLayer.markFoldersAsDeleted(parentFolders, false);
//            }
//        }
//    }
//
//    private void deleteOrUndeleteFolder(InventoryDBLayer dbLayer, String folder, boolean deleteFlag) throws Exception {
//        try {
//            dbLayer.getSession().beginTransaction();
//            List<DBItemInventoryConfiguration> items = dbLayer.getFolderContent(folder, true);
//            if (items != null) {
//                dbLayer.markConfigurationsAsDeleted(items.stream().map(DBItemInventoryConfiguration::getId).collect(Collectors.toSet()), deleteFlag);
//            }
//            Globals.commit(dbLayer.getSession());
//        } catch (Exception e) {
//            Globals.rollback(dbLayer.getSession());
//            throw e;
//        }
//    }
    
    private boolean isFolderEmptyOrHasOnlyEmptySubFolders(InventoryDBLayer dbLayer, String folder) {
      Long result = null;
      try {
          dbLayer.getSession().beginTransaction();
          Set<Integer> types = JocInventory.getDeployableTypes();
          types.addAll(JocInventory.getReleasableTypes());
          result = dbLayer.getCountConfigurationsByFolder(folder, true, types);
          dbLayer.getSession().commit();
      } catch (SOSHibernateException e) {
          Globals.rollback(dbLayer.getSession());
      }
      return result != null && result.equals(0L);
  }

    private void storeAuditLog(ConfigurationType objectType, String path, String folder) {
        InventoryAudit audit = new InventoryAudit(objectType, path, folder);
        logAuditMessage(audit);
        storeAuditLogEntry(audit);
    }

}
