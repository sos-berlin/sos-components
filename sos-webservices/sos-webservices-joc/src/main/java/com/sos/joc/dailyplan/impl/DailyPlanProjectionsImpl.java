package com.sos.joc.dailyplan.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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
import com.sos.joc.model.dailyplan.projections.ProjectionsCalendarResponse;
import com.sos.joc.model.dailyplan.projections.ProjectionsRequest;
import com.sos.joc.model.dailyplan.projections.ProjectionsScheduleRequest;
import com.sos.joc.model.dailyplan.projections.ProjectionsScheduleResponse;
import com.sos.joc.model.dailyplan.projections.ProjectionsSchedulesResponse;
import com.sos.joc.model.dailyplan.projections.items.meta.ControllerInfoItem;
import com.sos.joc.model.dailyplan.projections.items.meta.MetaItem;
import com.sos.joc.model.dailyplan.projections.items.meta.WorkflowsItem;
import com.sos.joc.model.dailyplan.projections.items.year.DateItem;
import com.sos.joc.model.dailyplan.projections.items.year.DatePeriodItem;
import com.sos.joc.model.dailyplan.projections.items.year.MonthsItem;
import com.sos.joc.model.dailyplan.projections.items.year.YearsItem;
import com.sos.joc.model.security.configuration.permissions.JocPermissions;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanProjectionsImpl extends JOCResourceImpl implements IDailyPlanProjectionsResource {

    @Override
    public JOCDefaultResponse calendarProjections(String accessToken, byte[] filterBytes) {

        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH_CALENDAR, filterBytes, accessToken);
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

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_CALENDAR);
            DBLayerDailyPlanProjections dbLayer = new DBLayerDailyPlanProjections(session);
            List<DBItemDailyPlanProjection> items = dbLayer.getProjections(yearFrom, yearTo);
            dbLayer.close();
            session = null;

            MetaItem metaItem = null;
            Set<String> permittedSchedules = new HashSet<>();
            boolean unPermittedSchedulesExist = false;
            YearsItem yearsItem = new YearsItem();
            Date surveyDate = null;
            
            if (items != null) {
                System.out.println(Instant.now().toEpochMilli());
                Optional<DBItemDailyPlanProjection> metaContentOpt = items.stream().filter(DBItemDailyPlanProjection::isMeta).findAny();
                if (metaContentOpt.isPresent()) {
                    surveyDate = metaContentOpt.get().getCreated();
                    metaItem = Globals.objectMapper.readValue(metaContentOpt.get().getContent(), MetaItem.class);
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
                    throw new DBMissingDataException(
                            "Couldn't find projections meta data. Maybe a recalculation of the projections is in progress right now.");
                }
                final boolean unPermittedSchedulesExist2 = unPermittedSchedulesExist;
                for (DBItemDailyPlanProjection item : items) {
                    if (!item.isMeta()) {
                        System.out.println(Instant.now().toEpochMilli());
                        String year = String.valueOf(item.getId());

                        YearsItem yi = Globals.objectMapper.readValue(item.getContent(), YearsItem.class);
                        System.out.println(Instant.now().toEpochMilli());
                        yi.getAdditionalProperties().forEach((y, yearItem) -> {
                            pMonthFromTo.ifPresent(p -> yearItem.getAdditionalProperties().keySet().removeIf(p));
                            yearItem.getAdditionalProperties().forEach((m, monthItem) -> {
                                pDayFromTo.ifPresent(p -> monthItem.getAdditionalProperties().keySet().removeIf(p));
                                monthItem.getAdditionalProperties().forEach((d, dateItem) -> {
                                    if (unPermittedSchedulesExist2) {
                                        dateItem.getPeriods().removeIf(p -> !permittedSchedules.contains(p.getSchedule()));
                                    }
                                    dateItem.setNumOfPeriods(dateItem.getPeriods().size());
                                    dateItem.setPeriods(null);
                                });
                                monthItem.getAdditionalProperties().values().removeIf(dateItem -> dateItem.getNumOfPeriods() == 0);
                            });
                            yearItem.getAdditionalProperties().values().removeIf(monthItem -> monthItem.getAdditionalProperties().isEmpty());
                        });
                        yi.getAdditionalProperties().values().removeIf(yearItem -> yearItem.getAdditionalProperties().isEmpty());
                        
                        MonthsItem mi = yi.getAdditionalProperties().get(year);
                        if (mi != null) {
                            yearsItem.setAdditionalProperty(year, mi);
                        }
                    }
                }
            }
            System.out.println(Instant.now().toEpochMilli());

            ProjectionsCalendarResponse entity = new ProjectionsCalendarResponse();
            entity.setDeliveryDate(Date.from(Instant.now()));
            entity.setSurveyDate(surveyDate);
            //entity.setMeta(metaItem);
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
    
    @Override
    public JOCDefaultResponse schedulesProjections(String accessToken, byte[] filterBytes) {

        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH_SCHEDULES, filterBytes, accessToken);
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

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_SCHEDULES);
            DBLayerDailyPlanProjections dbLayer = new DBLayerDailyPlanProjections(session);
            List<DBItemDailyPlanProjection> items = dbLayer.getProjections(yearFrom, yearTo);
            dbLayer.close();
            session = null;

            MetaItem metaItem = null;
            Set<String> permittedSchedules = new HashSet<>();
            boolean unPermittedSchedulesExist = false;
//            SchedulesItem si = new SchedulesItem();
            SortedSet<String> schedules = new TreeSet<>();
            Date surveyDate = null;
            
            if (items != null) {
                System.out.println(Instant.now().toEpochMilli());
                Optional<DBItemDailyPlanProjection> metaContentOpt = items.stream().filter(DBItemDailyPlanProjection::isMeta).findAny();
                if (metaContentOpt.isPresent()) {
                    surveyDate = metaContentOpt.get().getCreated();
                    metaItem = Globals.objectMapper.readValue(metaContentOpt.get().getContent(), MetaItem.class);
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
                    throw new DBMissingDataException(
                            "Couldn't find projections meta data. Maybe a recalculation of the projections is in progress right now.");
                }
                
//                List<DatePeriodItem> datePeriodItemItems = new ArrayList<>();
                
                final boolean unPermittedSchedulesExist2 = unPermittedSchedulesExist;
                for (DBItemDailyPlanProjection item : items) {
                    if (!item.isMeta()) {

                        YearsItem yi = Globals.objectMapper.readValue(item.getContent(), YearsItem.class);
                        yi.getAdditionalProperties().forEach((y, yearItem) -> {
                            pMonthFromTo.ifPresent(p -> yearItem.getAdditionalProperties().keySet().removeIf(p));
                            yearItem.getAdditionalProperties().forEach((m, monthItem) -> {
                                pDayFromTo.ifPresent(p -> monthItem.getAdditionalProperties().keySet().removeIf(p));
                                monthItem.getAdditionalProperties().forEach((d, dateItem) -> {
                                    if (unPermittedSchedulesExist2) {
                                        dateItem.getPeriods().removeIf(p -> !permittedSchedules.contains(p.getSchedule()));
                                    }
                                    dateItem.setNumOfPeriods(dateItem.getPeriods().size());
                                    //dateItem.setPeriods(null);
                                    if (dateItem.getNumOfPeriods() != 0) {
                                        dateItem.getPeriods().forEach(dpi -> {
//                                            dpi.setAdditionalProperty("planned", dateItem.getPlanned());
//                                            dpi.setAdditionalProperty("day", d);
//                                            datePeriodItemItems.add(dpi);
                                            schedules.add(dpi.getSchedule());
                                        });
                                    }
                                });
                            });
                        });
                    }
                }
                
//                datePeriodItemItems.stream().collect(Collectors.groupingBy(DatePeriodItem::getSchedule, Collectors.groupingBy(dpi -> (String) dpi
//                        .getAdditionalProperties().get("day")))).forEach((schedule, dpiMap) -> {
//                            MonthItem mi = new MonthItem();
//
//                            dpiMap.forEach((day, dpiList) -> {
//                                if (!dpiList.isEmpty()) {
//                                    DateItem di = new DateItem();
//                                    di.setPlanned((Boolean) dpiList.get(0).getAdditionalProperties().get("planned"));
//                                    di.setNumOfPeriods(dpiList.size());
//                                    di.setPeriods(null);
//                                    mi.setAdditionalProperty(day, di);
//                                }
//                            });
//                            
//                            si.setAdditionalProperty(schedule, mi);
//                        });
                
                
                
            }
            System.out.println(Instant.now().toEpochMilli());

            ProjectionsSchedulesResponse entity = new ProjectionsSchedulesResponse();
            entity.setDeliveryDate(Date.from(Instant.now()));
            entity.setSurveyDate(surveyDate);
            entity.setSchedules(schedules);
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
    
    @Override
    public JOCDefaultResponse scheduleProjections(String accessToken, byte[] filterBytes) {

        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH_SCHEDULES, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ProjectionsScheduleRequest.class);
            ProjectionsScheduleRequest in = Globals.objectMapper.readValue(filterBytes, ProjectionsScheduleRequest.class);
            
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
            Optional<Set<String>> scheduleNames = Optional.of(Collections.singleton(JocInventory.pathToName(in.getSchedulePath())));

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_SCHEDULES);
            DBLayerDailyPlanProjections dbLayer = new DBLayerDailyPlanProjections(session);
            List<DBItemDailyPlanProjection> items = dbLayer.getProjections(yearFrom, yearTo);
            dbLayer.close();
            session = null;

            MetaItem metaItem = null;
            Set<String> permittedSchedules = new HashSet<>();
            boolean unPermittedSchedulesExist = false;
            ProjectionsScheduleResponse entity = new ProjectionsScheduleResponse();
            Date surveyDate = null;
            
            if (items != null) {
                System.out.println(Instant.now().toEpochMilli());
                Optional<DBItemDailyPlanProjection> metaContentOpt = items.stream().filter(DBItemDailyPlanProjection::isMeta).findAny();
                if (metaContentOpt.isPresent()) {
                    surveyDate = metaContentOpt.get().getCreated();
                    metaItem = Globals.objectMapper.readValue(metaContentOpt.get().getContent(), MetaItem.class);
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
                    throw new DBMissingDataException(
                            "Couldn't find projections meta data. Maybe a recalculation of the projections is in progress right now.");
                }
                
                List<DatePeriodItem> datePeriodItemItems = new ArrayList<>();
                
                final boolean unPermittedSchedulesExist2 = unPermittedSchedulesExist;
                for (DBItemDailyPlanProjection item : items) {
                    if (!item.isMeta()) {

                        YearsItem yi = Globals.objectMapper.readValue(item.getContent(), YearsItem.class);
                        yi.getAdditionalProperties().forEach((y, yearItem) -> {
                            pMonthFromTo.ifPresent(p -> yearItem.getAdditionalProperties().keySet().removeIf(p));
                            yearItem.getAdditionalProperties().forEach((m, monthItem) -> {
                                pDayFromTo.ifPresent(p -> monthItem.getAdditionalProperties().keySet().removeIf(p));
                                monthItem.getAdditionalProperties().forEach((d, dateItem) -> {
                                    if (unPermittedSchedulesExist2) {
                                        dateItem.getPeriods().removeIf(p -> !permittedSchedules.contains(p.getSchedule()));
                                    }
                                    dateItem.setNumOfPeriods(dateItem.getPeriods().size());
                                    if (dateItem.getNumOfPeriods() != 0) {
                                        dateItem.getPeriods().forEach(dpi -> {
                                            dpi.setAdditionalProperty("planned", dateItem.getPlanned());
                                            dpi.setAdditionalProperty("day", d);
                                            datePeriodItemItems.add(dpi);
                                        });
                                    }
                                });
                            });
                        });
                    }
                }
                
                datePeriodItemItems.stream().collect(Collectors.groupingBy(dpi -> (String) dpi.getAdditionalProperties().get("day"))).forEach((day,
                        dpiList) -> {
                    if (!dpiList.isEmpty()) {
                        DateItem di = new DateItem();
                        di.setPlanned((Boolean) dpiList.get(0).getAdditionalProperties().get("planned"));
                        di.setNumOfPeriods(dpiList.size());
                        di.setPeriods(null);
                        entity.setAdditionalProperty(day, di);
                    }
                });
                
            }

            entity.setDeliveryDate(Date.from(Instant.now()));
            entity.setSurveyDate(surveyDate);
            
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
        if (monthFrom != null && !(monthFrom + "").endsWith("01")) {
            pMonthFromTo = pMonthFrom;
            if (monthTo != null && !(monthTo + "").endsWith("12")) {
                pMonthFromTo = pMonthFrom.or(pMonthTo);
            }
        } else if (monthTo != null && !(monthTo + "").endsWith("12")) {
            pMonthFromTo = pMonthTo;
        }
        return pMonthFromTo == null ? Optional.empty() : Optional.of(pMonthFromTo);
    }
    
    private Optional<Predicate<String>> getDayFromToPredicate(Integer dayFrom, Integer dayTo) {
        Predicate<String> pDayFrom = d -> getDay(d) < dayFrom;
        Predicate<String> pDayTo = d -> getDay(d) > dayTo;
        List<String> ultimos = Arrays.asList("0131", "0228", "0229", "0331", "0430", "0531", "0630", "0731", "0831", "0930", "1031", "1130", "1231");
        boolean dayToIsUltimo = dayTo != null ? ultimos.stream().anyMatch(u -> (dayTo + "").endsWith(u)) : false;

        Predicate<String> pDayFromTo = null;
        if (dayFrom != null && !(dayFrom + "").endsWith("01")) {
            pDayFromTo = pDayFrom;
            if (dayTo != null && !dayToIsUltimo) {
                pDayFromTo = pDayFrom.or(pDayTo);
            }
        } else if (dayTo != null && !dayToIsUltimo) {
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
    
    protected static boolean filterControllerIds(MetaItem metaItem, Set<String> allowedControllers) {
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
    
    protected static boolean filterPermittedSchedules(ControllerInfoItem cii, Set<Folder> permittedFolders, Optional<Set<String>> scheduleNames,
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
