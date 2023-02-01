package com.sos.joc.db.monitoring;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.EqualsBuilder;

@Embeddable
public class DBItemNotificationAcknowledgementId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "[NOT_ID]", nullable = false)
    private Long notificationId;

    @Column(name = "[APPLICATION]", nullable = false)
    private Integer application;

    public DBItemNotificationAcknowledgementId() {
        this.notificationId = null;
        this.application = null;
    }

    public DBItemNotificationAcknowledgementId(Long notificationId, Integer application) {
        this.notificationId = notificationId;
        this.application = application;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long val) {
        notificationId = val;
    }

    public Integer getApplication() {
        return application;
    }

    public void setApplication(Integer val) {
        application = val;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        Class<?> otherClazz = other.getClass();
        if (!(otherClazz.isInstance(this))) {
            return false;
        }

        try {
            DBItemNotificationAcknowledgementId o = (DBItemNotificationAcknowledgementId) other;
            EqualsBuilder eb = new EqualsBuilder();
            eb.append(notificationId, o.getNotificationId());
            eb.append(application, o.getApplication());
            return eb.isEquals();
        } catch (Throwable ex) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNotificationId(), getApplication());
    }

}
