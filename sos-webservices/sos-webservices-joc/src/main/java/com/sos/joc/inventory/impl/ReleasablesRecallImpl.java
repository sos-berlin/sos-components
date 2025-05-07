package com.sos.joc.inventory.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.inventory.resource.IReleasablesRecall;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.RequestFolder;
import com.sos.joc.model.inventory.release.ReleasableRecallFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("inventory")
public class ReleasablesRecallImpl extends JOCResourceImpl implements IReleasablesRecall {

    private static final String API_CALL = "./inventory/releasables/recall";
    private static final String API_CALL_FOLDER = "./inventory/releasables/recall/folder";

    @Override
    public JOCDefaultResponse postRecall(String accessToken, byte[] filter) {
        SOSHibernateSession hibernateSession = null;
        try {
            filter = initLogging(API_CALL, filter, accessToken);
            JsonValidator.validate(filter, ReleasableRecallFilter.class, true);
            ReleasableRecallFilter recallFilter = Globals.objectMapper.readValue(filter, ReleasableRecallFilter.class);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
            if (response != null) {
                return response;
            }
            
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            Optional<JocBadRequestException> optException = recallFilter.getReleasables().stream().filter(released -> !JocInventory.isReleasable(
                    released.getObjectType())).findAny().map(r -> new JocBadRequestException(String.format(
                            "The object '%s' of type '%s' is not a releasable object.", r.getPath(), r.getObjectType().value().toLowerCase())));
            if (optException.isPresent()) {
                throw optException.get();
            }
            DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), recallFilter.getAuditLog());
            Long dbAuditLogId = dbAuditLog.getId();

            List<DBItemInventoryReleasedConfiguration> dbItemReleasables = recallFilter.getReleasables().stream().map(released -> dbLayer
                    .getReleasedConfiguration(JocInventory.pathToName(released.getPath()), released.getObjectType())).filter(Objects::nonNull).filter(
                            dbItemReleased -> dbLayer.recallReleasedConfiguration(dbItemReleased, dbAuditLogId)).collect(Collectors.toList());

            List<AuditLogDetail> auditLogDetails = new ArrayList<>();
            Set<String> events = new HashSet<>();
            for (DBItemInventoryReleasedConfiguration dbItemReleasable : dbItemReleasables) {
                auditLogDetails.add(new AuditLogDetail(dbItemReleasable.getPath(), dbItemReleasable.getType()));
                events.add(dbItemReleasable.getFolder());
            }
            JocAuditLog.storeAuditLogDetails(auditLogDetails, hibernateSession, dbAuditLogId, dbAuditLog.getCreated());
            events.stream().forEach(JocInventory::postEvent);
            
            return JOCDefaultResponse.responseStatusJSOk(new Date());
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postRecallByFolder(String accessToken, byte[] filter) {
        SOSHibernateSession hibernateSession = null;
        try {
            filter = initLogging(API_CALL_FOLDER, filter, accessToken);
            JsonValidator.validate(filter, RequestFolder.class, true);
            RequestFolder recallFilter = Globals.objectMapper.readValue(filter, RequestFolder.class);
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
            if (response != null) {
                return response;
            }
            
            InventoryDBLayer dbInvLayer = new InventoryDBLayer(hibernateSession);
            DBLayerDeploy dbDepLayer = new DBLayerDeploy(hibernateSession);
            
            if (!folderPermissions.isPermittedForFolder(recallFilter.getPath())) {
                throw new JocFolderPermissionsException("Access denied: " + recallFilter.getPath());
            }
            
            DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), recallFilter.getAuditLog());
            Long dbAuditLogId = dbAuditLog.getId();
            
            Folder folder = new Folder();
            folder.setFolder(recallFilter.getPath());
            folder.setRecursive(recallFilter.getRecursive());
            Stream<ConfigurationType> objectTypes = JocInventory.getReleasableTypesStream(recallFilter.getObjectTypes());
            List<DBItemInventoryReleasedConfiguration> releasables = dbInvLayer.getReleasedConfigurationsByFolder(Collections.singleton(folder),
                    objectTypes.collect(Collectors.toSet()));

            if (releasables != null && !releasables.isEmpty()) {
                List<AuditLogDetail> auditLogDetails = releasables.stream().filter(Objects::nonNull).map(releasable -> {
                    if (dbDepLayer.recallReleasedConfiguration(releasable, dbAuditLogId)) {
                        return new AuditLogDetail(releasable.getPath(), releasable.getType());
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toList());
                
                JocAuditLog.storeAuditLogDetails(auditLogDetails, hibernateSession, dbAuditLogId, dbAuditLog.getCreated());
                JocInventory.postEvent(recallFilter.getPath());
            }
            
            return JOCDefaultResponse.responseStatusJSOk(new Date());
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}
