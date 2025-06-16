package com.sos.joc.dailyplan.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.dailyplan.common.ProjectionsImpl;
import com.sos.joc.dailyplan.common.ScheduleOrderCounter;
import com.sos.joc.dailyplan.db.DBLayerDailyPlanProjections;
import com.sos.joc.dailyplan.resource.IDailyPlanProjectionsDayResource;
import com.sos.joc.db.dailyplan.DBItemDailyPlanProjection;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
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
public class DailyPlanProjectionsDayImpl extends ProjectionsImpl implements IDailyPlanProjectionsDayResource {

    @Override
    public JOCDefaultResponse dateProjection(String accessToken, byte[] filterBytes) {
        return dayProjection(accessToken, filterBytes);
    }

    @Override
    public JOCDefaultResponse dayProjection(String accessToken, byte[] filterBytes) {

        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.DAILYPLAN);
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
                allowedControllers = controllerIds.filter(availableController -> getBasicControllerPermissions(availableController, accessToken)
                        .getOrders().getView()).collect(Collectors.toSet());
                permitted = !allowedControllers.isEmpty();
            }
            if (permitted) {
                JocPermissions perms = getBasicJocPermissions(accessToken);
                permitted = perms.getCalendars().getView() || perms.getDailyPlan().getView();
            }

            JOCDefaultResponse jocDefaultResponse = initPermissions(null, permitted);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            DBLayerDailyPlanProjections dbLayer = new DBLayerDailyPlanProjections(session);
            List<DBItemDailyPlanProjection> items = dbLayer.getProjections(Collections.singletonList(getMonth(in.getDate())));
            dbLayer.close();
            session = null;

            boolean invertedProjection = in.getWithoutStartTime() != null && in.getWithoutStartTime();

            Set<String> permittedSchedules = new HashSet<>();
            ProjectionsDayResponse entity = new ProjectionsDayResponse();
            entity.setNumOfOrders(0);
            Optional<DBItemDailyPlanProjection> metaOpt = Optional.empty();
            Optional<MetaItem> metaContentOpt = Optional.empty();

            if (items != null) {
                if (items.isEmpty()) {
                    throw getDBMissingDataException();
                }

                metaOpt = items.stream().filter(DBItemDailyPlanProjection::isMeta).findAny();
                metaContentOpt = metaOpt.map(m -> {
                    try {
                        return Globals.objectMapper.readValue(m.getContent(), MetaItem.class);
                    } catch (Exception e) {
                        throw new DBInvalidDataException(e);
                    }
                });
                // use the original metadata for this calculation, as totalOrdes will be removed later(due to meta recreated)
                Map<String, ScheduleOrderCounter> scheduleOrderCounter = mapScheduleOrderCounter(metaContentOpt, allowedControllers);

                Set<String> scheduleNames = Collections.emptySet();
                for (DBItemDailyPlanProjection item : items) {
                    if (!item.isMeta()) {
                        MonthItem mi = Globals.objectMapper.readValue(item.getContent(), MonthItem.class);
                        DateItem d = mi.getAdditionalProperties().get(in.getDate());
                        if (d != null) {
                            entity.setPeriods(d.getPeriods());
                            entity.setPlanned(d.getPlanned());

                            scheduleNames = d.getPeriods().stream().map(DatePeriodItem::getSchedule).filter(Objects::nonNull).map(
                                    JocInventory::pathToName).collect(Collectors.toSet());

                            // remove from Meta "ExcludedFromProjection" entries if a day is not a "planned" day
                            getSchedulesExcludedFromProjection(metaContentOpt, !isPlanned(entity));
                        }
                    }
                }

                if (!scheduleNames.isEmpty()) {
                    Optional<Set<String>> scheduleNamesOpt = getNamesOptional(in.getSchedulePaths());
                    Optional<Set<String>> workflowNamesOpt = getNamesOptional(in.getWorkflowPaths());
                    Optional<Set<String>> nonPeriodScheduleNamesOpt = Optional.empty();

                    if (invertedProjection) {
                        nonPeriodScheduleNamesOpt = Optional.of(scheduleNames);
                    } else {
                        if (scheduleNamesOpt.isPresent()) {
                            Set<String> scheduleNames1 = scheduleNamesOpt.get();
                            scheduleNames1.retainAll(scheduleNames);
                            scheduleNamesOpt = Optional.of(scheduleNames1);
                        } else {
                            scheduleNamesOpt = Optional.of(scheduleNames);
                        }
                    }

                    final boolean unPermittedSchedulesExist = setPermittedSchedules(metaContentOpt, allowedControllers, scheduleNamesOpt, in
                            .getScheduleFolders(), nonPeriodScheduleNamesOpt, workflowNamesOpt, in.getWorkflowFolders(), permittedSchedules,
                            folderPermissions);

                    if (invertedProjection) {
                        entity.setNonPeriods(permittedSchedules.stream().map(s -> {
                            DatePeriodItem dpi = new DatePeriodItem();
                            dpi.setSchedule(s);
                            return dpi;
                        }).filter(Objects::nonNull).collect(Collectors.toList()));
                        entity.setNumOfNonPeriods(entity.getNonPeriods().size());

                        // remove Periods
                        entity.setPeriods(null);
                        entity.setNumOfOrders(null);

                    } else {
                        if (unPermittedSchedulesExist) {
                            entity.getPeriods().removeIf(p -> !permittedSchedules.contains(p.getSchedule()));
                        }
                        setDateItemNumOfOrders(entity, scheduleOrderCounter);

                        // remove NonPeriods
                        entity.setNonPeriods(null);
                        entity.setNumOfNonPeriods(null);
                    }
                }

            }

            metaOpt.ifPresent(meta -> entity.setSurveyDate(meta.getCreated()));
            entity.setDeliveryDate(Date.from(Instant.now()));
            // if (entity.getPlanned() == null || !entity.getPlanned()) {
            metaContentOpt.ifPresent(mc -> entity.setMeta(mc));
            // }
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
