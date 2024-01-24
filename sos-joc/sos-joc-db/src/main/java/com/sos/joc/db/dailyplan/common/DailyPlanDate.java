package com.sos.joc.db.dailyplan.common;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DailyPlanDate {

    public static final String PERIOD_DEFAULT_DATE = "2000-01-01";
//    private static final String datetimeFormat = "yyyy-MM-dd HH:mm:ss";

    public static Long getRepeatInterval(String repeat) throws ParseException {
        return getSecondsfromHHMMSS(repeat);
    }

    public static Date getPeriodAsDate(Date dailyPlanDate, String time) throws ParseException {
        if (time == null) {
            return null;
        }
        Long seconds = getSecondsfromHHMMSS(time);
        if (seconds == null) {
            return null;
        }
        return Date.from(dailyPlanDate.toInstant().plusSeconds(seconds));
//        SimpleDateFormat formatter = new SimpleDateFormat(datetimeFormat);
//        return formatter.parse(PERIOD_DEFAULT_DATE + " " + time);
    }
    
    private static Long getSecondsfromHHMMSS(String hhmmss) {
        if (hhmmss != null) {
            String sf = hhmmss.trim();
            if (hhmmss.matches("\\d{1,2}:\\d{1,2}(:\\d{1,2})?")) {
                Matcher m = Pattern.compile("^(\\d{1,2}):(\\d{1,2}):(\\d{1,2})").matcher(sf + ":00");
                if (m.find()) {
                    return ((Long.parseLong(m.group(1)) * 60 * 60) + (Long.parseLong(m.group(2)) * 60) + Long.parseLong(m.group(3)));
                }
            }
        }
        return null;
    }

}