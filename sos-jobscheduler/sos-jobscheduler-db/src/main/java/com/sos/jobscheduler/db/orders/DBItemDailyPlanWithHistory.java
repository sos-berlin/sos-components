package com.sos.jobscheduler.db.orders;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.sos.jobscheduler.db.history.DBItemOrder;

public class DBItemDailyPlanWithHistory {

    private int tolerance = 2;
    private int toleranceUnit = Calendar.MINUTE;

    private DBItemDailyPlan dbItemDailyPlan;
    private DBItemOrder dbItemOrder;

    public DBItemDailyPlanWithHistory(DBItemDailyPlan dbItemDailyPlan, DBItemOrder dbItemOrder) {
        super();
        this.dbItemDailyPlan = dbItemDailyPlan;
        this.dbItemOrder = dbItemOrder;
    }

    public DBItemOrder getDbItemOrder() {
        return dbItemOrder;
    }

    public DBItemDailyPlan getDbItemDailyPlan() {
        return dbItemDailyPlan;
    }

    public Boolean isLate() {
        Date planned = dbItemDailyPlan.getPlannedStart();
        Date start = dbItemOrder.getStartTime();

        if (start == null) {
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
            return dbItemOrder.getState();
        }
    }

    public Integer getStartMode() {
        if (this.dbItemDailyPlan.getPeriodBegin() == null) {
            return 0;
        } else {
            return 1;
        }
    }

}