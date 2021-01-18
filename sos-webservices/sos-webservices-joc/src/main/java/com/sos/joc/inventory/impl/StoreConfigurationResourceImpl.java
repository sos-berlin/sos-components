package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
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
                item = JocInventory.getConfiguration(dbLayer, in.getId(), in.getPath(), in.getObjectType(), folderPermissions);
                item = setProperties(in, item, false);
                JocInventory.updateConfiguration(dbLayer, item, in.getConfiguration());
            } catch (DBMissingDataException e) {
                item = new DBItemInventoryConfiguration();
                item.setType(in.getObjectType());
                item = setProperties(in, item, true);
                item.setCreated(Date.from(Instant.now()));
                createAuditLog(item, in.getObjectType());
                JocInventory.insertConfiguration(dbLayer, item, in.getConfiguration());
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

        if (JocInventory.isCalendar(in.getObjectType())) {
            item.setType(in.getObjectType().intValue());
        }

        if (!ConfigurationType.FOLDER.equals(in.getObjectType())) {
            if (in.getConfiguration() == null) {
                item.setContent(null);
                item.setValid(false);
            } else {
                item.setValid(in.getValid() == null ? true : in.getValid());
                item.setTitle(in.getConfiguration().getTitle());
                // "path" is required in schemas except for JOB and FOLDER
                if (!ConfigurationType.JOB.equals(in.getObjectType())) {
//                    in.getConfiguration().setPath(item.getPath());
                }
                if (JocInventory.isCalendar(in.getObjectType())) {
                    ((ICalendarObject) in.getConfiguration()).setType(CalendarType.fromValue(in.getObjectType().value()));
                }
                validate(item, in);
            }
        } else {
            item.setTitle(null);
            item.setValid(true);
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
