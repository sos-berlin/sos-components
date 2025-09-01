package com.sos.joc.classes.calendar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.CalendarType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.model.inventory.common.ConfigurationType;

public abstract class ACalendarBaseResourceImpl extends JOCResourceImpl {

    public Map<String, Calendar> getNonWorkingDayCalendars(InventoryDBLayer dbLayer, Calendar baseCalendar) throws Exception {
        Map<String, Calendar> map = new HashMap<>();
        if (baseCalendar.getExcludes() == null || SOSCollection.isEmpty(baseCalendar.getExcludes().getNonWorkingDayCalendars())) {
            return map;
        }

        List<DBItemInventoryReleasedConfiguration> dbItems = dbLayer.getReleasedConfigurations(baseCalendar.getExcludes().getNonWorkingDayCalendars()
                .stream().collect(Collectors.toList()), ConfigurationType.NONWORKINGDAYSCALENDAR);

        int dbItemsCount = dbItems == null ? 0 : dbItems.size();
        if (baseCalendar.getExcludes().getNonWorkingDayCalendars().size() != dbItemsCount) {
            throw new DBMissingDataException(String.format("[%s][getNonWorkingDayCalendars][configured=%s][found released]%s", baseCalendar.getPath(),
                    SOSString.join(baseCalendar.getExcludes().getNonWorkingDayCalendars(), SOSString.join(dbItems.stream().map(i -> i.getName())
                            .collect(Collectors.toList())))));
        }

        for (DBItemInventoryReleasedConfiguration dbItem : dbItems) {
            checkFolderPermissions(dbItem.getPath());

            Calendar c = Globals.objectMapper.readValue(dbItem.getContent(), Calendar.class);
            c.setType(CalendarType.NONWORKINGDAYSCALENDAR);
            c.setId(dbItem.getId());
            c.setPath(dbItem.getPath());
            c.setName(dbItem.getName());
            c.setTitle(dbItem.getTitle());

            map.put(dbItem.getName(), c);
        }
        return map;
    }
}
