package com.sos.jobscheduler.history.helper;

import com.sos.jobscheduler.db.history.DBItemMaster;

public class CachedMaster {

    private final Long id;
    private String schedulerId;
    private String timezone;

    public CachedMaster(final DBItemMaster item) {
        id = item.getId();
        schedulerId = item.getSchedulerId();
        timezone = item.getTimezone();
    }

    public Long getId() {
        return id;
    }

    public void setSchedulerId(String val) {
        schedulerId = val;
    }

    public String getSchedulerId() {
        return schedulerId;
    }

    public void setTimezone(String val) {
        timezone = val;
    }

    public String getTimezone() {
        return timezone;
    }

}
