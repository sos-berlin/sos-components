package com.sos.joc.db.inventory;

import org.hibernate.annotations.Proxy;
import org.hibernate.type.NumericBooleanConverter;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.VIEW_INV_SCHEDULE2CALENDARS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[SCHEDULE_NAME]", "[CALENDAR_NAME]" }) })
@Proxy(lazy = false)
public class DBItemInventorySchedule2Calendar extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[SCHEDULE_NAME]", nullable = false)
    private String scheduleName;

    @Column(name = "[SCHEDULE_PATH]", nullable = false)
    private String schedulePath;

    @Id
    @Column(name = "[CALENDAR_NAME]", nullable = true)
    private String calendarName;

    @Column(name = "[RELEASED]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean released;

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

    public String getCalendarName() {
        return calendarName;
    }

    public void setCalendarName(String val) {
        calendarName = val;
    }

    public boolean getReleased() {
        return released;
    }

    public void setReleased(boolean val) {
        released = val;
    }

}
