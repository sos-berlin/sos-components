package com.sos.joc.order.impl;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.calendar.Calendar;
import com.sos.joc.model.calendar.Period;
import com.sos.joc.model.dailyplan.RunTime;
import com.sos.joc.model.order.OrderTemplateDatesFilter;
import com.sos.joc.order.resource.IOrderTemplatePeriodsResource;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.initiator.model.AssignedCalendars;
import com.sos.webservices.order.initiator.model.AssignedNonWorkingCalendars;

@Path("order_template")
public class OrderTemplatePeriodsResourceImpl extends JOCResourceImpl implements IOrderTemplatePeriodsResource {

    private static final String API_CALL = "./order_template/runtime";
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC);
    private static DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_INSTANT;

    @Override
    public JOCDefaultResponse postOrderTemplatePeriods(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            JsonValidator.validateFailFast(filterBytes, OrderTemplateDatesFilter.class);
            OrderTemplateDatesFilter in = Globals.objectMapper.readValue(filterBytes, OrderTemplateDatesFilter.class);
            // TODO permission
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, in, accessToken, null, getPermissonsJocCockpit(null, accessToken).getOrder()
                    .getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            RunTime entity = new RunTime();
            
            if (in.getCalendars() != null && !in.getCalendars().isEmpty()) {
                
                if (in.getTimeZone() == null || in.getTimeZone().isEmpty()) {
                    in.setTimeZone("Etc/UTC");
                }
                final ZoneId timezone = ZoneId.of(in.getTimeZone());
                
                FrequencyResolver fr = new FrequencyResolver();
                if (in.getDateFrom() == null) {
                    in.setDateFrom(fr.getToday());
                }
                if (in.getDateTo() == null) {
                    in.setDateTo(fr.getLastDayOfCurrentYear());
                }
                
                session = Globals.createSosHibernateStatelessConnection(API_CALL);
                InventoryDBLayer dbLayer = new InventoryDBLayer(session);
                
                final List<String> nonWorkingDays = getNonWorkingDays(dbLayer, in);

                List<DBItemInventoryConfiguration> workingDbCalendars = dbLayer.getCalendars(in.getCalendars().stream().map(
                        AssignedCalendars::getCalendarPath));
                
                SortedSet<Period> periods = new TreeSet<>(Comparator.comparing(p -> p.getSingleStart() == null ? p.getBegin() : p.getSingleStart()));
                
                if (workingDbCalendars != null && !workingDbCalendars.isEmpty()) {
                    // maybe filter by type
                    Map<String, String> pathContentMap = workingDbCalendars.stream().collect(Collectors.toMap(DBItemInventoryConfiguration::getPath,
                            DBItemInventoryConfiguration::getContent));

                    for (AssignedCalendars c : in.getCalendars()) {
                        if (!pathContentMap.containsKey(c.getCalendarPath())) {
                            continue;
                        }
                        Calendar restrictions = new Calendar();
                        restrictions.setIncludes(c.getIncludes());
                        //restrictions.setExcludes(c.getExcludes());
                        Calendar basedOn = Globals.objectMapper.readValue(pathContentMap.get(c.getCalendarPath()), Calendar.class);
                        fr.resolveRestrictions(basedOn, restrictions, in.getDateFrom(), in.getDateTo()).getDates().stream().flatMap(
                                date -> getPeriods(c.getPeriods(), nonWorkingDays, date, timezone)).collect(Collectors.toCollection(() -> periods));
                    }
                }
                entity.setPeriods(new ArrayList<>(periods));
            }
            
            entity.setDeliveryDate(Date.from(Instant.now()));
            
            return JOCDefaultResponse.responseStatus200(entity);

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private List<String> getNonWorkingDays(InventoryDBLayer dbLayer, OrderTemplateDatesFilter in) throws SOSHibernateException, JsonParseException,
            JsonMappingException, IOException, SOSMissingDataException, SOSInvalidDataException {
        FrequencyResolver fr = new FrequencyResolver();
        List<String> nonWorkingDays = new ArrayList<>();
        
        if (in.getNonWorkingCalendars() != null && !in.getNonWorkingCalendars().isEmpty()) {
            List<DBItemInventoryConfiguration> nonWorkingDbCalendars = dbLayer.getCalendars(in.getNonWorkingCalendars().stream().map(
                    AssignedNonWorkingCalendars::getCalendarPath));

            if (nonWorkingDbCalendars != null && !nonWorkingDbCalendars.isEmpty()) {

                Map<String, String> pathContentMap = nonWorkingDbCalendars.stream().collect(Collectors.toMap(DBItemInventoryConfiguration::getPath,
                        DBItemInventoryConfiguration::getContent));

                for (AssignedNonWorkingCalendars c : in.getNonWorkingCalendars()) {
                    if (!pathContentMap.containsKey(c.getCalendarPath())) {
                        continue;
                    }
                    Calendar basedOn = Globals.objectMapper.readValue(pathContentMap.get(c.getCalendarPath()), Calendar.class);
                    nonWorkingDays.addAll(fr.resolve(basedOn, in.getDateFrom(), in.getDateTo()).getDates());
                }
            }
        }
        return nonWorkingDays;
    }
    
    private static Stream<Period> getPeriods(List<Period> periods, List<String> holidays, String date, ZoneId timezone) {
        if (periods == null) {
            return Stream.empty();
        }
        return periods.stream().map(p -> getPeriod(p, holidays, date, timezone)).filter(Objects::nonNull);
    }
    
    private static Period getPeriod(Period period, List<String> holidays, String date, ZoneId timezone) {
        Period p = new Period();
        
        if (holidays.contains(date)) {
            if (period.getWhenHoliday() != null) {
                switch (period.getWhenHoliday()) {
                case SUPPRESS:
                    return null;
                case NEXTNONWORKINGDAY: 
                    try {
                        java.util.Calendar dateCal = FrequencyResolver.getCalendarFromString(date);
                        dateCal.add(java.util.Calendar.DATE, 1);
                        date = dateFormatter.format(dateCal.toInstant());
                        while (holidays.contains(date)) {
                            dateCal.add(java.util.Calendar.DATE, 1);
                            date = dateFormatter.format(dateCal.toInstant());
                        }
                    } catch (SOSInvalidDataException e) {
                        e.printStackTrace();
                        return null;
                    }
                    break;
                case PREVIOUSNONWORKINGDAY:
                    try {
                        java.util.Calendar dateCal = FrequencyResolver.getCalendarFromString(date);
                        dateCal.add(java.util.Calendar.DATE, -1);
                        date = dateFormatter.format(dateCal.toInstant());
                        while (holidays.contains(date)) {
                            dateCal.add(java.util.Calendar.DATE, -1);
                            date = dateFormatter.format(dateCal.toInstant());
                        }
                    } catch (SOSInvalidDataException e) {
                        e.printStackTrace();
                        return null;
                    }
                    break;
                case IGNORE: 
                    break;
                }
            } else {
                return null;
            }
        }
        
        if (period.getSingleStart() != null) {
            p.setSingleStart(isoFormatter.format(ZonedDateTime.of(LocalDateTime.parse(date + "T" + period.getSingleStart(), dateTimeFormatter),
                    timezone)));
            return p;
        }
        if (period.getRepeat() != null && !period.getRepeat().isEmpty()) {
            p.setRepeat(period.getRepeat());
            String begin = period.getBegin();
            if (begin == null || begin.isEmpty()) {
                begin = "00:00:00";
            }

            p.setBegin(isoFormatter.format(ZonedDateTime.of(LocalDateTime.parse(date + "T" + begin, dateTimeFormatter), timezone)));
            String end = period.getEnd();
            if (end == null || end.isEmpty()) {
                end = "24:00:00";
            }
            if (end.startsWith("24:00")) {
                p.setEnd(isoFormatter.format(ZonedDateTime.of(LocalDateTime.parse(date + "T23:59:59", dateTimeFormatter).plusSeconds(1L), timezone)));
            } else {
                p.setEnd(isoFormatter.format(ZonedDateTime.of(LocalDateTime.parse(date + "T" + end, dateTimeFormatter), timezone)));
            }
            return p;
        }
        return null;
    }

}
