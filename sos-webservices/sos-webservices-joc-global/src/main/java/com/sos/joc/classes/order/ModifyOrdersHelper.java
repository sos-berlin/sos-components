package com.sos.joc.classes.order;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.model.dailyplan.DailyPlanModifyOrder;

public class ModifyOrdersHelper extends DailyPlanModifyOrder {
    
    @JsonIgnore
    private boolean isDateWithoutTime = false;
    @JsonIgnore
    private Optional<Long> secondsFromCurDate = Optional.empty();
    @JsonIgnore
    private Instant utcScheduledFor = null;
    @JsonIgnore
    private boolean initialized = false;
    
    
    public void initScheduledFor() {
        isDateWithoutTime = getScheduledFor() == null ? false : getScheduledFor().matches("\\d{4}-\\d{2}-\\d{2}");
        secondsFromCurDate = JobSchedulerDate.getSecondsOfRelativeCurDate(getScheduledFor());
        setUtcScheduledFor();
        initialized = true;
    }
    
    public Instant getNewPlannedStart(final Date oldPlannedStart) {
        if (isDateWithoutTime) {
            // use time from old planned start
            return JobSchedulerDate.convertUTCDate(getScheduledFor(), oldPlannedStart.toInstant(), getTimeZone());
        } else if (secondsFromCurDate.isPresent()) {
            return oldPlannedStart.toInstant().plusSeconds(secondsFromCurDate.get());
        }
        return utcScheduledFor;
    }
    
    protected static boolean isCurDate(String datetime) {
        return datetime == null ? false : datetime.matches("cur\\s*[-+]\\s*(\\d{1,2}:\\d{1,2}(:\\d{1,2})?|\\d+)");
    }
    
    private Optional<Instant> getScheduledForInUTC(String datetime, String timeZone) {
        if (isDateWithoutTime) {
            return Optional.of(Instant.parse(datetime + "T00:00:00Z").plusSeconds(86400));
        } else {
            return JobSchedulerDate.getScheduledForInUTC(datetime, timeZone);
        }
    }
    
    private void setUtcScheduledFor() throws JocBadRequestException {
        utcScheduledFor = Instant.now();
        // a "cur" date cannot be checked if future or not and a date without time only roughly
        if (!isCurDate(getScheduledFor())) {

            Optional<Instant> scheduledForUtc = getScheduledForInUTC(getScheduledFor(), getTimeZone());
            if (scheduledForUtc.isPresent()) { // not present for now
                if (scheduledForUtc.get().isBefore(utcScheduledFor)) {
                    throw new JocBadRequestException("The new planned start must be in the future.");
                }
                utcScheduledFor = scheduledForUtc.get();
            }
        }
    }
}
