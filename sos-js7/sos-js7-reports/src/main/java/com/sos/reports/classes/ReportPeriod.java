package com.sos.reports.classes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportPeriod {

    private static final int periodLength = 5;
    private static final int periodStep = 5;
    protected LocalDateTime from;
    protected LocalDateTime end;
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

}
