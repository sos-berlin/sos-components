package com.sos.joc.inventory.impl;

import java.net.URI;
import java.nio.charset.StandardCharsets;
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
import com.sos.joc.model.common.IConfigurationObject;
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

            // session.beginTransaction();
            DBItemInventoryConfiguration config;
            try {
                config = JocInventory.getConfiguration(dbLayer, in.getId(), in.getPath(), in.getObjectType(), folderPermissions);
                config = setProperties(in, config, false);
                session.update(config);

            } catch (DBMissingDataException e) {
                config = new DBItemInventoryConfiguration();
                config.setType(in.getObjectType());
                config = setProperties(in, config, true);
                config.setCreated(Date.from(Instant.now()));
                createAuditLog(config, in.getObjectType());
                session.save(config);
            }
            // if (in.getId() != null && in.getId() > 0) {
            // config = dbLayer.getConfiguration(in.getId());
            // } else if (JocInventory.isCalendar(in.getObjectType())) {
            // config = dbLayer.getCalendar(in.getPath());
            // } else {
            // config = dbLayer.getConfiguration(in.getPath(), in.getObjectType().intValue());
            // }
            //
            // if (config == null) {
            // config = new DBItemInventoryConfiguration();
            // config.setType(in.getObjectType());
            // config = setProperties(in, config, true);
            // config.setCreated(new Date());
            // createAuditLog(config, in.getObjectType());
            // session.save(config);
            // } else {
            // TODO
            // if (1 == 2 && in.getConfiguration() != null) {
            // if (in.getValid() != null) {
            // if (!in.getValid().equals(config.getValid())) {
            // config.setValid(in.getValid());
            // config.setDeployed(false);
            // config.setModified(new Date());
            // session.update(config);
            // }
            // }
            // session.commit();
            //
            // ConfigurationObject item = new ConfigurationObject();
            // item.setId(config.getId());
            // item.setDeliveryDate(new Date());
            // item.setPath(config.getPath());
            // item.setConfigurationDate(config.getModified());
            // item.setObjectType(JocInventory.getType(config.getType()));
            // item.setValid(config.getValid());
            // item.setDeployed(config.getDeployed());
            // item.setReleased(config.getReleased());
            // return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(item));
            // }

            // config = setProperties(in, config, false);
            // session.update(config);
            // }

            switch (in.getObjectType()) {
            // case WORKFLOW:
            // // Workflow w = (Workflow) in.getConfiguration();
            // break;
            // case JOBCLASS:
            // JobClass jobClass = (JobClass) in.getConfiguration();
            // Integer maxProcesses = jobClass.getMaxProcesses();
            // if (maxProcesses == null) {
            // maxProcesses = 30;
            // }
            // DBItemInventoryJobClass jc = dbLayer.getJobClass(config.getId());
            // if (jc == null) {
            // jc = new DBItemInventoryJobClass();
            // jc.setCid(config.getId());
            // jc.setMaxProcesses(maxProcesses);
            // session.save(jc);
            // } else if (jc.getMaxProcesses() != maxProcesses) {
            // jc.setMaxProcesses(maxProcesses);
            // session.update(jc);
            // }
            // break;
            // case AGENTCLUSTER:
            // DBItemInventoryAgentCluster ac = dbLayer.getAgentCluster(config.getId());
            // if (ac == null) {
            // ac = new DBItemInventoryAgentCluster();
            // ac.setCid(config.getId());
            // ac.setNumberOfAgents(1L); // TODO
            // ac.setSchedulingType(AgentClusterSchedulingType.FIXED_PRIORITY); // TODO
            // session.save(ac);
            // } else {
            // // ac.setNumberOfAgents(1L); // TODO
            // // ac.setSchedulingType(AgentClusterSchedulingType.FIXED_PRIORITY); // TODO
            // // session.update(ac);
            // }
            // break;
            // case LOCK:
            // Lock lock = (Lock) in.getConfiguration();
            // LockType lockType = LockType.EXCLUSIVE; // TODO a lock is not exclusive but it could be used exclusive by a job
            // Integer maxNonExclusive = 0;
            //
            // Integer inConfigMaxNonExclusive = lock.getMaxNonExclusive();
            // if (inConfigMaxNonExclusive != null) {
            // maxNonExclusive = inConfigMaxNonExclusive;
            // }
            //
            // if (maxNonExclusive > 0) {
            // lockType = LockType.SHARED;
            // }
            //
            //
            // DBItemInventoryLock l = dbLayer.getLock(config.getId());
            // if (l == null) {
            // l = new DBItemInventoryLock();
            // l.setCid(config.getId());
            // l.setType(lockType);
            // l.setMaxNonExclusive(maxNonExclusive);
            // session.save(l);
            // } else {
            // l.setType(lockType);
            // l.setMaxNonExclusive(maxNonExclusive);
            // session.update(l);
            // }
            // break;
            // case JUNCTION:
            // Junction junction = (Junction) in.getConfiguration();
            // Integer lifeTime = 0;
            // Integer inConfigLifeTime = junction.getLifetime();
            // if (inConfigLifeTime != null) {
            // lifeTime = inConfigLifeTime;
            // }
            //
            // DBItemInventoryJunction j = dbLayer.getJunction(config.getId());
            // if (j == null) {
            // j = new DBItemInventoryJunction();
            // j.setCid(config.getId());
            // j.setLifetime(lifeTime + "");
            // session.save(j);
            // } else {
            // j.setLifetime(lifeTime + "");
            // session.update(j);
            // }
            // break;
            case ORDER:
                break;
            // case WORKINGDAYSCALENDAR:
            // case NONWORKINGDAYSCALENDAR:
            // // Nothing to do
            // break;
            default:
                break;
            }
            // session.commit();

            ConfigurationObject item = new ConfigurationObject();
            item.setId(config.getId());
            item.setDeliveryDate(Date.from(Instant.now()));
            item.setPath(config.getPath());
            item.setConfigurationDate(config.getModified());
            item.setObjectType(JocInventory.getType(config.getType()));
            item.setValid(config.getValid());
            item.setInvalidMsg(in.getInvalidMsg());
            item.setDeployed(false);
            item.setState(ItemStateEnum.DRAFT_IS_NEWER);// TODO

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(item));
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

                IConfigurationObject obj = in.getConfiguration();
                item.setTitle(obj.getTitle());

                // "path" is required in schemas except for JOB and FOLDER
                if (!ConfigurationType.JOB.equals(in.getObjectType())) {
                    obj.setPath(item.getPath());
                }
                if (JocInventory.isCalendar(in.getObjectType())) {
                    ((ICalendarObject) obj).setType(CalendarType.fromValue(in.getObjectType().value()));
                }
                validate(item, in, obj);
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

    private static void validate(DBItemInventoryConfiguration item, ConfigurationObject in, IConfigurationObject obj) {
        try {
            byte[] objBytes = Globals.objectMapper.writeValueAsBytes(obj);
            item.setContent(new String(objBytes, StandardCharsets.UTF_8));
            JsonValidator.validate(objBytes, URI.create(JocInventory.SCHEMA_LOCATION.get(in.getObjectType())));
            item.setValid(true);
        } catch (Throwable e) {
            item.setValid(false);
            in.setInvalidMsg(e.getMessage());
            // LOGGER.warn(String.format("[invalid][client valid=%s][%s] %s", in.getValid(), in.getConfiguration().toString(), e.toString()));
        }
    }

}
