
package com.sos.joc.model.order;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.common.HistoryStateText;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * orders filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "orders",
    "excludeOrders",
    "compact",
    "regex",
    "processingStates",
    "types",
    "dateFrom",
    "dateTo",
    "timeZone",
    "folders",
    "limit",
    "historyStates",
    "historyIds",
    "runTimeIsTemporary"
})
public class OrdersFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("orders")
    private List<OrderPath> orders = new ArrayList<OrderPath>();
    @JsonProperty("excludeOrders")
    private List<OrderPath> excludeOrders = new ArrayList<OrderPath>();
    /**
     * compact parameter
     * <p>
     * controls if the object view is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JsonPropertyDescription("controls if the object view is compact or detailed")
    private Boolean compact = false;
    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    @JsonPropertyDescription("regular expression to filter JobScheduler objects by matching the path")
    private String regex;
    @JsonProperty("processingStates")
    private List<OrderStateFilter> processingStates = new ArrayList<OrderStateFilter>();
    @JsonProperty("types")
    private List<OrderType> types = new ArrayList<OrderType>();
    @JsonProperty("dateFrom")
    private String dateFrom;
    @JsonProperty("dateTo")
    private String dateTo;
    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timeZone;
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    private List<Folder> folders = new ArrayList<Folder>();
    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JsonPropertyDescription("only for db history urls to restrict the number of responsed records; -1=unlimited")
    private Integer limit = 10000;
    @JsonProperty("historyStates")
    private List<HistoryStateText> historyStates = new ArrayList<HistoryStateText>();
    @JsonProperty("historyIds")
    private List<Long> historyIds = new ArrayList<Long>();
    @JsonProperty("runTimeIsTemporary")
    private Boolean runTimeIsTemporary;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    @JsonProperty("orders")
    public List<OrderPath> getOrders() {
        return orders;
    }

    @JsonProperty("orders")
    public void setOrders(List<OrderPath> orders) {
        this.orders = orders;
    }

    @JsonProperty("excludeOrders")
    public List<OrderPath> getExcludeOrders() {
        return excludeOrders;
    }

    @JsonProperty("excludeOrders")
    public void setExcludeOrders(List<OrderPath> excludeOrders) {
        this.excludeOrders = excludeOrders;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object view is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public Boolean getCompact() {
        return compact;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object view is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public void setCompact(Boolean compact) {
        this.compact = compact;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    public String getRegex() {
        return regex;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    public void setRegex(String regex) {
        this.regex = regex;
    }

    @JsonProperty("processingStates")
    public List<OrderStateFilter> getProcessingStates() {
        return processingStates;
    }

    @JsonProperty("processingStates")
    public void setProcessingStates(List<OrderStateFilter> processingStates) {
        this.processingStates = processingStates;
    }

    @JsonProperty("types")
    public List<OrderType> getTypes() {
        return types;
    }

    @JsonProperty("types")
    public void setTypes(List<OrderType> types) {
        this.types = types;
    }

    @JsonProperty("dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    @JsonProperty("dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    @JsonProperty("dateTo")
    public String getDateTo() {
        return dateTo;
    }

    @JsonProperty("dateTo")
    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
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
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    public List<Folder> getFolders() {
        return folders;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @JsonProperty("historyStates")
    public List<HistoryStateText> getHistoryStates() {
        return historyStates;
    }

    @JsonProperty("historyStates")
    public void setHistoryStates(List<HistoryStateText> historyStates) {
        this.historyStates = historyStates;
    }

    @JsonProperty("historyIds")
    public List<Long> getHistoryIds() {
        return historyIds;
    }

    @JsonProperty("historyIds")
    public void setHistoryIds(List<Long> historyIds) {
        this.historyIds = historyIds;
    }

    @JsonProperty("runTimeIsTemporary")
    public Boolean getRunTimeIsTemporary() {
        return runTimeIsTemporary;
    }

    @JsonProperty("runTimeIsTemporary")
    public void setRunTimeIsTemporary(Boolean runTimeIsTemporary) {
        this.runTimeIsTemporary = runTimeIsTemporary;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("orders", orders).append("excludeOrders", excludeOrders).append("compact", compact).append("regex", regex).append("processingStates", processingStates).append("types", types).append("dateFrom", dateFrom).append("dateTo", dateTo).append("timeZone", timeZone).append("folders", folders).append("limit", limit).append("historyStates", historyStates).append("historyIds", historyIds).append("runTimeIsTemporary", runTimeIsTemporary).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(processingStates).append(types).append(folders).append(compact).append(excludeOrders).append(historyStates).append(timeZone).append(dateFrom).append(historyIds).append(regex).append(dateTo).append(limit).append(orders).append(runTimeIsTemporary).append(jobschedulerId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrdersFilter) == false) {
            return false;
        }
        OrdersFilter rhs = ((OrdersFilter) other);
        return new EqualsBuilder().append(processingStates, rhs.processingStates).append(types, rhs.types).append(folders, rhs.folders).append(compact, rhs.compact).append(excludeOrders, rhs.excludeOrders).append(historyStates, rhs.historyStates).append(timeZone, rhs.timeZone).append(dateFrom, rhs.dateFrom).append(historyIds, rhs.historyIds).append(regex, rhs.regex).append(dateTo, rhs.dateTo).append(limit, rhs.limit).append(orders, rhs.orders).append(runTimeIsTemporary, rhs.runTimeIsTemporary).append(jobschedulerId, rhs.jobschedulerId).isEquals();
    }

}
