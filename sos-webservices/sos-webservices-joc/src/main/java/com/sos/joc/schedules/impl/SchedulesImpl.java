package com.sos.joc.schedules.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.dailyplan.common.FolderPermissionEvaluator;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.db.DBLayerSchedules;
import com.sos.joc.dailyplan.db.FilterSchedules;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.schedules.resource.ISchedulesResource;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.initiator.model.ScheduleSelector;
import com.sos.webservices.order.initiator.model.SchedulesList;
import com.sos.webservices.order.initiator.model.SchedulesSelector;

@Path("schedules")
public class SchedulesImpl extends JOCOrderResourceImpl implements ISchedulesResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulesImpl.class);
    private static final String API_CALL = "./schedules";

    @Override
    public JOCDefaultResponse postSchedules(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        LOGGER.debug("reading list of schedules");
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ScheduleSelector.class);
            ScheduleSelector in = Globals.objectMapper.readValue(filterBytes, ScheduleSelector.class);

            if (in.getSelector() == null) {
                Folder root = new Folder();
                root.setFolder("/");
                root.setRecursive(true);
                in.setSelector(new SchedulesSelector());
                in.getSelector().setFolders(new ArrayList<Folder>());
                in.getSelector().getFolders().add(root);
            }

            Set<String> allowedControllers = getAllowedControllersOrdersView(in.getControllerId(), in.getSelector().getControllerIds(), accessToken)
                    .stream().filter(availableController -> getControllerPermissions(availableController, accessToken).getWorkflows().getView())
                    .collect(Collectors.toSet());

            boolean permitted = !allowedControllers.isEmpty();

            JOCDefaultResponse jocDefaultResponse = initPermissions(null, permitted);

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            FolderPermissionEvaluator evaluator = new FolderPermissionEvaluator();
            evaluator.setScheduleFolders(in.getSelector().getFolders());
            evaluator.setSchedulePaths(in.getSelector().getSchedulePaths());
            evaluator.setWorkflowPaths(in.getSelector().getWorkflowPaths());

            SchedulesList answer = new SchedulesList();
            answer.setSchedules(new ArrayList<Schedule>());

            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerSchedules dbLayer = new DBLayerSchedules(session);
            for (String controllerId : allowedControllers) {
                folderPermissions.setSchedulerId(controllerId);
                evaluator.getPermittedNames(folderPermissions, controllerId, null);

                if (evaluator.isHasPermission()) {
                    FilterSchedules filter = new FilterSchedules();
                    filter.addControllerId(controllerId);
                    filter.setScheduleNames(evaluator.getPermittedScheduleNames());
                    filter.setWorkflowNames(evaluator.getPermittedWorkflowNames());
                    filter.setFolders(in.getSelector().getFolders());

                    List<DBItemInventoryReleasedConfiguration> items = dbLayer.getSchedules(filter, 0);
                    for (DBItemInventoryReleasedConfiguration item : items) {
                        if (item.getContent() != null) {
                            permitted = true;
                            if (folderPermissions.getListOfFolders(in.getControllerId()).size() > 0) {
                                String folder = getParent(item.getPath());
                                permitted = folderPermissions.isPermittedForFolder(folder);
                            }
                            if (permitted) {
                                answer.getSchedules().add(item.getSchedule());
                            }
                        }
                    }
                }
            }
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }

    }

}
