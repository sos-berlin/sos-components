package com.sos.joc.db.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.Proxy;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.VIEW_INV_RELEASED_SCHEDULE2CALENDARS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[SCHEDULE_NAME]",
        "[CALENDAR_NAME]" }) })
@Proxy(lazy = false)
public class DBItemInventoryReleasedSchedule2Calendar extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[CALENDAR_NAME]", nullable = false)
    private String calendarName;

    @Id
    @Column(name = "[SCHEDULE_NAME]", nullable = false)
    private String scheduleName;

    @Column(name = "[SCHEDULE_PATH]", nullable = false)
    private String schedulePath;

    @Column(name = "[SCHEDULE_FOLDER]", nullable = false)
    private String scheduleFolder;

    public String getCalendarName() {
        return calendarName;
    }

    public void setCalendarName(String val) {
        calendarName = val;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String val) {
        scheduleName = val;
    }

    public String getSchedulePath() {
        return schedulePath;
    }

    public void setSchedulePath(String val) {
        schedulePath = val;
    }

    public String getScheduleFolder() {
        return scheduleFolder;
    }

    public void setScheduleFolder(String val) {
        scheduleFolder = val;
    }

}
