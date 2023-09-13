package com.sos.joc.dailyplan.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
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
import com.sos.joc.dailyplan.DailyPlanRunner;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.db.DBLayerDailyPlanProjections;
import com.sos.joc.dailyplan.resource.IDailyPlanProjectionsResource;
import com.sos.joc.db.dailyplan.DBItemDailyPlanProjection;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.dailyplan.DailyPlanProjectionEvent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.projections.ProjectionsRequest;
import com.sos.joc.model.dailyplan.projections.ProjectionsResponse;
import com.sos.joc.model.dailyplan.projections.items.meta.ControllerInfoItem;
import com.sos.joc.model.dailyplan.projections.items.meta.MetaItem;
import com.sos.joc.model.dailyplan.projections.items.meta.WorkflowsItem;
import com.sos.joc.model.dailyplan.projections.items.year.MonthsItem;
import com.sos.joc.model.dailyplan.projections.items.year.YearsItem;
import com.sos.joc.model.security.configuration.permissions.JocPermissions;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanProjectionsImpl extends JOCResourceImpl implements IDailyPlanProjectionsResource {

    @Override
    public JOCDefaultResponse projections(String accessToken, byte[] filterBytes) {

        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ProjectionsRequest.class);
            ProjectionsRequest in = Globals.objectMapper.readValue(filterBytes, ProjectionsRequest.class);
            
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

            Long yearFrom = in.getDateFrom() != null ? Long.valueOf(in.getDateFrom().split("-",2)[0]) : null;
            Long yearTo = in.getDateTo() != null ? Long.valueOf(in.getDateTo().split("-",2)[0]) : null;
            Integer monthFrom = in.getDateFrom() != null ? getMonth(in.getDateFrom()) : null;
            Integer monthTo = in.getDateTo() != null ? getMonth(in.getDateTo()) : null;
            Integer dayFrom = in.getDateFrom() != null ? getDay(in.getDateFrom()) : null;
            Integer dayTo = in.getDateTo() != null ? getDay(in.getDateTo()) : null;
            
            Optional<Predicate<String>> pMonthFromTo = getMonthFromToPredicate(monthFrom, monthTo);
            Optional<Predicate<String>> pDayFromTo = getDayFromToPredicate(dayFrom, dayTo);
            Optional<Set<String>> scheduleNames = Optional.empty();
            if (in.getSchedulePaths() != null && !in.getSchedulePaths().isEmpty()) {
                scheduleNames = Optional.of(in.getSchedulePaths().stream().map(JocInventory::pathToName).collect(Collectors.toSet()));
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            DBLayerDailyPlanProjections dbLayer = new DBLayerDailyPlanProjections(session);
            List<DBItemDailyPlanProjection> items = dbLayer.getProjections(yearFrom, yearTo);
            dbLayer.close();
            session = null;

            MetaItem metaItem = null;
            Set<String> permittedSchedules = new HashSet<>();
            boolean unPermittedSchedulesExist = false;
            YearsItem yearsItem = new YearsItem();
            if (items != null) {
                Optional<String> metaContentOpt = items.stream().filter(DBItemDailyPlanProjection::isMeta).findAny().map(DBItemDailyPlanProjection::getContent);
                if (metaContentOpt.isPresent()) {
                    metaItem = Globals.objectMapper.readValue(metaContentOpt.get(), MetaItem.class);
                    if (metaItem != null && metaItem.getAdditionalProperties() != null) {
                        if (filterControllerIds(metaItem, allowedControllers)) {
                            unPermittedSchedulesExist = true;
                        }
                        for (String controllerId : allowedControllers) {
                            if (filterPermittedSchedules(metaItem.getAdditionalProperties().get(controllerId), folderPermissions.getListOfFolders(
                                    controllerId), scheduleNames, permittedSchedules)) {
                                unPermittedSchedulesExist = true;
                            }
                        }
                    }
                } else {
                    throw new DBMissingDataException("Couldn't find projection meta data. Maybe a recalculation of the projections is in progress right now.");
                }
                
                for (DBItemDailyPlanProjection item : items) {
                    if (!item.isMeta()) {
                        String year = String.valueOf(item.getId());

                        YearsItem yi = Globals.objectMapper.readValue(item.getContent(), YearsItem.class);
                        if (unPermittedSchedulesExist) {
                            yi.getAdditionalProperties().forEach((y, yearItem) -> {
                                pMonthFromTo.ifPresent(p -> yearItem.getAdditionalProperties().keySet().removeIf(p));
                                yearItem.getAdditionalProperties().forEach((m, monthItem) -> {
                                    pDayFromTo.ifPresent(p -> monthItem.getAdditionalProperties().keySet().removeIf(p));
                                    monthItem.getAdditionalProperties().forEach((d, dateItem) -> {
                                        dateItem.getPeriods().removeIf(p -> !permittedSchedules.contains(p.getSchedule()));
                                    });
                                    monthItem.getAdditionalProperties().values().removeIf(dateItem -> dateItem.getPeriods().isEmpty());
                                });
                                yearItem.getAdditionalProperties().values().removeIf(monthItem -> monthItem.getAdditionalProperties().isEmpty());
                            });
                            yi.getAdditionalProperties().values().removeIf(yearItem -> yearItem.getAdditionalProperties().isEmpty());
                            
                        } else if (pDayFromTo.isPresent()) {
                            yi.getAdditionalProperties().forEach((y, yearItem) -> {
                                yearItem.getAdditionalProperties().keySet().removeIf(pMonthFromTo.get());
                                yearItem.getAdditionalProperties().forEach((m, monthItem) -> {
                                    monthItem.getAdditionalProperties().keySet().removeIf(pDayFromTo.get());
                                });
                            });
                        }
                        
                        MonthsItem mi = yi.getAdditionalProperties().get(year);
                        if (mi != null) {
                            yearsItem.setAdditionalProperty(year, mi);
                        }
                    }
                }
            }

            // TODO filter - year, month, date, controllerId
            // TODO reduce response:
            // - e.g. for an year view - without meta and schedule/workflows informations - e.g. only months and dates
            // - e.g. for a day view - provide infos(meta + schedule/workflows) for the selected day only
            // -..
            // TODO check folder permissions for meta and year

            ProjectionsResponse entity = new ProjectionsResponse();
            entity.setDeliveryDate(Date.from(Instant.now()));
            entity.setMeta(metaItem);
            entity.setYears(yearsItem);
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

    private Optional<Predicate<String>> getMonthFromToPredicate(Integer monthFrom, Integer monthTo) {
        Predicate<String> pMonthFrom = m -> getMonth(m) < monthFrom;
        Predicate<String> pMonthTo = m -> getMonth(m) > monthTo;
        
        Predicate<String> pMonthFromTo = null;
        if (monthFrom != null) {
            pMonthFromTo = pMonthFrom;
            if (monthTo != null) {
                pMonthFromTo = pMonthFrom.or(pMonthTo);
            }
        } else if (monthTo != null) {
            pMonthFromTo = pMonthTo;
        }
        return pMonthFromTo == null ? Optional.empty() : Optional.of(pMonthFromTo);
    }
    
    private Optional<Predicate<String>> getDayFromToPredicate(Integer dayFrom, Integer dayTo) {
        Predicate<String> pDayFrom = d -> getDay(d) < dayFrom;
        Predicate<String> pDayTo = d -> getDay(d) > dayTo;
        
        Predicate<String> pDayFromTo = null;
        if (dayFrom != null) {
            pDayFromTo = pDayFrom;
            if (pDayTo != null) {
                pDayFromTo = pDayFrom.or(pDayTo);
            }
        } else if (dayTo != null) {
            pDayFromTo = pDayTo;
        }
        return pDayFromTo == null ? Optional.empty() : Optional.of(pDayFromTo);
    }

    @Override
    public JOCDefaultResponse recreate(String accessToken, byte[] filterBytes) {
        try {
            initLogging(IMPL_PATH_RECREATE, filterBytes, accessToken);

            // TODO run async
            CompletableFuture.runAsync(() -> {
                try {
                    DailyPlanRunner.recreateProjections(JOCOrderResourceImpl.getDailyPlanSettings());
                    EventBus.getInstance().post(new DailyPlanProjectionEvent());
                } catch (Exception e) {
                    ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, getJocError(), null);
                }
            });

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private static boolean filterControllerIds(MetaItem metaItem, Set<String> allowedControllers) {
        boolean controllerRemoved = false;
        if (metaItem != null && metaItem.getAdditionalProperties() != null) {
            //metaItem.getAdditionalProperties().keySet().removeIf(controllerId -> !allowedControllers.contains(controllerId));
            controllerRemoved = metaItem.getAdditionalProperties().keySet().retainAll(allowedControllers);
        }
        return controllerRemoved;
    }
    
    private static boolean filterPermittedWorkflows(WorkflowsItem workflowsItem, Set<Folder> permittedFolders) {
        boolean workflowsRemoved = false;
        if (!permittedFolders.isEmpty() && workflowsItem != null && workflowsItem.getAdditionalProperties() != null) {
            workflowsRemoved = workflowsItem.getAdditionalProperties().keySet().removeIf(wPath -> !canAdd(wPath, permittedFolders));
        }
        return workflowsRemoved;
    }
    
    private static boolean filterPermittedSchedules(ControllerInfoItem cii, Set<Folder> permittedFolders, Optional<Set<String>> scheduleNames,
            Set<String> permittedSchedules) {
        boolean schedulesRemoved = false;
        if (cii != null && cii.getAdditionalProperties() != null) {
            int numOfSchedules = cii.getAdditionalProperties().keySet().size();

            scheduleNames.ifPresent(sn -> cii.getAdditionalProperties().keySet().removeIf(schedule -> !sn.contains(JocInventory.pathToName(
                    schedule))));

            if (!permittedFolders.isEmpty()) {
                cii.getAdditionalProperties().keySet().removeIf(schedule -> !canAdd(schedule, permittedFolders));
                cii.getAdditionalProperties().values().forEach(sii -> {
                    if (sii != null) {
                        filterPermittedWorkflows(sii.getWorkflows(), permittedFolders);
                    }
                });
            }
            cii.getAdditionalProperties().values().removeIf(sii -> sii == null || sii.getWorkflows() == null || sii
                    .getWorkflows().getAdditionalProperties() == null || sii.getWorkflows().getAdditionalProperties()
                            .isEmpty());

            int newNumOfSchedules = cii.getAdditionalProperties().keySet().size();
            if (numOfSchedules > newNumOfSchedules) {
                schedulesRemoved = true;
            }
            permittedSchedules.addAll(cii.getAdditionalProperties().keySet());
        }
        return schedulesRemoved;
    }
    
    private static Integer getMonth(String month) {
        return Integer.valueOf(month.replaceFirst("^(\\d{4})-(\\d{2}).*", "$1$2"));
    }
    
    private static Integer getDay(String day) {
        return Integer.valueOf(day.replace("-", ""));
    }

}
