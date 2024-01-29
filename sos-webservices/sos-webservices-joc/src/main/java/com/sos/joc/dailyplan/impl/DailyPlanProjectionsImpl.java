package com.sos.joc.dailyplan.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
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
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.dailyplan.DailyPlanRunner;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.common.ProjectionsImpl;
import com.sos.joc.dailyplan.db.DBLayerDailyPlanProjections;
import com.sos.joc.dailyplan.resource.IDailyPlanProjectionsResource;
import com.sos.joc.db.dailyplan.DBItemDailyPlanProjection;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.projections.ProjectionsCalendarResponse;
import com.sos.joc.model.dailyplan.projections.ProjectionsRequest;
import com.sos.joc.model.dailyplan.projections.items.meta.MetaItem;
import com.sos.joc.model.dailyplan.projections.items.year.DateItem;
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
public class DailyPlanProjectionsImpl extends ProjectionsImpl implements IDailyPlanProjectionsResource {

    // Export
    @Override
    public JOCDefaultResponse datesProjections(String accessToken, String acceptEncoding, byte[] filterBytes) {
        return projections(accessToken, acceptEncoding, filterBytes, IMPL_PATH_DATES);
    }

    // Month/Year
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

            boolean invertedProjection = in.getWithoutStartTime() != null && in.getWithoutStartTime();
            boolean export = action == IMPL_PATH_DATES;

            if (items != null) {
                metaOpt = items.stream().filter(DBItemDailyPlanProjection::isMeta).findAny();
                metaContentOpt = metaOpt.map(m -> {
                    try {
                        return Globals.objectMapper.readValue(m.getContent(), MetaItem.class);
                    } catch (Exception e) {
                        throw new DBInvalidDataException(e);
                    }
                });

                Set<String> permittedSchedules = new HashSet<>();
                final boolean unPermittedSchedulesExist = setPermittedSchedules(metaContentOpt, allowedControllers, scheduleNames, in
                        .getScheduleFolders(), workflowNames, in.getWorkflowFolders(), permittedSchedules, folderPermissions);

                Set<String> schedulesExcludedFromProjection = getSchedulesExcludedFromProjection(metaContentOpt, false);
                for (DBItemDailyPlanProjection item : items) {
                    setYearsItem(item, invertedProjection, export, schedulesExcludedFromProjection, unPermittedSchedulesExist, permittedSchedules,
                            pDayFromTo, yearsItem);
                }

                if (export) {
                    removeObsoleteSchedulesFromMetaData(metaContentOpt, allowedControllers, invertedProjection, permittedSchedules.size(), yearsItem);
                }
            }

            ProjectionsCalendarResponse entity = new ProjectionsCalendarResponse();
            entity.setDeliveryDate(Date.from(Instant.now()));
            metaOpt.ifPresent(meta -> entity.setSurveyDate(meta.getCreated()));
            entity.setYears(yearsItem);

            if (export) {
                return export(acceptEncoding, entity, metaContentOpt);
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

    private JOCDefaultResponse export(String acceptEncoding, ProjectionsCalendarResponse entity, Optional<MetaItem> metaContentOpt) {
        metaContentOpt.ifPresent(meta -> entity.setMeta(meta));

        boolean withGzipEncoding = acceptEncoding != null && acceptEncoding.contains("gzip");
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
    }

    private Map<String, Object> getGzipHeaders(boolean withGzipEncoding) {
        Map<String, Object> headers = new HashMap<String, Object>();
        if (withGzipEncoding) {
            headers.put("Content-Encoding", "gzip");
        }
        headers.put("Transfer-Encoding", "chunked");
        return headers;
    }

    private boolean setPermittedSchedules(Optional<MetaItem> metaContentOpt, Set<String> allowedControllers, Optional<Set<String>> scheduleNames,
            List<Folder> scheduleFolders, Optional<Set<String>> workflowNames, List<Folder> workflowFolders, Set<String> permittedSchedules,
            SOSAuthFolderPermissions folderPermissions) throws DBMissingDataException {
        return setPermittedSchedules(metaContentOpt, allowedControllers, scheduleNames, scheduleFolders, Optional.empty(), workflowNames,
                workflowFolders, permittedSchedules, folderPermissions);
    }

    private void removeObsoleteSchedulesFromMetaData(Optional<MetaItem> metaContentOpt, Set<String> allowedControllers, boolean invertedProjection,
            int numOfPermittedSchedules, YearsItem yearsItem) {
        Set<String> schedules = null;
        try {
            Stream<DateItem> dateItemStream = yearsItem.getAdditionalProperties().values().stream().map(MonthsItem::getAdditionalProperties).map(
                    Map::values).flatMap(Collection::stream).map(MonthItem::getAdditionalProperties).map(Map::values).flatMap(Collection::stream);
            Stream<DatePeriodItem> datePeriodItemStream = Stream.empty();
            if (invertedProjection) {
                datePeriodItemStream = dateItemStream.map(DateItem::getNonPeriods).filter(Objects::nonNull).flatMap(List::stream);
            } else {
                datePeriodItemStream = dateItemStream.map(DateItem::getPeriods).filter(Objects::nonNull).flatMap(List::stream);
            }
            schedules = datePeriodItemStream.map(DatePeriodItem::getSchedule).filter(Objects::nonNull).map(JocInventory::pathToName).collect(
                    Collectors.toSet());
        } catch (Exception e) {
            //
        }

        if (schedules != null && schedules.size() < numOfPermittedSchedules) {
            setPermittedSchedules(metaContentOpt, allowedControllers, Optional.of(schedules), null, Optional.empty(), Optional.empty(), null,
                    new HashSet<>(), null);
        }
    }

    private void setYearsItem(DBItemDailyPlanProjection item, boolean invertedProjection, boolean export, Set<String> schedulesExcludedFromProjection,
            boolean unPermittedSchedulesExist, Set<String> permittedSchedules, Optional<Predicate<String>> pDayFromTo, YearsItem yearsItem)
            throws StreamReadException, DatabindException, IOException {
        if (!item.isMeta()) {
            final int numOfPermittedSchedules = permittedSchedules.size();
            final Integer numOfPermittedSchedulesForProjectionDays;
            if (schedulesExcludedFromProjection.size() > 0) {// removed or "Plan Order automatically" deactivated
                numOfPermittedSchedulesForProjectionDays = Long.valueOf(permittedSchedules.stream().filter(s -> !schedulesExcludedFromProjection
                        .contains(s)).count()).intValue();
            } else {
                numOfPermittedSchedulesForProjectionDays = numOfPermittedSchedules;
            }

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
                boolean isPlanned = isPlanned(dateItem);
                if (invertedProjection) {
                    if (export) {
                        Set<String> schedulesOfTheDay = dateItem.getPeriods().stream().map(DatePeriodItem::getSchedule).filter(Objects::nonNull)
                                .collect(Collectors.toSet());

                        dateItem.setNonPeriods(permittedSchedules.stream().filter(s -> isPlanned ? true : !schedulesExcludedFromProjection.contains(
                                s)).filter(s -> !schedulesOfTheDay.contains(s)).map(s -> {
                                    DatePeriodItem dpi = new DatePeriodItem();
                                    dpi.setSchedule(s);
                                    return dpi;
                                }).collect(Collectors.toList()));
                    } else {
                        int numOfschedulesOfTheDay = dateItem.getPeriods().stream().map(DatePeriodItem::getSchedule).filter(Objects::nonNull)
                                .distinct().mapToInt(i -> 1).sum();

                        int total = numOfPermittedSchedules;
                        if (!isPlanned) {
                            total = numOfPermittedSchedulesForProjectionDays;
                        }
                        dateItem.setNumOfNonPeriods(total - numOfschedulesOfTheDay);
                    }
                    dateItem.setPeriods(null);
                } else {
                    if (!export) {
                        dateItem.setNumOfPeriods(dateItem.getPeriods().size());
                        dateItem.setPeriods(null);
                    }
                }
            });

            if (invertedProjection) {
                if (export) {
                    monthItem.getAdditionalProperties().values().removeIf(dateItem -> dateItem.getNonPeriods().isEmpty());
                } else {
                    monthItem.getAdditionalProperties().values().removeIf(dateItem -> dateItem.getNumOfNonPeriods() == 0);
                }
            } else {
                if (export) {
                    monthItem.getAdditionalProperties().values().removeIf(dateItem -> dateItem.getPeriods().isEmpty());
                } else {
                    monthItem.getAdditionalProperties().values().removeIf(dateItem -> dateItem.getNumOfPeriods() == 0);
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

    private Integer getDay(String day) {
        return Integer.valueOf(day.replace("-", ""));
    }

}
