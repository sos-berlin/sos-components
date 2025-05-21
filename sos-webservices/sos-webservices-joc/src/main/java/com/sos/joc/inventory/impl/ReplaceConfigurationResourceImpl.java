package com.sos.joc.inventory.impl;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.audit.JocAuditObjectsLog;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.inventory.resource.IReplaceConfigurationResource;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.RequestFilter;
import com.sos.joc.model.inventory.replace.RequestFilters;
import com.sos.joc.model.inventory.replace.RequestFolder;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class ReplaceConfigurationResourceImpl extends JOCResourceImpl implements IReplaceConfigurationResource {

    @Override
    public JOCDefaultResponse replace(final String accessToken, byte[] inBytes) {
        try {
            inBytes = initLogging(IMPL_PATH, inBytes, accessToken, CategoryType.INVENTORY);
            JsonValidator.validate(inBytes, RequestFilters.class, true);
            RequestFilters in = Globals.objectMapper.readValue(inBytes, RequestFilters.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
            if (response == null) {
                response = replace(in);
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
    public JOCDefaultResponse replaceFolder(final String accessToken,  byte[] inBytes) {
        try {
            inBytes = initLogging(IMPL_PATH, inBytes, accessToken, CategoryType.INVENTORY);
            JsonValidator.validate(inBytes, RequestFolder.class, true);
            RequestFolder in = Globals.objectMapper.readValue(inBytes, RequestFolder.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
            if (response == null) {
                response = replaceFolder(in);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private JOCDefaultResponse replaceFolder(RequestFolder in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            session.setAutoCommit(false);
            final InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();

            DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, null, in.getPath(), ConfigurationType.FOLDER,
                    folderPermissions);
            DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());
            
            String search = in.getSearch().replaceAll("%", ".*");
            Predicate<String> regex = Pattern.compile(search, Pattern.CASE_INSENSITIVE).asPredicate();
            Predicate<DBItemInventoryConfiguration> regexFilter = item -> regex.test(item.getName());
            Predicate<DBItemInventoryConfiguration> notFolderFilter = item -> ConfigurationType.FOLDER.intValue() != item.getType();
            String replace = in.getReplace() == null ? "" : in.getReplace();

            Set<String> events = new HashSet<>();
            
            List<DBItemInventoryConfiguration> dBFolderContent = dbLayer.getFolderContent(config.getPath(), true, null, false).stream().filter(
                    notFolderFilter).filter(regexFilter).collect(Collectors.toList());
            JocAuditObjectsLog auditLogObjectsLogging = new JocAuditObjectsLog(dbAuditLog.getId());
            
            for (DBItemInventoryConfiguration item : dBFolderContent) {
                String newName = item.getName().replaceAll(search, replace);
                SOSCheckJavaVariableName.test("name", newName);
                List<DBItemInventoryConfiguration> names = dbLayer.getConfigurationByName(newName, item.getType());
                if (!names.isEmpty()) {
                    throw new JocObjectAlreadyExistException("Cannot rename " + item.getName() + " to " + newName);
                }
                events.addAll(JocInventory.deepCopy(item, newName, dBFolderContent, dbLayer));

                auditLogObjectsLogging.addDetail(JocAuditLog.storeAuditLogDetail(new AuditLogDetail(item.getPath(), item.getType()), session, dbAuditLog));
                setItem(item, Paths.get(item.getFolder()).resolve(newName), dbAuditLog.getId());
                JocInventory.updateConfiguration(dbLayer, item);
            }
            
            session.commit();
            auditLogObjectsLogging.log();
            
            for (String event : events) {
                JocInventory.postEvent(event);
            }
            if (!events.isEmpty()) {
                JocInventory.postFolderEvent(config.getFolder());
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));

        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private JOCDefaultResponse replace(RequestFilters in) throws Exception {
        SOSHibernateSession session = null;
        try {
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            session.setAutoCommit(false);
            final InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            session.beginTransaction();

            Predicate<RequestFilter> isFolder = r -> ConfigurationType.FOLDER.equals(r.getObjectType());
            if (in.getObjects().stream().parallel().anyMatch(isFolder)) {
                // throw new
            }
            DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());
            Set<String> events = new HashSet<>();
            List<Long> workflowInvIds = new ArrayList<>();
            String search = in.getSearch().replaceAll("%", ".*");
            String replace = in.getReplace() == null ? "" : in.getReplace();
            Set<RequestFilter> requests = in.getObjects().stream().filter(isFolder.negate()).collect(Collectors.toSet());
            JocAuditObjectsLog auditLogObjectsLogging = new JocAuditObjectsLog(dbAuditLog.getId());
            
            for (RequestFilter r : requests) {
                DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, r, folderPermissions);

                String newName = config.getName().replaceAll(search, replace);
                final java.nio.file.Path p = Paths.get(config.getFolder()).resolve(newName);

                if (config.getName().equals(newName)) { // Nothing to do
                    //return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
                    continue;
                }

                SOSCheckJavaVariableName.test("name", newName);

                if (!config.getName().equalsIgnoreCase(p.getFileName().toString())) { // if not only upper-lower case is changed then check if target exists
                    DBItemInventoryConfiguration targetItem = dbLayer.getConfiguration(p.toString().replace('\\', '/'), config.getType());

                    if (targetItem != null) {
                        throw new JocObjectAlreadyExistException(String.format("%s %s already exists", ConfigurationType.fromValue(config.getType())
                                .value().toLowerCase(), targetItem.getPath()));
                    } else {
                        // check unique name
                        List<DBItemInventoryConfiguration> namedItems = dbLayer.getConfigurationByName(newName, config.getType());
                        if (namedItems != null) {
                            namedItems.remove(config);
                            if (!namedItems.isEmpty()) {
                                throw new JocObjectAlreadyExistException(String.format("The name has to be unique: '%s' is already used in '%s'", p
                                        .getFileName().toString(), namedItems.get(0).getPath()));
                            }
                        }
                    }
                }

                events.addAll(JocInventory.deepCopy(config, p.getFileName().toString(), dbLayer));
                auditLogObjectsLogging.addDetail(JocAuditLog.storeAuditLogDetail(new AuditLogDetail(config.getPath(), config.getType()), session,
                        dbAuditLog));
                setItem(config, p, dbAuditLog.getId());
                JocInventory.updateConfiguration(dbLayer, config);
                events.add(config.getFolder());
                if (JocInventory.isWorkflow(config.getType())) {
                    workflowInvIds.add(config.getId());
                }
            }

            Globals.commit(session);
            auditLogObjectsLogging.log();
            events.forEach(JocInventory::postEvent);
            // post event: InventoryTaggingUpdated
            if (workflowInvIds != null && !workflowInvIds.isEmpty()) {
                InventoryTagDBLayer dbTagLayer = new InventoryTagDBLayer(dbLayer.getSession());
                dbTagLayer.getTags(workflowInvIds).stream().distinct().forEach(JocInventory::postTaggingEvent);
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private static void setItem(DBItemInventoryConfiguration oldItem, java.nio.file.Path newItem, Long auditLogId) {
        oldItem.setPath(newItem.toString().replace('\\', '/'));
        oldItem.setName(newItem.getFileName().toString());
        oldItem.setDeployed(false);
        oldItem.setReleased(false);
        oldItem.setAuditLogId(auditLogId);
        oldItem.setModified(Date.from(Instant.now()));
    }

}
