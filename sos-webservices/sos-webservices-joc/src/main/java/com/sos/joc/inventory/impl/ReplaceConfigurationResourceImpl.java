package com.sos.joc.inventory.impl;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.CheckJavaVariableName;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.InventoryAudit;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.inventory.resource.IReplaceConfigurationResource;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.replace.RequestFilter;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class ReplaceConfigurationResourceImpl extends JOCResourceImpl implements IReplaceConfigurationResource {

    @Override
    public JOCDefaultResponse replace(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);

            JOCDefaultResponse response = initPermissions(null, getPermissonsJocCockpit("", accessToken).getInventory().getConfigurations().isEdit());
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

    private JOCDefaultResponse replace(RequestFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            session.setAutoCommit(false);
            final InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            
            session.beginTransaction();
            DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, in, folderPermissions);
            ConfigurationType type = config.getTypeAsEnum();
            
            String search = in.getSearch().replaceAll("%", ".*");
            
            Set<String> events = new HashSet<>();
            
            if (JocInventory.isFolder(type)) {
                boolean isUpdated = false;
                List<DBItemInventoryConfiguration> dBFolderContent = dbLayer.getFolderContent(config.getPath(), true, null);
                for (DBItemInventoryConfiguration item : dBFolderContent) {
                    if (ConfigurationType.FOLDER.intValue() == item.getType()) {
                        continue;
                    } else {
                        String newName = item.getName().replaceAll(search, in.getReplace());
                        if (item.getName().equals(newName)) {
                            continue;
                        }
                        if (!item.getName().equalsIgnoreCase(newName)) {
                            CheckJavaVariableName.test("name", item.getName());
                            List<DBItemInventoryConfiguration> names = dbLayer.getConfigurationByName(item.getName(), item.getType());
                            if (!names.isEmpty()) {
                                throw new JocObjectAlreadyExistException("Cannot rename to " + item.getName());
                            }
                        }
                        setItem(item, Paths.get(item.getFolder()).resolve(newName));
                        JocInventory.updateConfiguration(dbLayer, item);
                        isUpdated = true;
                    }
                }
                if (isUpdated) {
                    createAuditLog(config);
                    events.add(config.getFolder());
                }

            } else {
                
                String newName = config.getName().replaceAll(search, in.getReplace());
                final java.nio.file.Path p = Paths.get(config.getFolder()).resolve(newName);
                
                if (config.getName().equals(newName)) { // Nothing to do
                    return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
                }
                
                CheckJavaVariableName.test("name", newName);
                
                if (!config.getName().equalsIgnoreCase(p.getFileName().toString())) { //if not only upper-lower case is changed then check if target exists
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
                
                switch (type) {
                case LOCK: // determine Workflows with Lock instructions
                    List<DBItemInventoryConfiguration> workflows = dbLayer.getUsedWorkflowsByLockId(config.getName());
                    if (workflows != null && !workflows.isEmpty()) {
                        for (DBItemInventoryConfiguration workflow : workflows) {
                            workflow.setContent(workflow.getContent().replaceAll("(\"lockId\"\\s*:\\s*\")" + config.getName() + "\"", "$1" + p
                                    .getFileName() + "\""));
                            workflow.setDeployed(false);
                            JocInventory.updateConfiguration(dbLayer, workflow);
                            events.add(workflow.getFolder());
                        }
                    }
                    break;
                case WORKFLOW: // determine Schedules with Workflow reference
                    List<DBItemInventoryConfiguration> schedules = dbLayer.getUsedSchedulesByWorkflowName(config.getName());
                    if (schedules != null && !schedules.isEmpty()) {
                        for (DBItemInventoryConfiguration schedule : schedules) {
                            schedule.setContent(schedule.getContent().replaceAll("(\"workflowName\"\\s*:\\s*\")" + config.getName() + "\"", "$1" + p
                                    .getFileName() + "\""));
                            schedule.setReleased(false);
                            JocInventory.updateConfiguration(dbLayer, schedule);
                            events.add(schedule.getFolder());
                        }
                    }
                    break;
                case WORKINGDAYSCALENDAR: // determine Schedules with Calendar reference
                case NONWORKINGDAYSCALENDAR:
                    List<DBItemInventoryConfiguration> schedules1 = dbLayer.getUsedSchedulesByCalendarName(config.getName());
                    if (schedules1 != null && !schedules1.isEmpty()) {
                        for (DBItemInventoryConfiguration schedule : schedules1) {
                            schedule.setContent(schedule.getContent().replaceAll("(\"calendarName\"\\s*:\\s*\")" + config.getName() + "\"", "$1" + p
                                    .getFileName() + "\""));
                            schedule.setReleased(false);
                            JocInventory.updateConfiguration(dbLayer, schedule);
                            events.add(schedule.getFolder());
                        }
                    }
                    break;
                default:
                    break;
                }
                
                setItem(config, p);
                createAuditLog(config);
                JocInventory.updateConfiguration(dbLayer, config);
                events.add(config.getFolder());
            }
            
            session.commit();
            for (String event : events) {
                JocInventory.postEvent(event);
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private void createAuditLog(DBItemInventoryConfiguration config) throws Exception {
        InventoryAudit audit = new InventoryAudit(config.getTypeAsEnum(), config.getPath(), config.getFolder());
        logAuditMessage(audit);
        DBItemJocAuditLog auditItem = storeAuditLogEntry(audit);
        if (auditItem != null) {
            config.setAuditLogId(auditItem.getId());
        }
    }

    private static void setItem(DBItemInventoryConfiguration oldItem, java.nio.file.Path newItem) {
        oldItem.setPath(newItem.toString().replace('\\', '/'));
        oldItem.setName(newItem.getFileName().toString());
        oldItem.setDeployed(false);
        oldItem.setReleased(false);
        oldItem.setModified(Date.from(Instant.now()));
    }

}
