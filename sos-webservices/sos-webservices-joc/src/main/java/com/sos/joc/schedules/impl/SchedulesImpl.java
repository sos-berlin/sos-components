package com.sos.joc.schedules.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.dailyplan.DailyPlanRunner;
import com.sos.joc.dailyplan.common.DailyPlanSchedule;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.db.DBBeanReleasedSchedule2DeployedWorkflow;
import com.sos.joc.dailyplan.db.DBLayerSchedules;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.schedules.resource.ISchedulesResource;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.initiator.model.ScheduleSelector;
import com.sos.webservices.order.initiator.model.SchedulesList;
import com.sos.webservices.order.initiator.model.SchedulesSelector;

@Path(WebservicePaths.SCHEDULES)
public class SchedulesImpl extends JOCOrderResourceImpl implements ISchedulesResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulesImpl.class);

    @Override
    public JOCDefaultResponse postSchedules(String accessToken, byte[] filterBytes) {
        LOGGER.debug("reading list of schedules");
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ScheduleSelector.class);
            ScheduleSelector in = Globals.objectMapper.readValue(filterBytes, ScheduleSelector.class);

            this.checkRequiredParameter("controllerId", in.getControllerId());

            if (in.getSelector() == null) {
                Folder root = new Folder();
                root.setFolder("/");
                root.setRecursive(true);
                in.setSelector(new SchedulesSelector());
                in.getSelector().setFolders(new ArrayList<Folder>());
                in.getSelector().getFolders().add(root);
            }

            String controllerId = in.getControllerId();
            Set<String> allowedControllers = Collections.emptySet();
            boolean permitted = false;
            if (controllerId == null || controllerId.isEmpty()) {
                controllerId = "";
                allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> getControllerPermissions(
                        availableController, accessToken).getOrders().getView()).collect(Collectors.toSet());
                permitted = !allowedControllers.isEmpty();
                if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                    allowedControllers = Collections.emptySet();
                }
            } else {
                allowedControllers = Collections.singleton(controllerId);
                permitted = getControllerPermissions(controllerId, accessToken).getOrders().getView();
            }

            JOCDefaultResponse response = initPermissions(controllerId, permitted);
            if (response != null) {
                return response;
            }

            Set<String> scheduleSingles = null;
            Set<String> workflowSingles = null;
            if (in.getSelector().getSchedulePaths() != null) {
                scheduleSingles = in.getSelector().getSchedulePaths().stream().distinct().collect(Collectors.toSet());
            }
            if (in.getSelector().getWorkflowPaths() != null) {
                workflowSingles = in.getSelector().getWorkflowPaths().stream().distinct().collect(Collectors.toSet());
            }
            final Set<Folder> permittedFolders = addPermittedFolder(in.getSelector().getFolders());
            Map<String, Boolean> checkedFolders = new HashMap<>();
            Collection<DailyPlanSchedule> dailyPlanSchedules = getSchedules(controllerId, scheduleSingles, workflowSingles, permittedFolders,
                    checkedFolders);

            SchedulesList answer = new SchedulesList();
            answer.setSchedules(new ArrayList<Schedule>());

            if (dailyPlanSchedules != null && dailyPlanSchedules.size() > 0) {
                for (DailyPlanSchedule dailyPlanSchedule : dailyPlanSchedules) {
                    answer.getSchedules().add(dailyPlanSchedule.getSchedule());
                }
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private Collection<DailyPlanSchedule> getSchedules(String controllerId, Set<String> scheduleSingles, Set<String> workflowSingles,
            Set<Folder> permittedFolders, Map<String, Boolean> checkedFolders) throws IOException, SOSHibernateException {

        SOSHibernateSession session = null;
        boolean hasSelectedSchedules = scheduleSingles != null && scheduleSingles.size() > 0;
        boolean hasSelectedWorkflows = workflowSingles != null && workflowSingles.size() > 0;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            DBLayerSchedules dbLayer = new DBLayerSchedules(session);

            // selected schedules
            List<DBBeanReleasedSchedule2DeployedWorkflow> scheduleItems = null;
            if (!hasSelectedSchedules && !hasSelectedWorkflows) {
                scheduleItems = dbLayer.getReleasedSchedule2DeployedWorkflows(controllerId, permittedFolders);
            } else {
                // selected schedules
                if (hasSelectedSchedules) {
                    scheduleItems = dbLayer.getReleasedSchedule2DeployedWorkflows(controllerId, permittedFolders, scheduleSingles, true);
                }
                // selected workflows
                if (hasSelectedWorkflows) {
                    List<DBBeanReleasedSchedule2DeployedWorkflow> workflowItems = dbLayer.getReleasedSchedule2DeployedWorkflows(controllerId,
                            permittedFolders, workflowSingles, false);
                    if (workflowItems != null && workflowItems.size() > 0) {
                        if (scheduleItems == null || scheduleItems.size() == 0) {
                            scheduleItems = workflowItems;
                        } else {
                            scheduleItems.addAll(workflowItems);
                        }
                    }
                }
            }
            session.close();
            session = null;

            if (scheduleItems == null || scheduleItems.size() == 0) {
                return new ArrayList<DailyPlanSchedule>();
            }

            return new DailyPlanRunner(null).convert(scheduleItems, permittedFolders, checkedFolders, false);
        } finally {
            Globals.disconnect(session);
        }
    }

}
