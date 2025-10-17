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

    // Row identifier: SCHEDULE_NAME, CALENDAR_NAME
    // @Id is not set due to:
    // - if a view DBItem defines @Id on a nullable field (or one of the composite keys references a nullable field),
    // - and this field is actually NULL in a record, Hibernate will return NULL instead of a DBItem for that row.
    // Setting @Id once for a view on a non-nullable field is enough
    // - Hibernate does not check this, and @Id is not needed for further processing (the view will not be stored or updated by Hibernate)
    @Column(name = "[CALENDAR_NAME]", nullable = true)
    private String calendarName;

    @Column(name = "[SCHEDULE_RELEASED]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean scheduleReleased;

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

    public boolean getScheduleReleased() {
        return scheduleReleased;
    }

    public void setScheduleReleased(boolean val) {
        scheduleReleased = val;
    }

}
