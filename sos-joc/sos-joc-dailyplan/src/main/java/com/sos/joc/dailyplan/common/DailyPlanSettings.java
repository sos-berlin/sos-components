package com.sos.joc.dailyplan.common;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;

public class DailyPlanSettings {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanSettings.class);

    private List<ControllerConfiguration> controllers;
    private StartupMode startMode;
    private Date dailyPlanDate;
    private Date submissionTime;
    private String userAccount = "JS7";
    private String timeZone = "UTC";
    private String periodBegin = "00:00";
    private String dailyPlanStartTime;
    private boolean overwrite = false;
    private boolean submit = true;
    private int dayAheadPlan = 0;
    private int dayAheadSubmit = 0;
    private int projectionsMonthsAhead = 0;
    private boolean projectionsAheadConfiguredAsYears = false;

    public List<ControllerConfiguration> getControllers() {
        return controllers;
    }

    public void setControllers(List<ControllerConfiguration> val) {
        controllers = val;
    }

    public StartupMode getStartMode() {
        return startMode;
    }

    public void setStartMode(StartupMode val) {
        startMode = val;
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
        periodBegin = val;
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

    public int getDayAheadPlan() {
        return dayAheadPlan;
    }

    public void setDayAheadPlan(int val) {
        dayAheadPlan = val;
    }

    public int getDayAheadSubmit() {
        return dayAheadSubmit;
    }

    public void setDayAheadSubmit(int val) {
        dayAheadSubmit = val;
    }

    public void setDayAheadSubmit(String val) {
        try {
            this.dayAheadSubmit = Integer.parseInt(val);
        } catch (NumberFormatException e) {
            this.dayAheadSubmit = 0;
            LOGGER.warn("Could not set setting for dayAheadSubmit: " + val);
        }
    }

    public void setDayAheadPlan(String val) {
        try {
            this.dayAheadPlan = Integer.parseInt(val);
        } catch (NumberFormatException e) {
            this.dayAheadSubmit = 0;
            LOGGER.warn("Could not set setting for dayAheadPlan: " + val);
        }
    }

    public void setProjectionsMonthsAhead(DailyPlanSettings settings) {
        projectionsMonthsAhead = settings.getProjectionsMonthsAhead();
        projectionsAheadConfiguredAsYears = settings.isProjectionsAheadConfiguredAsYears();
    }

    public void setProjectionsMonthsAhead(String val) {
        try {
            projectionsAheadConfiguredAsYears = false;

            int months = 0;
            if (!SOSString.isEmpty(val)) {
                String v = val.toLowerCase().trim();
                int indx = v.indexOf("m");
                if (indx > 0) {
                    months = Integer.parseInt(v.substring(0, v.indexOf("m")).trim());
                } else {
                    indx = v.indexOf("y");
                    if (indx > 0) {
                        months = 12 * Integer.parseInt(v.substring(0, v.indexOf("y")).trim());
                        projectionsAheadConfiguredAsYears = true;
                    }
                }
            }
            projectionsMonthsAhead = months;
        } catch (Throwable e) {
            projectionsMonthsAhead = 0;
            LOGGER.warn("Could not set setting for projectionsAhead: " + val);
        }
    }

    public int getProjectionsMonthsAhead() {
        return projectionsMonthsAhead;
    }

    public boolean isProjectionsAheadConfiguredAsYears() {
        return projectionsAheadConfiguredAsYears;
    }
}
