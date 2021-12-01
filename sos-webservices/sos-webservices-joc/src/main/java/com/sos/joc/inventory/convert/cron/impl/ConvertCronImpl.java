package com.sos.joc.inventory.convert.cron.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.Path;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.CalendarType;
import com.sos.inventory.model.lock.Lock;
import com.sos.joc.Globals;
import com.sos.joc.classes.CheckJavaVariableName;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JocInventory.InventoryPath;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.inventory.convert.cron.CronUtils;
import com.sos.joc.inventory.convert.cron.resource.IConvertCronResource;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.ICalendarObject;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.convert.ConvertCronFilter;
import com.sos.joc.model.inventory.workflow.WorkflowEdit;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.initiator.model.ScheduleEdit;

@Path("inventory")
public class ConvertCronImpl extends JOCResourceImpl implements IConvertCronResource {

    private static final String API_CALL = "./inventory/convert/cron";
    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertCronImpl.class);

    @Override
	public JOCDefaultResponse postConvertCron(String xAccessToken, 
			FormDataBodyPart body, 
			String folder,
            String calendarName,
            String agentName, 
            Boolean systemCrontab,
			String timeSpent,
			String ticketLink,
			String comment) throws Exception {
        AuditParams auditLog = new AuditParams();
        auditLog.setComment(comment);
        auditLog.setTicketLink(ticketLink);
        try {
            auditLog.setTimeSpent(Integer.valueOf(timeSpent));
        } catch (Exception e) {}
        ConvertCronFilter filter = new ConvertCronFilter();
        filter.setAuditLog(auditLog);
        filter.setFolder(folder);
        filter.setCalendarName(calendarName);
        filter.setAgentName(agentName);
        filter.setSystemCrontab(systemCrontab);
		return postConvertCron(xAccessToken, body, filter, auditLog);
	}

	private JOCDefaultResponse postConvertCron(String xAccessToken, FormDataBodyPart body, ConvertCronFilter filter, AuditParams auditLog)
	        throws Exception {
        InputStream stream = null;
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, null, xAccessToken); 
            JsonValidator.validate(Globals.objectMapper.writeValueAsBytes(filter), ConvertCronFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getInventory().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            DBItemJocAuditLog dbAuditItem = storeAuditLog(filter.getAuditLog(), CategoryType.INVENTORY);
            String account = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname();
            stream = body.getEntityAs(InputStream.class);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            if (filter.getFolder() == null || filter.getFolder().isEmpty()) {
                //default folder
                filter.setFolder("/");
            }
            
            // process uploaded cron file
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            String timezone = getTimezoneFromProfileConfiguration(hibernateSession, account);
            InventoryDBLayer invDbLayer = new InventoryDBLayer(hibernateSession);
            DBItemInventoryConfiguration calDbItem = invDbLayer.getCalendarByName(filter.getCalendarName());
            Calendar cal = Globals.objectMapper.readValue(calDbItem.getContent(), Calendar.class);
            cal.setName(calDbItem.getName());
            cal.setPath(calDbItem.getPath());
            Map<WorkflowEdit, ScheduleEdit> scheduledWorkflows = CronUtils.cronFile2Workflows(invDbLayer, bufferedReader, cal, filter.getAgentName(), timezone,
                    filter.getSystemCrontab());
            Set<ConfigurationObject> objects = new HashSet<ConfigurationObject>();
            for (Map.Entry<WorkflowEdit, ScheduleEdit> entry : scheduledWorkflows.entrySet()) {
                entry.getKey().setPath(Paths.get(filter.getFolder()).resolve(entry.getKey().getName()).toString().replace('\\', '/'));
                entry.getKey().setObjectType(ConfigurationType.WORKFLOW);
                entry.getValue().setPath(Paths.get(filter.getFolder()).resolve(entry.getValue().getName()).toString().replace('\\', '/'));
                entry.getValue().setObjectType(ConfigurationType.SCHEDULE);
                store(invDbLayer, entry.getKey(), dbAuditItem);
                objects.add(entry.getKey());
                store(invDbLayer, entry.getValue(), dbAuditItem);
                objects.add(entry.getValue());
            }
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {}
        }
	}
	
	private void store(InventoryDBLayer dbLayer, ConfigurationObject in, DBItemJocAuditLog dbAuditLog) throws Exception{
        DBItemInventoryConfiguration item;
        checkRequiredParameter("path", in.getPath());
        checkRequiredParameter("objectType", in.getObjectType());
        java.nio.file.Path path = JocInventory.normalizePath(in.getPath());

        // Check Java variable name rules
        for (int i = 0; i < path.getNameCount(); i++) {
            if (i == path.getNameCount() - 1) {
                CheckJavaVariableName.test("name", path.getName(i).toString());
            } else {
                CheckJavaVariableName.test("folder", path.getName(i).toString());
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
        
        // mkdirs if necessary
        JocInventory.makeParentDirs(dbLayer, path.getParent());
        item = new DBItemInventoryConfiguration();
        item.setType(in.getObjectType());
        item = setProperties(in, item, dbLayer, true);
        item.setCreated(Date.from(Instant.now()));
        item.setAuditLogId(dbAuditLog.getId());
        item.setContent(Globals.objectMapper.writeValueAsString(in.getConfiguration()));
        JocInventory.insertConfiguration(dbLayer, item, in.getConfiguration());
        if (JocInventory.isFolder(item.getType())) {
            JocInventory.postFolderEvent(item.getFolder());
        } else {
            JocInventory.postEvent(item.getFolder());
        }
        JocAuditLog.storeAuditLogDetail(new AuditLogDetail(item.getPath(), item.getType()), dbLayer.getSession(), dbAuditLog);
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
                default:
                    break;
                }
                validate(item, in, dbLayer);
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
        }
    }

	private String getTimezoneFromProfileConfiguration (SOSHibernateSession session, String account) {
        JocConfigurationDbLayer confDbLayer = new JocConfigurationDbLayer(session);
        JocConfigurationFilter confFilter = new JocConfigurationFilter();
        confFilter.setAccount(account);
        confFilter.setConfigurationType(com.sos.joc.model.configuration.ConfigurationType.PROFILE.toString());
        String timezone = null;
        JsonReader jsonReader = null;
        try {
            List<DBItemJocConfiguration> confs = confDbLayer.getJocConfigurations(confFilter, 1);
            jsonReader = Json.createReader(new StringReader(confs.get(0).getConfigurationItem()));
            JsonObject json = jsonReader.readObject();
            timezone = json.getString("zone", "Etc/UTC");
        } catch(Exception e) {
            confFilter.setAccount(ClusterSettings.getDefaultProfileAccount(Globals.getConfigurationGlobalsJoc()));
            try {
                List<DBItemJocConfiguration> confs = confDbLayer.getJocConfigurations(confFilter, 1);
                jsonReader.close();
                jsonReader = Json.createReader(new StringReader(confs.get(0).getConfigurationItem()));
                JsonObject json = jsonReader.readObject();
                timezone = json.getString("zone", "Etc/UTC");
            } catch(Exception ex) {
                LOGGER.warn("could not determine timezone from profile");
            } finally {
                jsonReader.close();
            }
        } finally {
            jsonReader.close();
        }
        return timezone;
	}
}
