package com.sos.joc.dailyplan.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.sos.auth.classes.SOSAuthFolderPermissions;
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
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.projections.ProjectionsCalendarResponse;
import com.sos.joc.model.dailyplan.projections.ProjectionsRequest;
import com.sos.joc.model.dailyplan.projections.items.meta.ControllerInfoItem;
import com.sos.joc.model.dailyplan.projections.items.meta.MetaItem;
import com.sos.joc.model.dailyplan.projections.items.meta.WorkflowsItem;
import com.sos.joc.model.dailyplan.projections.items.year.DatePeriodItem;
import com.sos.joc.model.dailyplan.projections.items.year.MonthItem;
import com.sos.joc.model.dailyplan.projections.items.year.MonthsItem;
import com.sos.joc.model.dailyplan.projections.items.year.YearsItem;
import com.sos.joc.model.security.configuration.permissions.JocPermissions;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanProjectionsImpl extends JOCResourceImpl implements IDailyPlanProjectionsResource {
    
    @Override
    public JOCDefaultResponse datesProjections(String accessToken, String acceptEncoding, byte[] filterBytes) {
        return projections(accessToken, acceptEncoding, filterBytes, IMPL_PATH_DATES);
    }
    
    @Override
    public JOCDefaultResponse calendarProjections(String accessToken, byte[] filterBytes) {
        return projections(accessToken, null, filterBytes, IMPL_PATH_CALENDAR);
    }

    private JOCDefaultResponse projections(String accessToken, String acceptEncoding, byte[] filterBytes, String action) {

        SOSHibernateSession session = null;
        try {
            initLogging(action, filterBytes, accessToken);
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
            
            Long monthFromAsLong = in.getDateFrom() != null ? getMonth(in.getDateFrom()) : null;
            Long monthToAsLong = in.getDateTo() != null ? getMonth(in.getDateTo()) : null;
            Integer dayFrom = in.getDateFrom() != null ? getDay(in.getDateFrom()) : null;
            Integer dayTo = in.getDateTo() != null ? getDay(in.getDateTo()) : null;
            
            Optional<Predicate<String>> pDayFromTo = getDayFromToPredicate(dayFrom, dayTo);
            
            Optional<Set<String>> scheduleNames = getNamesOptional(in.getSchedulePaths());
            Optional<Set<String>> workflowNames = getNamesOptional(in.getWorkflowPaths());

            session = Globals.createSosHibernateStatelessConnection(action);
            DBLayerDailyPlanProjections dbLayer = new DBLayerDailyPlanProjections(session);
            List<DBItemDailyPlanProjection> items = dbLayer.getProjections(monthFromAsLong, monthToAsLong);
            dbLayer.close();
            session = null;

            YearsItem yearsItem = new YearsItem();
            Optional<DBItemDailyPlanProjection> metaOpt = Optional.empty();
            Optional<MetaItem> metaContentOpt = Optional.empty();
            
            boolean withPeriods = action == IMPL_PATH_DATES;
            
            if (items != null) {
                Set<String> permittedSchedules = new HashSet<>();
                metaOpt = items.stream().filter(DBItemDailyPlanProjection::isMeta).findAny();
                metaContentOpt = metaOpt.map(m -> {
                    try {
                        return Globals.objectMapper.readValue(m.getContent(), MetaItem.class);
                    } catch (Exception e) {
                        throw new DBInvalidDataException(e);
                    }
                });
                
                final boolean unPermittedSchedulesExist = setPermittedSchedules(metaContentOpt, allowedControllers, scheduleNames, in
                        .getScheduleFolders(), workflowNames, in.getWorkflowFolders(), permittedSchedules, folderPermissions);
                
                for (DBItemDailyPlanProjection item : items) {
                    setYearsItem(item, in.getWithoutStartTime(), withPeriods, unPermittedSchedulesExist, permittedSchedules, pDayFromTo, yearsItem);
                }
            }

            ProjectionsCalendarResponse entity = new ProjectionsCalendarResponse();
            entity.setDeliveryDate(Date.from(Instant.now()));
            metaOpt.ifPresent(meta -> entity.setSurveyDate(meta.getCreated()));
            entity.setYears(yearsItem);
            if (withPeriods) {
                metaContentOpt.ifPresent(meta -> entity.setMeta(meta));
            }
            
            
            boolean withGzipEncoding = acceptEncoding != null && acceptEncoding.equals("gzip");
            if (withPeriods) {
                StreamingOutput entityStream = new StreamingOutput() {

                    @Override
                    public void write(OutputStream output) throws IOException {
                        if (withGzipEncoding) {
                            output = new GZIPOutputStream(output);
                        }
                        InputStream in = null;
                        try {
                            in = new ByteArrayInputStream(Globals.objectMapper.writeValueAsBytes(entity));
                            byte[] buffer = new byte[4096];
                            int length;
                            while ((length = in.read(buffer)) > 0) {
                                output.write(buffer, 0, length);
                            }
                            output.flush();
                        } finally {
                            try {
                                output.close();
                            } catch (Exception e) {
                            }
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                };
                return JOCDefaultResponse.responseStatus200(entityStream, MediaType.APPLICATION_JSON, getGzipHeaders(withGzipEncoding));
            } else {
                return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
            }
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
    
    private Map<String, Object> getGzipHeaders(boolean withGzipEncoding) {
        Map<String, Object> headers = new HashMap<String, Object>();
        if (withGzipEncoding) {
            headers.put("Content-Encoding", "gzip");
        }
        headers.put("Transfer-Encoding", "chunked");
        return headers;
    }
    
    public static Optional<Set<String>> getNamesOptional(List<String> paths) {
        Optional<Set<String>> names = Optional.empty();
        if (paths != null && !paths.isEmpty()) {
            names = Optional.of(paths.stream().map(JocInventory::pathToName).collect(Collectors.toSet()));
        }
        return names;
    }
    
    private static boolean setPermittedSchedules(Optional<MetaItem> metaContentOpt, Set<String> allowedControllers,
            Optional<Set<String>> scheduleNames, List<Folder> scheduleFolders, Optional<Set<String>> workflowNames, List<Folder> workflowFolders,
            Set<String> permittedSchedules, SOSAuthFolderPermissions folderPermissions) throws DBMissingDataException {
        return setPermittedSchedules(metaContentOpt, allowedControllers, scheduleNames, scheduleFolders, Optional.empty(), workflowNames,
                workflowFolders, permittedSchedules, folderPermissions);
    }

    public static boolean setPermittedSchedules(Optional<MetaItem> metaContentOpt, Set<String> allowedControllers,
            Optional<Set<String>> scheduleNames, List<Folder> scheduleFolders, Optional<Set<String>> nonPeriodScheduleNames,
            Optional<Set<String>> workflowNames, List<Folder> workflowFolders, Set<String> permittedSchedules,
            SOSAuthFolderPermissions folderPermissions) throws DBMissingDataException {
        boolean schedulesRemoved = false;
        MetaItem metaItem = metaContentOpt.orElseThrow(DailyPlanProjectionsImpl::getDBMissingDataException);
        if (metaItem != null && metaItem.getAdditionalProperties() != null) {
            if (filterControllerIds(metaItem, allowedControllers)) {
                schedulesRemoved = true;
            }
            for (String controllerId : allowedControllers) {
                if (filterPermittedSchedules(metaItem.getAdditionalProperties().get(controllerId), folderPermissions.getListOfFolders(
                        controllerId), scheduleNames, scheduleFolders, nonPeriodScheduleNames, workflowNames, workflowFolders, permittedSchedules)) {
                    schedulesRemoved = true;
                }
            }
            metaContentOpt = Optional.of(metaItem);
        }
        return schedulesRemoved;
    }
    
    public static DBMissingDataException getDBMissingDataException() throws DBMissingDataException {
        if (DBLayerDailyPlanProjections.projectionsStart.isPresent()) {
            return new DBMissingDataException("Couldn't find projections data. A calculation of the projections are in progress right now.");
        } else {
            return new DBMissingDataException("Couldn't find projections data. Please start a calculation of the projections.");
        }
    }
    
    private static void setYearsItem(DBItemDailyPlanProjection item, boolean nonPeriods, boolean withPeriods, boolean unPermittedSchedulesExist,
            Set<String> permittedSchedules, Optional<Predicate<String>> pDayFromTo, YearsItem yearsItem) throws StreamReadException,
            DatabindException, IOException {
        if (!item.isMeta()) {
            final int numOfPermittedSchedules = permittedSchedules.size();
            String month = String.valueOf(item.getId());
            String year = month.substring(0, 4);
            month = year + "-" + month.substring(4);
            yearsItem.getAdditionalProperties().putIfAbsent(year, new MonthsItem());
            MonthsItem mi = yearsItem.getAdditionalProperties().get(year);

            MonthItem monthItem = Globals.objectMapper.readValue(item.getContent(), MonthItem.class);
            pDayFromTo.ifPresent(p -> monthItem.getAdditionalProperties().keySet().removeIf(p));
            monthItem.getAdditionalProperties().forEach((d, dateItem) -> {
                if (unPermittedSchedulesExist) {
                    dateItem.getPeriods().removeIf(p -> !permittedSchedules.contains(p.getSchedule()));
                }
                if (withPeriods) {
                    if (nonPeriods) {
                        Set<String> schedulesOfTheDay = dateItem.getPeriods().stream().map(DatePeriodItem::getSchedule).filter(Objects::nonNull)
                                .collect(Collectors.toSet());
                        
                        dateItem.setNonPeriods(permittedSchedules.stream().filter(s -> !schedulesOfTheDay.contains(s)).map(s -> {
                            DatePeriodItem dpi = new DatePeriodItem();
                            dpi.setSchedule(s);
                            return dpi;
                        }).collect(Collectors.toList()));
                        dateItem.setPeriods(null); //if setNonPeriods is introduced
                    }
                    
                } else {
                    if (!nonPeriods) {
                        dateItem.setNumOfPeriods(dateItem.getPeriods().size());
                    } else {
                        int numOfschedulesOfTheDay = dateItem.getPeriods().stream().map(DatePeriodItem::getSchedule).filter(Objects::nonNull)
                                .distinct().mapToInt(i -> 1).sum();
                        dateItem.setNumOfNonPeriods(numOfPermittedSchedules - numOfschedulesOfTheDay);
                    }
                    dateItem.setPeriods(null);
                }
            });
            if (withPeriods) {
                if (!nonPeriods) {
                    monthItem.getAdditionalProperties().values().removeIf(dateItem -> dateItem.getPeriods().isEmpty());
                } else {
                    monthItem.getAdditionalProperties().values().removeIf(dateItem -> dateItem.getNonPeriods().isEmpty());
                }
            } else {
                if (!nonPeriods) {
                    monthItem.getAdditionalProperties().values().removeIf(dateItem -> dateItem.getNumOfPeriods() == 0);
                } else {
                    monthItem.getAdditionalProperties().values().removeIf(dateItem -> dateItem.getNumOfNonPeriods() == 0);
                }
            }
            if (!monthItem.getAdditionalProperties().isEmpty()) {
                mi.setAdditionalProperty(month, monthItem);
            }
            if (!mi.getAdditionalProperties().isEmpty()) {
                yearsItem.getAdditionalProperties().put(year, mi);
            } else {
                yearsItem.getAdditionalProperties().remove(year);
            }
        }
    }
    
//    @Override
//    public JOCDefaultResponse schedulesProjections(String accessToken, byte[] filterBytes) {
//
//        SOSHibernateSession session = null;
//        try {
//            initLogging(IMPL_PATH_SCHEDULES, filterBytes, accessToken);
//            JsonValidator.validateFailFast(filterBytes, ProjectionsRequest.class);
//            ProjectionsRequest in = Globals.objectMapper.readValue(filterBytes, ProjectionsRequest.class);
//            
//            boolean noControllerAvailable = Proxies.getControllerDbInstances().isEmpty();
//            boolean permitted = true;
//            Set<String> allowedControllers = Collections.emptySet();
//            if (!noControllerAvailable) {
//                Stream<String> controllerIds = Proxies.getControllerDbInstances().keySet().stream();
//                if (in.getControllerIds() != null && !in.getControllerIds().isEmpty()) {
//                    controllerIds = controllerIds.filter(availableController -> in.getControllerIds().contains(availableController));
//                }
//                allowedControllers = controllerIds.filter(availableController -> getControllerPermissions(availableController,
//                        accessToken).getOrders().getView()).collect(Collectors.toSet());
//                permitted = !allowedControllers.isEmpty();
//            }
//            if (permitted) {
//                JocPermissions perms = getJocPermissions(accessToken);
//                permitted = perms.getCalendars().getView() || perms.getDailyPlan().getView();
//            }
//
//            JOCDefaultResponse jocDefaultResponse = initPermissions(null, permitted);
//            if (jocDefaultResponse != null) {
//                return jocDefaultResponse;
//            }
//
//            Long yearFrom = in.getDateFrom() != null ? Long.valueOf(in.getDateFrom().split("-",2)[0]) : null;
//            Long yearTo = in.getDateTo() != null ? Long.valueOf(in.getDateTo().split("-",2)[0]) : null;
//            Integer monthFrom = in.getDateFrom() != null ? getMonth(in.getDateFrom()) : null;
//            Integer monthTo = in.getDateTo() != null ? getMonth(in.getDateTo()) : null;
//            Integer dayFrom = in.getDateFrom() != null ? getDay(in.getDateFrom()) : null;
//            Integer dayTo = in.getDateTo() != null ? getDay(in.getDateTo()) : null;
//            
//            Optional<Predicate<String>> pMonthFromTo = getMonthFromToPredicate(monthFrom, monthTo);
//            Optional<Predicate<String>> pDayFromTo = getDayFromToPredicate(dayFrom, dayTo);
//            Optional<Set<String>> scheduleNames = Optional.empty();
//            if (in.getSchedulePaths() != null && !in.getSchedulePaths().isEmpty()) {
//                scheduleNames = Optional.of(in.getSchedulePaths().stream().map(JocInventory::pathToName).collect(Collectors.toSet()));
//            }
//
//            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_SCHEDULES);
//            DBLayerDailyPlanProjections dbLayer = new DBLayerDailyPlanProjections(session);
//            List<DBItemDailyPlanProjection> items = dbLayer.getProjections(yearFrom, yearTo);
//            dbLayer.close();
//            session = null;
//
//            MetaItem metaItem = null;
//            Set<String> permittedSchedules = new HashSet<>();
//            boolean unPermittedSchedulesExist = false;
////            SchedulesItem si = new SchedulesItem();
//            SortedSet<String> schedules = new TreeSet<>();
//            Date surveyDate = null;
//            
//            if (items != null) {
//                Optional<DBItemDailyPlanProjection> metaContentOpt = items.stream().filter(DBItemDailyPlanProjection::isMeta).findAny();
//                if (metaContentOpt.isPresent()) {
//                    surveyDate = metaContentOpt.get().getCreated();
//                    metaItem = Globals.objectMapper.readValue(metaContentOpt.get().getContent(), MetaItem.class);
//                    if (metaItem != null && metaItem.getAdditionalProperties() != null) {
//                        if (filterControllerIds(metaItem, allowedControllers)) {
//                            unPermittedSchedulesExist = true;
//                        }
//                        for (String controllerId : allowedControllers) {
//                            if (filterPermittedSchedules(metaItem.getAdditionalProperties().get(controllerId), folderPermissions.getListOfFolders(
//                                    controllerId), scheduleNames, permittedSchedules)) {
//                                unPermittedSchedulesExist = true;
//                            }
//                        }
//                    }
//                } else {
//                    throw new DBMissingDataException(
//                            "Couldn't find projections meta data. Maybe a recalculation of the projections is in progress right now.");
//                }
//                
////                List<DatePeriodItem> datePeriodItemItems = new ArrayList<>();
//                
//                final boolean unPermittedSchedulesExist2 = unPermittedSchedulesExist;
//                for (DBItemDailyPlanProjection item : items) {
//                    if (!item.isMeta()) {
//
//                        YearsItem yi = Globals.objectMapper.readValue(item.getContent(), YearsItem.class);
//                        yi.getAdditionalProperties().forEach((y, yearItem) -> {
//                            pMonthFromTo.ifPresent(p -> yearItem.getAdditionalProperties().keySet().removeIf(p));
//                            yearItem.getAdditionalProperties().forEach((m, monthItem) -> {
//                                pDayFromTo.ifPresent(p -> monthItem.getAdditionalProperties().keySet().removeIf(p));
//                                monthItem.getAdditionalProperties().forEach((d, dateItem) -> {
//                                    if (unPermittedSchedulesExist2) {
//                                        dateItem.getPeriods().removeIf(p -> !permittedSchedules.contains(p.getSchedule()));
//                                    }
//                                    dateItem.setNumOfPeriods(dateItem.getPeriods().size());
//                                    //dateItem.setPeriods(null);
//                                    if (dateItem.getNumOfPeriods() != 0) {
//                                        dateItem.getPeriods().forEach(dpi -> {
////                                            dpi.setAdditionalProperty("planned", dateItem.getPlanned());
////                                            dpi.setAdditionalProperty("day", d);
////                                            datePeriodItemItems.add(dpi);
//                                            schedules.add(dpi.getSchedule());
//                                        });
//                                    }
//                                });
//                            });
//                        });
//                    }
//                }
//                
////                datePeriodItemItems.stream().collect(Collectors.groupingBy(DatePeriodItem::getSchedule, Collectors.groupingBy(dpi -> (String) dpi
////                        .getAdditionalProperties().get("day")))).forEach((schedule, dpiMap) -> {
////                            MonthItem mi = new MonthItem();
////
////                            dpiMap.forEach((day, dpiList) -> {
////                                if (!dpiList.isEmpty()) {
////                                    DateItem di = new DateItem();
////                                    di.setPlanned((Boolean) dpiList.get(0).getAdditionalProperties().get("planned"));
////                                    di.setNumOfPeriods(dpiList.size());
////                                    di.setPeriods(null);
////                                    mi.setAdditionalProperty(day, di);
////                                }
////                            });
////                            
////                            si.setAdditionalProperty(schedule, mi);
////                        });
//                
//                
//                
//            }
//
//            ProjectionsSchedulesResponse entity = new ProjectionsSchedulesResponse();
//            entity.setDeliveryDate(Date.from(Instant.now()));
//            entity.setSurveyDate(surveyDate);
//            entity.setSchedules(schedules);
//            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
//        } catch (JocException e) {
//            e.addErrorMetaInfo(getJocError());
//            return JOCDefaultResponse.responseStatusJSError(e);
//        } catch (Exception e) {
//            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
//        } finally {
//            Globals.disconnect(session);
//        }
//    }
    
//    @Override
//    public JOCDefaultResponse scheduleProjections(String accessToken, byte[] filterBytes) {
//
//        SOSHibernateSession session = null;
//        try {
//            initLogging(IMPL_PATH_SCHEDULES, filterBytes, accessToken);
//            JsonValidator.validateFailFast(filterBytes, ProjectionsScheduleRequest.class);
//            ProjectionsScheduleRequest in = Globals.objectMapper.readValue(filterBytes, ProjectionsScheduleRequest.class);
//            
//            boolean noControllerAvailable = Proxies.getControllerDbInstances().isEmpty();
//            boolean permitted = true;
//            Set<String> allowedControllers = Collections.emptySet();
//            if (!noControllerAvailable) {
//                Stream<String> controllerIds = Proxies.getControllerDbInstances().keySet().stream();
//                if (in.getControllerIds() != null && !in.getControllerIds().isEmpty()) {
//                    controllerIds = controllerIds.filter(availableController -> in.getControllerIds().contains(availableController));
//                }
//                allowedControllers = controllerIds.filter(availableController -> getControllerPermissions(availableController,
//                        accessToken).getOrders().getView()).collect(Collectors.toSet());
//                permitted = !allowedControllers.isEmpty();
//            }
//            if (permitted) {
//                JocPermissions perms = getJocPermissions(accessToken);
//                permitted = perms.getCalendars().getView() || perms.getDailyPlan().getView();
//            }
//
//            JOCDefaultResponse jocDefaultResponse = initPermissions(null, permitted);
//            if (jocDefaultResponse != null) {
//                return jocDefaultResponse;
//            }
//
//            Long yearFrom = in.getDateFrom() != null ? Long.valueOf(in.getDateFrom().split("-",2)[0]) : null;
//            Long yearTo = in.getDateTo() != null ? Long.valueOf(in.getDateTo().split("-",2)[0]) : null;
//            Integer monthFrom = in.getDateFrom() != null ? getMonth(in.getDateFrom()) : null;
//            Integer monthTo = in.getDateTo() != null ? getMonth(in.getDateTo()) : null;
//            Integer dayFrom = in.getDateFrom() != null ? getDay(in.getDateFrom()) : null;
//            Integer dayTo = in.getDateTo() != null ? getDay(in.getDateTo()) : null;
//            
//            Optional<Predicate<String>> pMonthFromTo = getMonthFromToPredicate(monthFrom, monthTo);
//            Optional<Predicate<String>> pDayFromTo = getDayFromToPredicate(dayFrom, dayTo);
//            Optional<Set<String>> scheduleNames = Optional.of(Collections.singleton(JocInventory.pathToName(in.getSchedulePath())));
//
//            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_SCHEDULES);
//            DBLayerDailyPlanProjections dbLayer = new DBLayerDailyPlanProjections(session);
//            List<DBItemDailyPlanProjection> items = dbLayer.getProjections(yearFrom, yearTo);
//            dbLayer.close();
//            session = null;
//
//            MetaItem metaItem = null;
//            Set<String> permittedSchedules = new HashSet<>();
//            boolean unPermittedSchedulesExist = false;
//            ProjectionsScheduleResponse entity = new ProjectionsScheduleResponse();
//            Date surveyDate = null;
//            
//            if (items != null) {
//                Optional<DBItemDailyPlanProjection> metaContentOpt = items.stream().filter(DBItemDailyPlanProjection::isMeta).findAny();
//                if (metaContentOpt.isPresent()) {
//                    surveyDate = metaContentOpt.get().getCreated();
//                    metaItem = Globals.objectMapper.readValue(metaContentOpt.get().getContent(), MetaItem.class);
//                    if (metaItem != null && metaItem.getAdditionalProperties() != null) {
//                        if (filterControllerIds(metaItem, allowedControllers)) {
//                            unPermittedSchedulesExist = true;
//                        }
//                        for (String controllerId : allowedControllers) {
//                            if (filterPermittedSchedules(metaItem.getAdditionalProperties().get(controllerId), folderPermissions.getListOfFolders(
//                                    controllerId), scheduleNames, permittedSchedules)) {
//                                unPermittedSchedulesExist = true;
//                            }
//                        }
//                    }
//                } else {
//                    throw new DBMissingDataException(
//                            "Couldn't find projections meta data. Maybe a recalculation of the projections is in progress right now.");
//                }
//                
//                List<DatePeriodItem> datePeriodItemItems = new ArrayList<>();
//                
//                final boolean unPermittedSchedulesExist2 = unPermittedSchedulesExist;
//                for (DBItemDailyPlanProjection item : items) {
//                    if (!item.isMeta()) {
//
//                        YearsItem yi = Globals.objectMapper.readValue(item.getContent(), YearsItem.class);
//                        yi.getAdditionalProperties().forEach((y, yearItem) -> {
//                            pMonthFromTo.ifPresent(p -> yearItem.getAdditionalProperties().keySet().removeIf(p));
//                            yearItem.getAdditionalProperties().forEach((m, monthItem) -> {
//                                pDayFromTo.ifPresent(p -> monthItem.getAdditionalProperties().keySet().removeIf(p));
//                                monthItem.getAdditionalProperties().forEach((d, dateItem) -> {
//                                    if (unPermittedSchedulesExist2) {
//                                        dateItem.getPeriods().removeIf(p -> !permittedSchedules.contains(p.getSchedule()));
//                                    }
//                                    dateItem.setNumOfPeriods(dateItem.getPeriods().size());
//                                    if (dateItem.getNumOfPeriods() != 0) {
//                                        dateItem.getPeriods().forEach(dpi -> {
//                                            dpi.setAdditionalProperty("planned", dateItem.getPlanned());
//                                            dpi.setAdditionalProperty("day", d);
//                                            datePeriodItemItems.add(dpi);
//                                        });
//                                    }
//                                });
//                            });
//                        });
//                    }
//                }
//                
//                datePeriodItemItems.stream().collect(Collectors.groupingBy(dpi -> (String) dpi.getAdditionalProperties().get("day"))).forEach((day,
//                        dpiList) -> {
//                    if (!dpiList.isEmpty()) {
//                        DateItem di = new DateItem();
//                        di.setPlanned((Boolean) dpiList.get(0).getAdditionalProperties().get("planned"));
//                        di.setNumOfPeriods(dpiList.size());
//                        di.setPeriods(null);
//                        entity.setAdditionalProperty(day, di);
//                    }
//                });
//                
//            }
//
//            entity.setDeliveryDate(Date.from(Instant.now()));
//            entity.setSurveyDate(surveyDate);
//            
//            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
//        } catch (JocException e) {
//            e.addErrorMetaInfo(getJocError());
//            return JOCDefaultResponse.responseStatusJSError(e);
//        } catch (Exception e) {
//            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
//        } finally {
//            Globals.disconnect(session);
//        }
//    }

//    private Optional<Predicate<String>> getMonthFromToPredicate(Integer monthFrom, Integer monthTo) {
//        Predicate<String> pMonthFrom = m -> getMonth(m) < monthFrom;
//        Predicate<String> pMonthTo = m -> getMonth(m) > monthTo;
//        
//        Predicate<String> pMonthFromTo = null;
//        if (monthFrom != null && !(monthFrom + "").endsWith("01")) {
//            pMonthFromTo = pMonthFrom;
//            if (monthTo != null && !(monthTo + "").endsWith("12")) {
//                pMonthFromTo = pMonthFrom.or(pMonthTo);
//            }
//        } else if (monthTo != null && !(monthTo + "").endsWith("12")) {
//            pMonthFromTo = pMonthTo;
//        }
//        return pMonthFromTo == null ? Optional.empty() : Optional.of(pMonthFromTo);
//    }
    
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
    
    private static void filterPermittedWorkflows(WorkflowsItem workflowsItem, Set<Folder> permittedFolders, Optional<Set<String>> workflowNames) {
        // boolean workflowsRemoved = false;

        if (workflowsItem != null && workflowsItem.getAdditionalProperties() != null) {
            // int numOfWorkflow = workflowsItem.getAdditionalProperties().keySet().size();

            workflowNames.ifPresent(wn -> workflowsItem.getAdditionalProperties().keySet().removeIf(workflow -> !wn.contains(JocInventory.pathToName(
                    workflow))));

            if (!permittedFolders.isEmpty()) {
                workflowsItem.getAdditionalProperties().keySet().removeIf(wPath -> !canAdd(wPath, permittedFolders));
            }
            
            // int newNumOfWorkflow = workflowsItem.getAdditionalProperties().keySet().size();
            // if (numOfWorkflow > newNumOfWorkflow) {
            // workflowsRemoved = true;
            // }
        }

        // return workflowsRemoved;
    }
    
    protected static boolean filterPermittedSchedules(ControllerInfoItem cii, Set<Folder> permittedFolders, Optional<Set<String>> scheduleNames,
            List<Folder> scheduleFolders, Optional<Set<String>> nonPeriodScheduleNames, Optional<Set<String>> workflowNames,
            List<Folder> workflowFolders, Set<String> permittedSchedules) {
        boolean schedulesRemoved = false;
    
        if (cii != null && cii.getAdditionalProperties() != null) {
            int numOfSchedules = cii.getAdditionalProperties().keySet().size();

            scheduleNames.ifPresent(sn -> cii.getAdditionalProperties().keySet().removeIf(schedule -> !sn.contains(JocInventory.pathToName(
                    schedule))));
            
            nonPeriodScheduleNames.ifPresent(sn -> cii.getAdditionalProperties().keySet().removeIf(schedule -> sn.contains(JocInventory.pathToName(
                    schedule))));
            
            Set<Folder> permittedFolders1 = new HashSet<>();
            if (scheduleFolders != null && !scheduleFolders.isEmpty()) {
                permittedFolders1.addAll(SOSAuthFolderPermissions.getPermittedFolders(scheduleFolders, permittedFolders));
                if (permittedFolders1.isEmpty()) { // no folder permissions
                    cii.getAdditionalProperties().clear();
                }
            } else {
                permittedFolders1.addAll(permittedFolders);
            }
            if (!permittedFolders1.isEmpty()) {
                cii.getAdditionalProperties().keySet().removeIf(schedule -> !canAdd(schedule, permittedFolders1));
            } 
            
            Set<Folder> permittedFolders2 = new HashSet<>();
            if (workflowFolders != null && !workflowFolders.isEmpty()) {
                permittedFolders2.addAll(SOSAuthFolderPermissions.getPermittedFolders(workflowFolders, permittedFolders));
                if (permittedFolders2.isEmpty()) { // no folder permissions
                    cii.getAdditionalProperties().clear();
                }
            } else {
                permittedFolders2.addAll(permittedFolders);
            }
            
            cii.getAdditionalProperties().values().forEach(sii -> {
                if (sii != null) {
                    if (!permittedFolders2.isEmpty() || workflowNames.isPresent()) {
                        filterPermittedWorkflows(sii.getWorkflows(), permittedFolders2, workflowNames);
                    }
                    sii.setWorkflowPaths(sii.getWorkflows().getAdditionalProperties().keySet());
                    sii.setWorkflows(null);
                    sii.setTotalOrders(null);
                }
            });
            cii.getAdditionalProperties().values().removeIf(sii -> sii == null || sii.getWorkflowPaths() == null || sii.getWorkflowPaths().isEmpty());

            int newNumOfSchedules = cii.getAdditionalProperties().keySet().size();
            if (numOfSchedules > newNumOfSchedules) {
                schedulesRemoved = true;
            }
            permittedSchedules.addAll(cii.getAdditionalProperties().keySet());
        }
        return schedulesRemoved;
    }
    
    public static Long getMonth(String month) {
        return Long.valueOf(month.replaceFirst("^(\\d{4})-(\\d{2}).*", "$1$2"));
    }
    
    private static Integer getDay(String day) {
        return Integer.valueOf(day.replace("-", ""));
    }

}
