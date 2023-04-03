package com.sos.joc.inventory.impl.common;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.inventory.model.calendar.CalendarType;
import com.sos.inventory.model.common.IInventoryObject;
import com.sos.inventory.model.jobtemplate.JobTemplate;
import com.sos.inventory.model.lock.Lock;
import com.sos.inventory.model.schedule.OrderParameterisation;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.classes.inventory.JocInventory.InventoryPath;
import com.sos.joc.classes.inventory.ReferenceValidator;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.model.common.ICalendarObject;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.ItemStateEnum;

public abstract class AStoreConfiguration extends JOCResourceImpl {

    public JOCDefaultResponse store(ConfigurationObject in, ConfigurationType folderType, String request) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(request);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            session.beginTransaction();
            DBItemInventoryConfiguration item;
            try {
                item = JocInventory.getConfiguration(dbLayer, in.getId(), in.getPath(), in.getObjectType(), folderPermissions, true);
                item = setProperties(in, item, dbLayer, false);
                DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog(), Collections.singleton(new AuditLogDetail(item
                        .getPath(), item.getType())));
                item.setAuditLogId(dbAuditLog.getId());
                JocInventory.updateConfiguration(dbLayer, item, in.getConfiguration());
                if (JocInventory.isFolder(item.getType())) {
                    JocInventory.postFolderEvent(item.getFolder());
                } else {
                    JocInventory.postEvent(item.getFolder());
                }
                
            } catch (DBMissingDataException e) {
                checkRequiredParameter("path", in.getPath());
                checkRequiredParameter("objectType", in.getObjectType());
                java.nio.file.Path path = JocInventory.normalizePath(in.getPath());

                // Check Java variable name rules
                for (int i = 0; i < path.getNameCount(); i++) {
                    if (i == path.getNameCount() - 1) {
                        SOSCheckJavaVariableName.test("name", path.getName(i).toString());
                    } else {
                        SOSCheckJavaVariableName.test("folder", path.getName(i).toString());
                    }
                }

                // check if name is unique
                if (!JocInventory.isFolder(in.getObjectType())) {
                    String name = path.getFileName().toString();
                    List<DBItemInventoryConfiguration> namedItems = dbLayer.getConfigurationByName(name, in.getObjectType().intValue());
                    if (namedItems != null && !namedItems.isEmpty()) {
                        throw new JocObjectAlreadyExistException(String.format("The name has to be unique: '%s' is already used in '%s'", name,
                                namedItems.get(0).getPath()));
                    }
                }
                
                DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());

                // mkdirs if necessary
                JocInventory.makeParentDirs(dbLayer, path.getParent(), folderType);

                item = new DBItemInventoryConfiguration();
                item.setType(in.getObjectType());
                item = setProperties(in, item, dbLayer, true);
                item.setCreated(Date.from(Instant.now()));
                item.setAuditLogId(dbAuditLog.getId());
                JocInventory.insertConfiguration(dbLayer, item, in.getConfiguration());
                if (JocInventory.isFolder(item.getType())) {
                    JocInventory.postFolderEvent(item.getFolder());
                } else {
                    JocInventory.postEvent(item.getFolder());
                }
                JocAuditLog.storeAuditLogDetail(new AuditLogDetail(item.getPath(), item.getType()), session, dbAuditLog);
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

    private DBItemInventoryConfiguration setProperties(ConfigurationObject in, DBItemInventoryConfiguration item, InventoryDBLayer dbLayer,
            boolean isNew) throws Exception {

        if (isNew) {
            InventoryPath path = new InventoryPath(in.getPath(), in.getObjectType());
            item.setPath(path.getPath());
            item.setName(path.getName());
            item.setFolder(path.getFolder());
            item.setValid(false);
            item.setTitle(null);
        }

        if (JocInventory.isFolder(in.getObjectType())) {
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
                ((IInventoryObject) in.getConfiguration()).setVersion(Globals.getStrippedInventoryVersion());

                switch (in.getObjectType()) {
                case WORKINGDAYSCALENDAR:
                case NONWORKINGDAYSCALENDAR:
                    ((ICalendarObject) in.getConfiguration()).setType(CalendarType.fromValue(in.getObjectType().value()));
                    break;
                case LOCK:
                    // TODO tmp solution - should be removed when validation works
                    Lock lock = (Lock) in.getConfiguration();
                    if (lock.getLimit() == null) {
                        lock.setLimit(1);
                    }
                    break;
                case JOBTEMPLATE:
                    JobTemplate jt = (JobTemplate) in.getConfiguration();
                    jt.setHash(null);
                    break;
                case SCHEDULE:
                    Schedule schedule = (Schedule) in.getConfiguration();
                    Predicate<OrderParameterisation> requestHasPositionSettings = o -> o.getPositions() != null && (o.getPositions()
                            .getStartPosition() != null || (o.getPositions().getEndPositions() != null && !o.getPositions().getEndPositions()
                                    .isEmpty()));
                    if (schedule.getOrderParameterisations() != null && schedule.getOrderParameterisations().parallelStream().anyMatch(
                            requestHasPositionSettings)) {
                        boolean hasManagePositionsPermission = Proxies.getControllerDbInstances().keySet().parallelStream().anyMatch(
                                availableController -> getControllerPermissions(availableController, getAccessToken()).getOrders()
                                        .getManagePositions());
                        if (!hasManagePositionsPermission) {
                            throw new JocException(new JocError("Access denied for setting start-/endpositions"));
                        }
                    }
                    break;
                default:
                    break;
                }
                validate(item, in, dbLayer);
                ReferenceValidator.validate(item.getName(), in.getObjectType(), in.getConfiguration(), dbLayer, getAccessToken());
            }
        }

        item.setDeployed(false);
        item.setReleased(false);
        item.setModified(Date.from(Instant.now()));
        return item;
    }

    private void validate(DBItemInventoryConfiguration item, ConfigurationObject in, InventoryDBLayer dbLayer) {

        try {
            item.setContent(JocInventory.toString(in.getConfiguration()));
            Validator.validate(in.getObjectType(), in.getConfiguration(), dbLayer, null);
            item.setValid(true);
        } catch (Throwable e) {
            item.setValid(false);
            in.setInvalidMsg(e.getMessage());
            // LOGGER.warn(String.format("[invalid][client valid=%s][%s] %s", in.getValid(), in.getConfiguration().toString(), e.toString()));
        }
    }

}
