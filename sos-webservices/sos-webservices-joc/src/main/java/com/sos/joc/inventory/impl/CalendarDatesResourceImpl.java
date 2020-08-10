package com.sos.joc.inventory.impl;

import javax.ws.rs.Path;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.ICalendarDatesResource;
import com.sos.joc.model.calendar.Calendar;
import com.sos.joc.model.calendar.CalendarDatesFilter;
import com.sos.joc.model.calendar.Dates;
import com.sos.joc.model.common.JobSchedulerObjectType;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class CalendarDatesResourceImpl extends JOCResourceImpl implements ICalendarDatesResource {

    @Override
    public JOCDefaultResponse read(final String accessToken, final byte[] inBytes) {
        try {
            JsonValidator.validateFailFast(inBytes, CalendarDatesFilter.class);
            CalendarDatesFilter in = Globals.objectMapper.readValue(inBytes, CalendarDatesFilter.class);

            // checkRequiredParameter("objectType", in.getObjectType());
            // checkRequiredParameter("path", in.getPath());// for check permissions
            // in.setPath(Globals.normalizePath(in.getPath()));

            JOCDefaultResponse response = checkPermissions(accessToken, in);
            if (response == null) {
                if (in.getCalendar() == null && in.getPath() == null && in.getId() == null) {
                    throw new Exception("missing calendar input data");
                }
                response = JOCDefaultResponse.responseStatus200(read(in));
            }
            return response;

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private Dates read(CalendarDatesFilter in) throws Exception {
        SOSHibernateSession session = null;

        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            Calendar calendar = null;
            if (in.getId() != null || !SOSString.isEmpty(in.getPath())) {
                DBItemInventoryConfiguration config = getConfiguration(dbLayer, in.getId(), Globals.normalizePath(in.getPath()),
                        JobSchedulerObjectType.CALENDAR);
                calendar = Globals.objectMapper.readValue(config.getContentJoc(), Calendar.class);
            } else {
                calendar = in.getCalendar();
            }

            Dates dates = null;
            FrequencyResolver fr = new FrequencyResolver();
            if (!SOSString.isEmpty(calendar.getBasedOn())) {
                // TODO check calendar.getBasedOn() permissions
                DBItemInventoryConfiguration basedConfig = getConfiguration(dbLayer, null, Globals.normalizePath(calendar.getBasedOn()),
                        JobSchedulerObjectType.CALENDAR);
                Calendar basedCalendar = Globals.objectMapper.readValue(basedConfig.getContentJoc(), Calendar.class);
                if (SOSString.isEmpty(in.getDateFrom())) {
                    in.setDateFrom(fr.getToday());
                }
                dates = fr.resolveRestrictions(basedCalendar, calendar, in.getDateFrom(), in.getDateTo());
            } else {
                // TODO check for today?
                dates = fr.resolve(calendar, in.getDateFrom(), in.getDateTo());
            }
            return dates;
        } catch (Throwable e) {
            if (session != null && session.isTransactionOpened()) {
                Globals.rollback(session);
            }
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private DBItemInventoryConfiguration getConfiguration(InventoryDBLayer dbLayer, Long configId, String path, JobSchedulerObjectType objectType)
            throws Exception {
        dbLayer.getSession().beginTransaction();
        DBItemInventoryConfiguration config = null;
        if (configId != null && configId > 0L) {
            config = dbLayer.getConfiguration(configId);
        }
        if (config == null) {// TODO temp
            config = dbLayer.getConfiguration(path, JocInventory.getType(objectType));
        }
        dbLayer.getSession().commit();

        if (config == null) {
            throw new Exception(String.format("configuration not found: %s", path));
        }
        if (SOSString.isEmpty(config.getContentJoc())) {
            throw new Exception(String.format("[%s][%s]joc configuration is missing %s", config.getId(), config.getPath()));
        }
        return config;
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final CalendarDatesFilter in) throws Exception {
        SOSPermissionJocCockpit permissions = getPermissonsJocCockpit("", accessToken);
        boolean permission = permissions.getJS7Controller().getAdministration().getConfigurations().isEdit();

        JOCDefaultResponse response = init(IMPL_PATH, in, accessToken, "", permission);
        if (response == null && permission && !SOSString.isEmpty(in.getPath())) {
            String path = normalizePath(in.getPath());
            if (!folderPermissions.isPermittedForFolder(getParent(path))) {
                return accessDeniedResponse();
            }
        }
        return response;
    }

}
