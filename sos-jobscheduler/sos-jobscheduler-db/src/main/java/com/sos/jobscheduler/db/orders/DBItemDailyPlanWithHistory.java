package com.sos.jobscheduler.db.orders;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.sos.jobscheduler.db.history.DBItemOrder;

public class DBItemDailyPlanWithHistory {

    private int tolerance = 1;
    private int toleranceUnit = Calendar.MINUTE;

    private DBItemDailyPlannedOrders dbItemDailyPlannedOrders;
    private DBItemOrder dbItemOrder;

    public DBItemDailyPlanWithHistory(DBItemDailyPlannedOrders dbItemDailyPlannedOrders, DBItemOrder dbItemOrder) {
        super();
        this.dbItemDailyPlannedOrders = dbItemDailyPlannedOrders;
        this.dbItemOrder = dbItemOrder;
    }

    public DBItemOrder getDbItemOrder() {
        return dbItemOrder;
    }

    public DBItemDailyPlannedOrders getDbItemDailyPlannedOrders() {
        return dbItemDailyPlannedOrders;
    }

    public Boolean isLate() {
        Date planned = dbItemDailyPlannedOrders.getPlannedStart();
        Date start = null;
        if (dbItemOrder != null) {
            start = dbItemOrder.getStartTime();
        }

        if (start == null || start.getTime() == new Date(0).getTime()) {
            return planned.before(new Date());
        } else {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(planned);
            calendar.add(toleranceUnit, tolerance);
            Date scheduleToleranz = calendar.getTime();
            return start.after(scheduleToleranz);
        }
    }

    public String getState() {
        if (dbItemOrder == null) {
            return "planned";
        } else {
            return dbItemOrder.getStatus();
        }
    }

    public Integer getStartMode() {
        if (this.dbItemDailyPlannedOrders.getPeriodBegin() == null) {
            return 0;
        } else {
            return 1;
        }
    }

}