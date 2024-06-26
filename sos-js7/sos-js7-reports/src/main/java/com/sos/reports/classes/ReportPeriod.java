package com.sos.reports.classes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportPeriod {

    private static final int periodLengthDefault = 5;
    private static final int periodStepDefault = 5;
    private Integer periodLength = periodLengthDefault;
    private Integer periodStep = periodStepDefault;
    protected LocalDateTime from;
    protected LocalDateTime end;
    protected Long count;
    private LocalDateTime to;

    public LocalDateTime getFrom() {
        return from;
    }

    public void setFrom(LocalDateTime from) {
        int minute = from.getMinute();
        int offset = minute % periodLength;
        this.from = from.minusMinutes(offset);
        this.to = this.from.plusMinutes(periodLength);
        this.end = this.from.plusMinutes(periodLength);

    }

    public LocalDateTime getTo() {
        return this.to;
    }

    public void setEnd(LocalDateTime end) {
        if (end == null) {
            if (this.from != null) {
                this.end = this.from.plusMinutes(periodLength);
            }
        } else {
            this.end = end;
        }

        int minute = this.end.getMinute();
        int offset = minute % periodLength;
        this.end = this.end.minusMinutes(offset);
        this.end = this.end.plusMinutes(periodLength);

    }

    public void next() {
        this.from = this.to;
        this.to = this.from.plusMinutes(periodStep);
    }

    public boolean periodEnded() {
        return this.to.isAfter(this.end);
    }

    public String periodKey() {
        DateTimeFormatter formatterFrom = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:00");
        DateTimeFormatter formatterTo = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:00");
        return from.format(formatterFrom) + " - " + to.format(formatterTo);

    }

    public Integer getPeriodLength() {
        return periodLength;
    }

    public void setPeriodLength(Integer periodLength) {
        if (periodLength == null) {
            this.periodLength = periodLengthDefault;
        } else {
            this.periodLength = periodLength;
        }
    }

    public Integer getPeriodStep() {
        return periodStep;
    }

    public void setPeriodStep(Integer periodStep) {
        if (periodStep == null) {
            this.periodStep = periodStepDefault;
        } else {
            this.periodStep = periodStep;
        }
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public void addCount() {
        count += 1;
    }

    
    public void setTo(LocalDateTime to) {
        this.to = to;
    }

}
