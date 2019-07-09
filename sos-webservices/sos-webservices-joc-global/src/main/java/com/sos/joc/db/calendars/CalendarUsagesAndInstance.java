package com.sos.joc.db.calendars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sos.jobscheduler.db.calendar.DBItemCalendarUsage;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.joc.model.calendar.Calendar;

public class CalendarUsagesAndInstance {

    private Set<DBItemCalendarUsage> calendarUsages = null;
    private DBItemInventoryInstance instance = null;
    private Calendar baseCalendar = null;
    private List<String> dates = new ArrayList<String>();
    private Map<String, Exception> exceptions = new HashMap<String, Exception>();
    private String calendarPath = null;
    private String oldCalendarPath = null;

    public CalendarUsagesAndInstance(DBItemInventoryInstance instance) {
        this.instance = instance;
    }

    public Set<DBItemCalendarUsage> getCalendarUsages() {
        return calendarUsages;
    }

    public void setCalendarUsages(List<DBItemCalendarUsage> calendarUsages) {
        if (calendarUsages != null && !calendarUsages.isEmpty()) {
            this.calendarUsages = new HashSet<DBItemCalendarUsage>(calendarUsages);
        }
    }

    public DBItemInventoryInstance getInstance() {
        return instance;
    }

    public Calendar getBaseCalendar() {
        return baseCalendar;
    }

    public void setBaseCalendar(Calendar baseCalendar) {
        this.baseCalendar = baseCalendar;
    }

    public List<String> getDates() {
        if (dates == null) {
            dates = new ArrayList<String>();
        }
        return dates;
    }

    public void setDates(List<String> dates) {
        this.dates = dates;
    }
    
    public String getCalendarPath() {
        return calendarPath;
    }

    public void setCalendarPath(String calendarPath) {
        this.calendarPath = calendarPath;
    }
    
    public String getOldCalendarPath() {
        return oldCalendarPath;
    }

    public void setOldCalendarPath(String oldCalendarPath) {
        this.oldCalendarPath = oldCalendarPath;
    }

    public void setAllEdited(Exception e) {
        if (this.calendarUsages != null) {
            for (DBItemCalendarUsage item : this.calendarUsages) {
                if (!item.getEdited()) {
                    item.setEdited(true);
                }
                String key = String.format("%1$s: %2$s on %3$s", item.getObjectType(), getCalendarPath(), instance.getUri());
                exceptions.put(key, e);
            }
        }
    }

    public Map<String, Exception> getExceptions() {
        return exceptions;
    }

    public void setExceptions(Map<String, Exception> exceptions) {
        this.exceptions = exceptions;
    }
    
    public void putException(DBItemCalendarUsage item, Exception exception) {
        String key = String.format("%1$s: %2$s on %3$s", item.getObjectType(), item.getPath(), instance.getUri());
        this.exceptions.put(key, exception);
    }

}
