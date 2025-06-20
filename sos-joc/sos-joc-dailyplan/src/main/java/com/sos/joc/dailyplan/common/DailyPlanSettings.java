package com.sos.joc.dailyplan.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.model.common.Folder;

public class DailyPlanSettings {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanSettings.class);

    public static final int DEFAULT_START_TIME_MINUTES_BEFORE_PRERIOD_BEGIN = 30;

    private List<ControllerConfiguration> controllers;
    private StartupMode startMode;

    private Date dailyPlanDate;
    private Date submissionTime;

    private String userAccount = "JS7";
    private String timeZone = SOSDate.TIMEZONE_UTC;
    private String periodBegin = "00:00";
    private String dailyPlanStartTime;

    private boolean periodBeginMidnight = true;
    private boolean overwrite = false;
    private boolean submit = true;

    // Projections(DONE) and Schedule Preview(TODO)
    // The DailyPlanRunner start time calculation returns OrderListSynchronizer, which contains absolute Periods.
    // - Period start times: contains the date and time.
    // - For Cyclic Periods : only one Period with an absolute begin and end
    private boolean calculateAbsoluteMainPeriodsOnly = false;
    private Set<Folder> permittedFolders;

    private int daysAheadPlan = 0;
    private int daysAheadSubmit = 0;
    private int projectionsMonthAhead = 0;
    private int ageOfPlansToBeClosedAutomatically = 1;

    private List<String> caller;

    public List<ControllerConfiguration> getControllers() {
        return controllers;
    }

    public void setControllers(List<ControllerConfiguration> val) {
        controllers = val;
    }

    public StartupMode getStartMode() {
        if (startMode == null) {
            startMode = StartupMode.run_now;
        }
        return startMode;
    }

    public void setStartMode(StartupMode val) {
        startMode = val;
    }

    public boolean isRunNow() {
        return StartupMode.run_now.equals(getStartMode());
    }

    public Date getDailyPlanDate() {
        return dailyPlanDate;
    }

    public void setDailyPlanDate(Date val) {
        dailyPlanDate = val;
    }

    public Date getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(Date val) {
        submissionTime = val;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String val) {
        userAccount = val;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String val) {
        timeZone = val;
    }

    public String getPeriodBegin() {
        return periodBegin;
    }

    public void setPeriodBegin(String val) {
        if (!SOSString.isEmpty(val)) {
            periodBeginMidnight = SOSDate.getTimeAsSeconds(val) == Long.valueOf(0L).longValue();
        }
        periodBegin = val;
    }

    public boolean isPeriodBeginMidnight() {
        return periodBeginMidnight;
    }

    public String getDailyPlanStartTime() {
        return dailyPlanStartTime;
    }

    public void setDailyPlanStartTime(String val) {
        dailyPlanStartTime = val;
    }

    public boolean isSubmit() {
        return submit;
    }

    public void setSubmit(boolean val) {
        submit = val;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean val) {
        overwrite = val;
    }

    public boolean isCalculateAbsoluteMainPeriodsOnly() {
        return calculateAbsoluteMainPeriodsOnly;
    }

    public void setCalculateAbsoluteMainPeriodsOnly(boolean val) {
        calculateAbsoluteMainPeriodsOnly = val;
    }

    public int getDaysAheadPlan() {
        return daysAheadPlan;
    }

    public void setDaysAheadPlan(int val) {
        daysAheadPlan = val;
    }

    public int getDaysAheadSubmit() {
        return daysAheadSubmit;
    }

    public void setDaysAheadSubmit(int val) {
        daysAheadSubmit = val;
    }

    public void setDaysAheadSubmit(String val) {
        try {
            this.daysAheadSubmit = Integer.parseInt(val);
        } catch (NumberFormatException e) {
            this.daysAheadSubmit = 0;
            LOGGER.warn("Could not set setting for daysAheadSubmit: " + val);
        }
    }

    public void setDaysAheadPlan(String val) {
        try {
            this.daysAheadPlan = Integer.parseInt(val);
        } catch (NumberFormatException e) {
            this.daysAheadSubmit = 0;
            LOGGER.warn("Could not set setting for daysAheadPlan: " + val);
        }
    }

    public void setProjectionsMonthAhead(int val) {
        projectionsMonthAhead = val;
    }

    public void setProjectionsMonthAhead(String val) {
        try {
            this.projectionsMonthAhead = Integer.parseInt(val);
        } catch (NumberFormatException e) {
            this.projectionsMonthAhead = 6;
            LOGGER.warn("Could not set setting for projectionsMonthAhead: " + val);
        }
    }

    public int getProjectionsMonthAhead() {
        return projectionsMonthAhead;
    }

    public void setAgeOfPlansToBeClosedAutomatically(int val) {
        ageOfPlansToBeClosedAutomatically = val;
    }

    public void setAgeOfPlansToBeClosedAutomatically(String val) {
        try {
            this.ageOfPlansToBeClosedAutomatically = Integer.parseInt(val);
        } catch (NumberFormatException e) {
            this.ageOfPlansToBeClosedAutomatically = 1;
            LOGGER.warn("Could not set setting for ageOfPlansToBeClosedAutomatically: " + val);
        }
    }

    public int getAgeOfPlansToBeClosedAutomatically() {
        return ageOfPlansToBeClosedAutomatically;
    }

    public void setCaller(String val) {
        caller = SOSString.isEmpty(val) ? null : Collections.singletonList(val);
    }

    public void setCaller(String parent, String child) {
        if (!SOSString.isEmpty(parent)) {
            caller = new ArrayList<>();
            caller.add(parent);
        }
        if (!SOSString.isEmpty(child)) {
            if (caller == null) {
                caller = new ArrayList<>();
            }
            caller.add(child);
        }
    }

    public void setCaller(List<String> elements) {
        caller = elements;
    }

    public List<String> getCaller() {
        return caller;
    }

    public Set<Folder> getPermittedFolders() {
        return permittedFolders;
    }

    public void setPermittedFolders(Set<Folder> val) {
        permittedFolders = val;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("time_zone=").append(timeZone);
        if (!isRunNow()) {
            sb.append(",period_begin=").append(periodBegin);
            sb.append(",start_time=");
            if (SOSString.isEmpty(dailyPlanStartTime)) {
                sb.append("<").append(DEFAULT_START_TIME_MINUTES_BEFORE_PRERIOD_BEGIN).append("m before period_begin>");
            } else {
                sb.append(dailyPlanStartTime);
            }
        }
        sb.append(",days_ahead_plan=").append(daysAheadPlan);
        sb.append(",days_ahead_submit=").append(daysAheadSubmit);
        sb.append(",projections_month_ahead=").append(projectionsMonthAhead);
        return sb.toString();
    }
}
