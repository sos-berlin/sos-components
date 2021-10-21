package com.sos.joc.db.dailyplan.classes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DailyPlanDate {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanDate.class);
    private final String conClassName = "DailyScheduleDate";
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";
    private String dateFormatOnlyDay = "yyyy-MM-dd";
    private String isoDateFormat = "yyyy-MM-dd HH:mm:ss";

    private Date schedule;
    private String isoDate;

    public DailyPlanDate(String dateFormat_) {
        this.dateFormat = dateFormat_;
    }

    public DailyPlanDate() {
     }

    private void setIsoDate() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(isoDateFormat);
        this.isoDate = formatter.format(schedule);
    }

    public void setSchedule(String schedule) throws ParseException {
        
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        if ("now".equals(schedule)) {
            this.schedule = new Date();
        } else {
            this.schedule = formatter.parse(schedule);
        }
        this.setIsoDate();
    }

    
   public void setSchedule(String dateformat, String schedule) throws ParseException {
        
        SimpleDateFormat formatter = new SimpleDateFormat(dateformat);
        if ("now".equals(schedule)) {
            this.schedule = new Date();
        } else {
            this.schedule = formatter.parse(schedule);
        }
        this.setIsoDate();
    }
   
    public void setSchedule(Date start,String schedule) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormatOnlyDay);
        String d = formatter.format(start);
        formatter = new SimpleDateFormat(dateFormat);
        schedule = d + " " + schedule;
        if ("now".equals(schedule)) {
            this.schedule = new Date();
        } else {
            this.schedule = formatter.parse(schedule);
        }
        this.setIsoDate();
    }
    
    public void setPeriod(Date start,String schedule) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormatOnlyDay);
        String d = formatter.format(start);
        formatter = new SimpleDateFormat(dateFormat);
        schedule = "2000-01-01" + " " + schedule;
        if ("now".equals(schedule)) {
            this.schedule = new Date();
        } else {
            this.schedule = formatter.parse(schedule);
        }
        this.setIsoDate();
    }
    
    public Date getSchedule() {
        return schedule;
    }

    public String getIsoDate() {
        return isoDate;
    }

    public void setSchedule(Date schedule) {
        this.schedule = schedule;
        try {
            this.setIsoDate();
        } catch (ParseException e) {
            LOGGER.info(conClassName + ".setScheduler: Could not set Iso-Date");
        }
    }

}