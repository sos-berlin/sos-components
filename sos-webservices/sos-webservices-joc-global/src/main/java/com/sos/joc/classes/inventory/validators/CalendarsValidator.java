package com.sos.joc.classes.inventory.validators;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Frequencies;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class CalendarsValidator {

    public static void validate(IConfigurationObject config, InventoryDBLayer dbLayer) throws Exception {
        validateCalendarRefs((Calendar) config, dbLayer);
    }

    public static void revalidate(IConfigurationObject config, Map<ConfigurationType, Set<String>> invObjNames) throws Exception {
        validateCalendarRefs((Calendar) config, invObjNames.getOrDefault(ConfigurationType.WORKINGDAYSCALENDAR, Collections.emptySet()), invObjNames
                .getOrDefault(ConfigurationType.NONWORKINGDAYSCALENDAR, Collections.emptySet()));
    }

    private static void validateCalendarRefs(Calendar calendar, InventoryDBLayer dbLayer) throws SOSHibernateException, JocConfigurationException {
        List<String> calendarNames = getCalendarRefsInCalendar(calendar).stream().collect(Collectors.toList());
        validateCalendarRefs(calendarNames, dbLayer);
    }

    private static void validateCalendarRefs(Calendar calendar, Set<String> allWorkingDaysCalendarNames, Set<String> allNonWorkingDaysCalendarNames)
            throws JocConfigurationException {
        Set<String> calendarNames = getCalendarRefsInCalendar(calendar);

        calendarNames.removeAll(allWorkingDaysCalendarNames);
        calendarNames.removeAll(allNonWorkingDaysCalendarNames);
        if (!calendarNames.isEmpty()) {
            throw new JocConfigurationException("Missing assigned Calendars: " + calendarNames.toString());
        }
    }

    private static Set<String> getCalendarRefsInCalendar(Calendar calendar) {
        return Optional.ofNullable(calendar.getExcludes()).map(Frequencies::getNonWorkingDayCalendars).orElse(Collections.emptySet());
    }

    private static void validateCalendarRefs(List<String> calendarNames, InventoryDBLayer dbLayer) throws SOSHibernateException {
        if (!calendarNames.isEmpty()) {
            List<DBItemInventoryConfiguration> dbCalendars = dbLayer.getCalendarsByNames(calendarNames);
            if (dbCalendars == null || dbCalendars.isEmpty()) {
                throw new JocConfigurationException("Missing assigned Calendars: " + calendarNames.toString());
            } else if (dbCalendars.size() < calendarNames.size()) {
                calendarNames.removeAll(dbCalendars.stream().map(DBItemInventoryConfiguration::getName).collect(Collectors.toSet()));
                throw new JocConfigurationException("Missing assigned Calendars: " + calendarNames.toString());
            }
        }
    }

}
