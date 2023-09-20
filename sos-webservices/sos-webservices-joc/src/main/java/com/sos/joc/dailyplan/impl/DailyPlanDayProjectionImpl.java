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
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.dailyplan.db.DBLayerDailyPlanProjections;
import com.sos.joc.dailyplan.resource.IDailyPlanDayProjectionResource;
import com.sos.joc.db.dailyplan.DBItemDailyPlanProjection;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.projections.ProjectionsDayRequest;
import com.sos.joc.model.dailyplan.projections.ProjectionsDayResponse;
import com.sos.joc.model.dailyplan.projections.items.meta.MetaItem;
import com.sos.joc.model.dailyplan.projections.items.year.DateItem;
import com.sos.joc.model.dailyplan.projections.items.year.DatePeriodItem;
import com.sos.joc.model.dailyplan.projections.items.year.MonthItem;
import com.sos.joc.model.dailyplan.projections.items.year.MonthsItem;
import com.sos.joc.model.dailyplan.projections.items.year.YearsItem;
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
                allowedControllers = controllerIds.filter(availableController -> getControllerPermissions(availableController,
                        accessToken).getOrders().getView()).collect(Collectors.toSet());
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
            
            String[] splittedDate = in.getDate().split("-");
            String year = splittedDate[0];
            String month = splittedDate[0] + "-" + splittedDate[1];
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            DBLayerDailyPlanProjections dbLayer = new DBLayerDailyPlanProjections(session);
            List<DBItemDailyPlanProjection> items = dbLayer.getProjections(Collections.singletonList(Long.valueOf(year)));
            dbLayer.close();
            session = null;

            MetaItem metaItem = null;
            Set<String> permittedSchedules = new HashSet<>();
            boolean unPermittedSchedulesExist = false;
            ProjectionsDayResponse entity = new ProjectionsDayResponse();
            entity.setNumOfPeriods(0);
            Date surveyDate = null;
            
            Set<String> scheduleNames = Collections.emptySet();
            if (items != null) {
                for (DBItemDailyPlanProjection item : items) {
                    if (!item.isMeta()) {
                        //System.out.println(Instant.now().toEpochMilli());

                        YearsItem yi = Globals.objectMapper.readValue(item.getContent(), YearsItem.class);
                        //System.out.println(Instant.now().toEpochMilli());
                        DateItem d = yi.getAdditionalProperties().getOrDefault(year, new MonthsItem())
                                .getAdditionalProperties().getOrDefault(month, new MonthItem()).getAdditionalProperties().get(in.getDate());
                        if (d != null) {
                            entity.setPeriods(d.getPeriods());
                            entity.setPlanned(d.getPlanned());
                            surveyDate = item.getCreated();
                            scheduleNames = d.getPeriods().stream().map(DatePeriodItem::getSchedule).filter(Objects::nonNull).map(
                                    JocInventory::pathToName).collect(Collectors.toSet());
                        }
                    }
                }
                
                if (!scheduleNames.isEmpty()) {
                    Optional<Set<String>> scheduleNamesOpt = Optional.empty();
                    if (in.getSchedulePaths() != null && !in.getSchedulePaths().isEmpty()) {
                        Set<String> scheduleNames1 = in.getSchedulePaths().stream().map(JocInventory::pathToName).collect(Collectors.toSet());
                        scheduleNames1.retainAll(scheduleNames);
                        scheduleNamesOpt = Optional.of(scheduleNames1);
                    } else {
                        scheduleNamesOpt = Optional.of(scheduleNames);
                    }
                    
                    Optional<DBItemDailyPlanProjection> metaContentOpt = items.stream().filter(DBItemDailyPlanProjection::isMeta).findAny();
                    if (metaContentOpt.isPresent()) {
                        surveyDate = metaContentOpt.get().getCreated();
                        metaItem = Globals.objectMapper.readValue(metaContentOpt.get().getContent(), MetaItem.class);
                        if (metaItem != null && metaItem.getAdditionalProperties() != null) {
                            if (DailyPlanProjectionsImpl.filterControllerIds(metaItem, allowedControllers)) {
                                unPermittedSchedulesExist = true;
                            }
                            for (String controllerId : allowedControllers) {
                                if (DailyPlanProjectionsImpl.filterPermittedSchedules(metaItem.getAdditionalProperties().get(controllerId), folderPermissions.getListOfFolders(
                                        controllerId), scheduleNamesOpt, permittedSchedules)) {
                                    unPermittedSchedulesExist = true;
                                }
                            }
                        }
                    } else {
                        throw new DBMissingDataException(
                                "Couldn't find projections meta data. Maybe a recalculation of the projections is in progress right now.");
                    }
                    
                    if (unPermittedSchedulesExist) {
                        entity.getPeriods().removeIf(p -> !permittedSchedules.contains(p.getSchedule()));
                    }
                    entity.setNumOfPeriods(entity.getPeriods().size());
                }
                
                
            }
            //System.out.println(Instant.now().toEpochMilli());
            
            entity.setSurveyDate(surveyDate);
            entity.setDeliveryDate(Date.from(Instant.now()));
            entity.setMeta(metaItem);
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
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
