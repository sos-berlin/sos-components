package com.sos.joc.dailyplan.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.dailyplan.db.DBLayerDailyPlanProjections;
import com.sos.joc.dailyplan.resource.IDailyPlanDayProjectionResource;
import com.sos.joc.db.dailyplan.DBItemDailyPlanProjection;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.projections.ProjectionsDayRequest;
import com.sos.joc.model.dailyplan.projections.ProjectionsDayResponse;
import com.sos.joc.model.dailyplan.projections.items.meta.MetaItem;
import com.sos.joc.model.dailyplan.projections.items.year.DateItem;
import com.sos.joc.model.dailyplan.projections.items.year.DatePeriodItem;
import com.sos.joc.model.dailyplan.projections.items.year.MonthItem;
import com.sos.joc.model.security.configuration.permissions.JocPermissions;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanDayProjectionImpl extends JOCResourceImpl implements IDailyPlanDayProjectionResource {

    @Override
    public JOCDefaultResponse dateProjection(String accessToken, byte[] filterBytes) {
        return dayProjection(accessToken, filterBytes);
    }

    @Override
    public JOCDefaultResponse dayProjection(String accessToken, byte[] filterBytes) {

        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ProjectionsDayRequest.class);
            ProjectionsDayRequest in = Globals.objectMapper.readValue(filterBytes, ProjectionsDayRequest.class);

            boolean noControllerAvailable = Proxies.getControllerDbInstances().isEmpty();
            boolean permitted = true;
            Set<String> allowedControllers = Collections.emptySet();
            if (!noControllerAvailable) {
                Stream<String> controllerIds = Proxies.getControllerDbInstances().keySet().stream();
                if (in.getControllerIds() != null && !in.getControllerIds().isEmpty()) {
                    controllerIds = controllerIds.filter(availableController -> in.getControllerIds().contains(availableController));
                }
                allowedControllers = controllerIds.filter(availableController -> getControllerPermissions(availableController, accessToken)
                        .getOrders().getView()).collect(Collectors.toSet());
                permitted = !allowedControllers.isEmpty();
            }
            if (permitted) {
                JocPermissions perms = getJocPermissions(accessToken);
                permitted = perms.getCalendars().getView() || perms.getDailyPlan().getView();
            }

            JOCDefaultResponse jocDefaultResponse = initPermissions(null, permitted);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            DBLayerDailyPlanProjections dbLayer = new DBLayerDailyPlanProjections(session);
            List<DBItemDailyPlanProjection> items = dbLayer.getProjections(Collections.singletonList(DailyPlanProjectionsImpl.getMonth(in
                    .getDate())));
            dbLayer.close();
            session = null;

            Set<String> permittedSchedules = new HashSet<>();
            ProjectionsDayResponse entity = new ProjectionsDayResponse();
            entity.setNumOfPeriods(0);
            Boolean nonPeriods = in.getWithoutStartTime();
            Optional<DBItemDailyPlanProjection> metaOpt = Optional.empty();
            Optional<MetaItem> metaContentOpt = Optional.empty();
            
            if (items != null) {
                Set<String> scheduleNames = Collections.emptySet();

                if (items.isEmpty()) {
                    throw DailyPlanProjectionsImpl.getDBMissingDataException();
                }
                for (DBItemDailyPlanProjection item : items) {
                    if (!item.isMeta()) {

                        MonthItem mi = Globals.objectMapper.readValue(item.getContent(), MonthItem.class);
                        DateItem d = mi.getAdditionalProperties().get(in.getDate());
                        if (d != null) {
                            entity.setPeriods(d.getPeriods());
                            entity.setPlanned(d.getPlanned());
                            scheduleNames = d.getPeriods().stream().map(DatePeriodItem::getSchedule).filter(Objects::nonNull).map(
                                    JocInventory::pathToName).collect(Collectors.toSet());
                        }
                    }
                }

                if (!scheduleNames.isEmpty()) {
                    Optional<Set<String>> scheduleNamesOpt = DailyPlanProjectionsImpl.getNamesOptional(in.getSchedulePaths());
                    Optional<Set<String>> workflowNamesOpt = DailyPlanProjectionsImpl.getNamesOptional(in.getWorkflowPaths());
                    Optional<Set<String>> nonPeriodScheduleNamesOpt = Optional.empty();
                    
                    if (!nonPeriods) {
                        if (scheduleNamesOpt.isPresent()) {
                            Set<String> scheduleNames1 = scheduleNamesOpt.get();
                            scheduleNames1.retainAll(scheduleNames);
                            scheduleNamesOpt = Optional.of(scheduleNames1);
                        } else {
                            scheduleNamesOpt = Optional.of(scheduleNames);
                        }
                    } else {
                        nonPeriodScheduleNamesOpt = Optional.of(scheduleNames);
                    }

                    metaOpt = items.stream().filter(DBItemDailyPlanProjection::isMeta).findAny();
                    metaContentOpt = metaOpt.map(m -> {
                        try {
                            return Globals.objectMapper.readValue(m.getContent(), MetaItem.class);
                        } catch (Exception e) {
                            throw new DBInvalidDataException(e);
                        }
                    });
                    final boolean unPermittedSchedulesExist = DailyPlanProjectionsImpl.setPermittedSchedules(metaContentOpt, allowedControllers,
                            scheduleNamesOpt, in.getScheduleFolders(), nonPeriodScheduleNamesOpt, workflowNamesOpt, in.getWorkflowFolders(),
                            permittedSchedules, folderPermissions);
                    if (!nonPeriods) {
                        if (unPermittedSchedulesExist) {
                            entity.getPeriods().removeIf(p -> !permittedSchedules.contains(p.getSchedule()));
                        }
                        entity.setNumOfPeriods(entity.getPeriods().size());
                    } else {
                        entity.setPeriods(null);
                    }
                }

            }

            metaOpt.ifPresent(meta -> entity.setSurveyDate(meta.getCreated()));
            entity.setDeliveryDate(Date.from(Instant.now()));
            metaContentOpt.ifPresent(mc -> entity.setMeta(mc));
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (DBMissingDataException e) {
            ProblemHelper.postMessageAsHintIfExist(e.getMessage(), accessToken, getJocError(), null);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatus434JSError(e);
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
