package com.sos.webservices.order.impl;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanSubmissionsFilter;
import com.sos.js7.order.initiator.db.DBLayerSchedules;
import com.sos.js7.order.initiator.db.FilterSchedules;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.initiator.model.ScheduleSelector;
import com.sos.webservices.order.initiator.model.SchedulesList;
import com.sos.webservices.order.initiator.model.SchedulesSelector;
import com.sos.webservices.order.resource.ISchedulesResource;

@Path("schedules")
public class SchedulesImpl extends JOCResourceImpl implements ISchedulesResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulesImpl.class);
    private static final String API_CALL = "./schedules";

    @Override
    public JOCDefaultResponse postSchedules(String accessToken, byte[] filterBytes) {
        SOSHibernateSession sosHibernateSession = null;
        LOGGER.debug("reading list of schedules");
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanSubmissionsFilter.class);
            ScheduleSelector scheduleSelector = Globals.objectMapper.readValue(filterBytes, ScheduleSelector.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions(scheduleSelector.getControllerId(), getJocPermissions(accessToken).getDailyPlan()
                    .getView());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            if (scheduleSelector.getSelector() == null) {
                Folder root = new Folder();
                root.setFolder("/");
                root.setRecursive(true);
                scheduleSelector.setSelector(new SchedulesSelector());
                scheduleSelector.getSelector().setFolders(new ArrayList<Folder>());
                scheduleSelector.getSelector().getFolders().add(root);
            }

            SchedulesList schedulesList = new SchedulesList();
            schedulesList.setSchedules(new ArrayList<Schedule>());
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            DBLayerSchedules dbLayerSchedules = new DBLayerSchedules(sosHibernateSession);
            FilterSchedules filterSchedules = new FilterSchedules();

            if (scheduleSelector.getSelector().getSchedulePaths() != null) {
                if (scheduleSelector.getSelector().getScheduleNames() == null) {
                    scheduleSelector.getSelector().setScheduleNames(new ArrayList<String>());
                }
                for (String path : scheduleSelector.getSelector().getSchedulePaths()) {
                    String name = Paths.get(path).getFileName().toString();
                    String folder = Paths.get(path).getParent().toString();
                    try {
                        checkFolderPermissions(folder);
                        scheduleSelector.getSelector().getScheduleNames().add(name);
                    } catch (JocFolderPermissionsException e) {
                        LOGGER.debug("Folder permission for " + folder + " is missing. Schedule" + name + " will not be added ");
                    }

                }
            }
            if (scheduleSelector.getSelector().getWorkflowPaths() != null) {
                if (scheduleSelector.getSelector().getWorkflowNames() == null) {
                    scheduleSelector.getSelector().setWorkflowNames(new ArrayList<String>());
                }

                for (String path : scheduleSelector.getSelector().getWorkflowPaths()) {
                    String name = Paths.get(path).getFileName().toString();
                    String folder = Paths.get(path).getParent().toString();
                    try {
                        checkFolderPermissions(folder);
                        scheduleSelector.getSelector().getWorkflowNames().add(name);
                    } catch (JocFolderPermissionsException e) {
                        LOGGER.debug("Folder permission for " + folder + " is missing. Workflow" + name + " will not be added ");
                    }
                }
            }
            filterSchedules.setListOfControllerIds(scheduleSelector.getSelector().getControllerIds());
            filterSchedules.addControllerId(scheduleSelector.getControllerId());
            filterSchedules.setListOfScheduleNames(scheduleSelector.getSelector().getScheduleNames());
            List<Folder> folders = null;
            if (scheduleSelector.getSelector().getFolders() != null) {
                folders = new ArrayList<Folder>();
                for (Folder folder : scheduleSelector.getSelector().getFolders()) {
                    try {
                        checkFolderPermissions(folder.getFolder());
                        folders.add(folder);
                    } catch (JocFolderPermissionsException e) {
                        LOGGER.debug("Folder permission for " + folder + " is missing. Folder" + folder.getFolder() + " will not be added ");
                    }
                }
            }
            filterSchedules.setListOfFolders(folders);
            filterSchedules.setListOfWorkflowNames(scheduleSelector.getSelector().getWorkflowNames());

            List<DBItemInventoryReleasedConfiguration> listOfSchedules = dbLayerSchedules.getSchedules(filterSchedules, 0);
            for (DBItemInventoryReleasedConfiguration dbItemInventoryConfiguration : listOfSchedules) {
                if (dbItemInventoryConfiguration.getContent() != null) {
                    schedulesList.getSchedules().add(dbItemInventoryConfiguration.getSchedule());
                }
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(schedulesList));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

}
