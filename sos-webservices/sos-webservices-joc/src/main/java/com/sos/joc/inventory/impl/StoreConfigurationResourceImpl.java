package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.model.lock.Lock;
import com.sos.joc.Globals;
import com.sos.joc.classes.CheckJavaVariableName;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.InventoryAudit;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JocInventory.InventoryPath;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.inventory.resource.IStoreConfigurationResource;
import com.sos.joc.model.common.ICalendarObject;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.CalendarType;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ItemStateEnum;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class StoreConfigurationResourceImpl extends JOCResourceImpl implements IStoreConfigurationResource {

    @Override
    public JOCDefaultResponse store(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, ConfigurationObject.class);
            ConfigurationObject in = Globals.objectMapper.readValue(inBytes, ConfigurationObject.class);

            JOCDefaultResponse response = initPermissions(null, getPermissonsJocCockpit("", accessToken).getInventory().getConfigurations().isEdit());
            if (response == null) {
                response = store(in);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private JOCDefaultResponse store(ConfigurationObject in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            session.beginTransaction();
            DBItemInventoryConfiguration item;
            try {
                item = JocInventory.getConfiguration(dbLayer, in.getId(), in.getPath(), null, in.getObjectType(), folderPermissions);
                item = setProperties(in, item, false);
                JocInventory.updateConfiguration(dbLayer, item, in.getConfiguration());
            } catch (DBMissingDataException e) {
                checkRequiredParameter("path", in.getPath());
                checkRequiredParameter("objectType", in.getObjectType());
                java.nio.file.Path path = JocInventory.normalizePath(in.getPath());

                // Check Java variable name rules
//                for (int i = 0; i < path.getNameCount(); i++) {
//                    if (i == path.getNameCount() - 1) {
//                        CheckJavaVariableName.test("name", path.getName(i).toString());
//                    } else {
//                        CheckJavaVariableName.test("folder", path.getName(i).toString());
//                    }
//                }
                CheckJavaVariableName.test("name", path.getFileName().toString());

                // check if name is unique
                if (!JocInventory.isFolder(in.getObjectType())) {
                    String name = path.getFileName().toString();
                    List<DBItemInventoryConfiguration> namedItems = dbLayer.getConfigurationByName(name, in.getObjectType().intValue());
                    if (namedItems != null && !namedItems.isEmpty()) {
                        throw new JocObjectAlreadyExistException(String.format("The name has to be unique: '%s' is already used in '%s'", name,
                                namedItems.get(0).getPath()));
                    }
                }

                // mkdirs if necessary
                JocInventory.makeParentDirs(dbLayer, path.getParent());

                item = new DBItemInventoryConfiguration();
                item.setType(in.getObjectType());
                item = setProperties(in, item, true);
                item.setCreated(Date.from(Instant.now()));
                createAuditLog(item, in.getObjectType());
                JocInventory.insertConfiguration(dbLayer, item, in.getConfiguration());
                JocInventory.postEvent(item.getFolder());
            }
            session.commit();

            ConfigurationObject answer = new ConfigurationObject();
            answer.setId(item.getId());
            answer.setDeliveryDate(Date.from(Instant.now()));
            answer.setPath(item.getPath());
            answer.setConfigurationDate(item.getModified());
            answer.setObjectType(JocInventory.getType(item.getType()));
            answer.setValid(item.getValid());
            answer.setInvalidMsg(in.getInvalidMsg());
            answer.setDeployed(false);
            answer.setReleased(false);
            answer.setState(ItemStateEnum.DRAFT_IS_NEWER);// TODO

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private void createAuditLog(DBItemInventoryConfiguration config, ConfigurationType objectType) throws Exception {
        InventoryAudit audit = new InventoryAudit(objectType, config.getPath(), config.getFolder());
        logAuditMessage(audit);
        DBItemJocAuditLog auditItem = storeAuditLogEntry(audit);
        if (auditItem != null) {
            config.setAuditLogId(auditItem.getId());
        }
    }

    private DBItemInventoryConfiguration setProperties(ConfigurationObject in, DBItemInventoryConfiguration item, boolean isNew) throws Exception {

        if (isNew) {
            InventoryPath path = new InventoryPath(in.getPath(), in.getObjectType());
            item.setPath(path.getPath());
            item.setName(path.getName());
            item.setFolder(path.getFolder());
            item.setValid(false);
            item.setDocumentationId(0L);
            item.setTitle(null);
        }

        if (ConfigurationType.FOLDER.equals(in.getObjectType())) {
            item.setTitle(null);
            item.setValid(true);
            item.setContent(null);
        } else {
            if (JocInventory.isCalendar(in.getObjectType())) {
                item.setType(in.getObjectType().intValue());
            }

            if (in.getConfiguration() == null) {
                item.setContent(null);
                item.setValid(false);
            } else {
                item.setValid(in.getValid() == null ? true : in.getValid());
                item.setTitle(in.getConfiguration().getTitle());

                switch (in.getObjectType()) {
                // case WORKFLOW:
                // ((Workflow) in.getConfiguration()).setPath(item.getPath());
                // break;
                // case SCHEDULE:
                // ((Schedule) in.getConfiguration()).setPath(item.getPath());
                // break;
                case WORKINGDAYSCALENDAR:
                case NONWORKINGDAYSCALENDAR:
                    // Calendar calendar = (Calendar) in.getConfiguration();
                    // calendar.setPath(item.getPath());
                    // calendar.setType(CalendarType.fromValue(in.getObjectType().value()));
                    ((ICalendarObject) in.getConfiguration()).setType(CalendarType.fromValue(in.getObjectType().value()));
                    break;
                case LOCK: // without Path
                    // TODO tmp solution - should be removed when validation works
                    Lock lock = (Lock) in.getConfiguration();
                    lock.setId(item.getName());
                    if (lock.getLimit() == null) {
                        lock.setLimit(1);
                    }
                    break;
                default:
                    break;
                }
                validate(item, in);
            }
        }

        item.setDeployed(false);
        item.setReleased(false);
        item.setModified(Date.from(Instant.now()));
        return item;
    }

    private void validate(DBItemInventoryConfiguration item, ConfigurationObject in) {

        try {
            item.setContent(JocInventory.toString(in.getConfiguration()));
            ValidateResourceImpl.validate(in.getObjectType(), in.getConfiguration());
            item.setValid(true);
        } catch (Throwable e) {
            item.setValid(false);
            in.setInvalidMsg(e.getMessage());
            // LOGGER.warn(String.format("[invalid][client valid=%s][%s] %s", in.getValid(), in.getConfiguration().toString(), e.toString()));
        }
    }

}
