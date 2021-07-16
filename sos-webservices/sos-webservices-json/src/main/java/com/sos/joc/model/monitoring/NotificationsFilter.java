
package com.sos.joc.model.monitoring;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.MonitoringNotificationTypeText;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * notifications filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "dateFrom",
    "timeZone",
    "limit",
    "types",
    "notificationIds"
})
public class NotificationsFilter {

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFrom")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String dateFrom;
    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timeZone;
    /**
     * to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JsonPropertyDescription("to restrict the number of responsed records; -1=unlimited")
    private Integer limit = 10000;
    @JsonProperty("types")
    private List<MonitoringNotificationTypeText> types = new ArrayList<MonitoringNotificationTypeText>();
    @JsonProperty("notificationIds")
    private List<Long> notificationIds = new ArrayList<Long>();

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @JsonProperty("types")
    public List<MonitoringNotificationTypeText> getTypes() {
        return types;
    }

    @JsonProperty("types")
    public void setTypes(List<MonitoringNotificationTypeText> types) {
        this.types = types;
    }

    @JsonProperty("notificationIds")
    public List<Long> getNotificationIds() {
        return notificationIds;
    }

    @JsonProperty("notificationIds")
    public void setNotificationIds(List<Long> notificationIds) {
        this.notificationIds = notificationIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("dateFrom", dateFrom).append("timeZone", timeZone).append("limit", limit).append("types", types).append("notificationIds", notificationIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(types).append(controllerId).append(limit).append(timeZone).append(notificationIds).append(dateFrom).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof NotificationsFilter) == false) {
            return false;
        }
        NotificationsFilter rhs = ((NotificationsFilter) other);
        return new EqualsBuilder().append(types, rhs.types).append(controllerId, rhs.controllerId).append(limit, rhs.limit).append(timeZone, rhs.timeZone).append(notificationIds, rhs.notificationIds).append(dateFrom, rhs.dateFrom).isEquals();
    }

}
