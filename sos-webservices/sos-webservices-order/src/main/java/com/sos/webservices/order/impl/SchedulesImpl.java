package com.sos.webservices.order.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanSubmissionsFilter;
import com.sos.js7.order.initiator.db.DBLayerSchedules;
import com.sos.js7.order.initiator.db.FilterSchedules;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.classes.FolderPermissionEvaluator;
import com.sos.webservices.order.classes.JOCOrderResourceImpl;
import com.sos.webservices.order.initiator.model.ScheduleSelector;
import com.sos.webservices.order.initiator.model.SchedulesList;
import com.sos.webservices.order.initiator.model.SchedulesSelector;
import com.sos.webservices.order.resource.ISchedulesResource;

@Path("schedules")
public class SchedulesImpl extends JOCOrderResourceImpl implements ISchedulesResource {

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

            if (scheduleSelector.getSelector() == null) {
                Folder root = new Folder();
                root.setFolder("/");
                root.setRecursive(true);
                scheduleSelector.setSelector(new SchedulesSelector());
                scheduleSelector.getSelector().setFolders(new ArrayList<Folder>());
                scheduleSelector.getSelector().getFolders().add(root);
            }

            Set<String> allowedControllers = getAllowedControllersOrdersView(scheduleSelector.getControllerId(), scheduleSelector.getSelector()
                    .getControllerIds(), accessToken).stream().filter(availableController -> getControllerPermissions(availableController,
                            accessToken).getWorkflows().getView()).collect(Collectors.toSet());

            boolean permitted = !allowedControllers.isEmpty();

            JOCDefaultResponse jocDefaultResponse = initPermissions(null, permitted);

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            FolderPermissionEvaluator folderPermissionEvaluator = new FolderPermissionEvaluator();
            folderPermissionEvaluator.setListOfScheduleFolders(scheduleSelector.getSelector().getFolders());
            folderPermissionEvaluator.setListOfScheduleNames(scheduleSelector.getSelector().getScheduleNames());
            folderPermissionEvaluator.setListOfSchedulePaths(scheduleSelector.getSelector().getSchedulePaths());
            folderPermissionEvaluator.setListOfWorkflowNames(scheduleSelector.getSelector().getWorkflowNames());
            folderPermissionEvaluator.setListOfWorkflowPaths(scheduleSelector.getSelector().getWorkflowPaths());

            SchedulesList schedulesList = new SchedulesList();
            schedulesList.setSchedules(new ArrayList<Schedule>());
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            DBLayerSchedules dbLayerSchedules = new DBLayerSchedules(sosHibernateSession);
            for (String controllerId : allowedControllers) {
                folderPermissions.setSchedulerId(controllerId);
                folderPermissionEvaluator.getPermittedNames(folderPermissions, controllerId, null);

                if (folderPermissionEvaluator.isHasPermission()) {

                    FilterSchedules filterSchedules = new FilterSchedules();
                    filterSchedules.addControllerId(controllerId);
                    filterSchedules.setListOfScheduleNames(folderPermissionEvaluator.getListOfPermittedScheduleNames());
                    filterSchedules.setListOfWorkflowNames(folderPermissionEvaluator.getListOfPermittedWorkflowNames());
                    filterSchedules.setListOfFolders(scheduleSelector.getSelector().getFolders());

                    List<DBItemInventoryReleasedConfiguration> listOfSchedules = dbLayerSchedules.getSchedules(filterSchedules, 0);
                    for (DBItemInventoryReleasedConfiguration dbItemInventoryConfiguration : listOfSchedules) {
                        if (dbItemInventoryConfiguration.getContent() != null) {
                            permitted = true;
                            if (folderPermissions.getListOfFolders(scheduleSelector.getControllerId()).size() > 0) {
                                String folder = getParent(dbItemInventoryConfiguration.getPath());
                                permitted = folderPermissions.isPermittedForFolder(folder);
                            }
                            if (permitted) {
                                schedulesList.getSchedules().add(dbItemInventoryConfiguration.getSchedule());
                            }
                        }
                    }
                }
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(schedulesList));

        } catch (

        JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

}
