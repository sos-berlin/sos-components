
package com.sos.joc.model.monitoring.notification.system;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.monitoring.notification.NotificationType;
import com.sos.monitoring.notification.SystemNotificationCategory;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * notifications filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "dateFrom",
    "timeZone",
    "limit",
    "types",
    "categories",
    "notificationIds"
})
public class SystemNotificationsFilter {

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
    private List<NotificationType> types = new ArrayList<NotificationType>();
    @JsonProperty("categories")
    private List<SystemNotificationCategory> categories = new ArrayList<SystemNotificationCategory>();
    @JsonProperty("notificationIds")
    private List<Long> notificationIds = new ArrayList<Long>();

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
    public List<NotificationType> getTypes() {
        return types;
    }

    @JsonProperty("types")
    public void setTypes(List<NotificationType> types) {
        this.types = types;
    }

    @JsonProperty("categories")
    public List<SystemNotificationCategory> getCategories() {
        return categories;
    }

    @JsonProperty("categories")
    public void setCategories(List<SystemNotificationCategory> categories) {
        this.categories = categories;
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
        return new ToStringBuilder(this).append("dateFrom", dateFrom).append("timeZone", timeZone).append("limit", limit).append("types", types).append("categories", categories).append("notificationIds", notificationIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(types).append(limit).append(timeZone).append(notificationIds).append(categories).append(dateFrom).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SystemNotificationsFilter) == false) {
            return false;
        }
        SystemNotificationsFilter rhs = ((SystemNotificationsFilter) other);
        return new EqualsBuilder().append(types, rhs.types).append(limit, rhs.limit).append(timeZone, rhs.timeZone).append(notificationIds, rhs.notificationIds).append(categories, rhs.categories).append(dateFrom, rhs.dateFrom).isEquals();
    }

}
