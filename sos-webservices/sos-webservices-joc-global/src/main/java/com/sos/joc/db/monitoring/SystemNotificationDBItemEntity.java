package com.sos.joc.db.monitoring;

import java.util.Date;

public class SystemNotificationDBItemEntity {

    private Long id;
    private Integer type;
    private Integer category;
    private String jocId;
    private String source;
    private String notifier;
    private Date time;
    private String message;
    private String exception;
    private boolean hasMonitors;
    private Date created;

    private String acknowledgementAccount;
    private String acknowledgementComment;
    private Date acknowledgementCreated;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer val) {
        type = val;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer val) {
        category = val;
    }

    public String getJocId() {
        return jocId;
    }

    public void setJocId(String val) {
        jocId = val;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String val) {
        source = val;
    }

    public String getNotifier() {
        return notifier;
    }

    public void setNotifier(String val) {
        notifier = val;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date val) {
        time = val;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String val) {
        message = val;
    }

    public String getException() {
        return exception;
    }

    public void setException(String val) {
        exception = val;
    }

    public boolean getHasMonitors() {
        return hasMonitors;
    }

    public void setHasMonitors(boolean val) {
        hasMonitors = val;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date val) {
        created = val;
    }

    public String getAcknowledgementAccount() {
        return acknowledgementAccount;
    }

    public void setAcknowledgementAccount(String val) {
        acknowledgementAccount = val;
    }

    public String getAcknowledgementComment() {
        return acknowledgementComment;
    }

    public void setAcknowledgementComment(String val) {
        acknowledgementComment = val;
    }

    public Date getAcknowledgementCreated() {
        return acknowledgementCreated;
    }

    public void setAcknowledgementCreated(Date val) {
        acknowledgementCreated = val;
    }

}
