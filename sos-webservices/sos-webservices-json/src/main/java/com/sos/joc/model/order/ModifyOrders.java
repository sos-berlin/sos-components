
package com.sos.joc.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.order.OrderModeType;
import com.sos.inventory.model.common.Variables;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * modify order commands
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "dateFrom",
    "dateTo",
    "timeZone",
    "orderType",
    "kill",
    "deep",
    "reset",
    "force",
    "fromCurrentBlock",
    "position",
    "priority",
    "variables",
    "cycleEndTime"
})
public class ModifyOrders
    extends ModifyOrdersBase
{

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
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateTo")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String dateTo;
    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timeZone;
    /**
     * orderModeType
     * <p>
     * relevant for cancel or suspend order
     * 
     */
    @JsonProperty("orderType")
    @JsonPropertyDescription("relevant for cancel or suspend order")
    private OrderModeType orderType = OrderModeType.fromValue("FreshOrStarted");
    /**
     * only relevant for 'suspend' and 'cancel'
     * 
     */
    @JsonProperty("kill")
    @JsonPropertyDescription("only relevant for 'suspend' and 'cancel'")
    private Boolean kill = false;
    /**
     * only relevant for 'suspend' and 'cancel'
     * 
     */
    @JsonProperty("deep")
    @JsonPropertyDescription("only relevant for 'suspend' and 'cancel'")
    private Boolean deep = false;
    /**
     * only relevant for 'suspend'
     * 
     */
    @JsonProperty("reset")
    @JsonPropertyDescription("only relevant for 'suspend'")
    private Boolean reset = false;
    /**
     * only relevant for 'resume'; force execution of non-startable jobs after kill
     * 
     */
    @JsonProperty("force")
    @JsonPropertyDescription("only relevant for 'resume'; force execution of non-startable jobs after kill")
    private Boolean force = false;
    /**
     * only relevant for 'resume'
     * 
     */
    @JsonProperty("fromCurrentBlock")
    @JsonPropertyDescription("only relevant for 'resume'")
    private Boolean fromCurrentBlock = false;
    @JsonProperty("position")
    private Object position;
    @JsonProperty("priority")
    private Integer priority;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("variables")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables variables;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("cycleEndTime")
    private Long cycleEndTime;

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
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateTo")
    public String getDateTo() {
        return dateTo;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateTo")
    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * orderModeType
     * <p>
     * relevant for cancel or suspend order
     * 
     */
    @JsonProperty("orderType")
    public OrderModeType getOrderType() {
        return orderType;
    }

    /**
     * orderModeType
     * <p>
     * relevant for cancel or suspend order
     * 
     */
    @JsonProperty("orderType")
    public void setOrderType(OrderModeType orderType) {
        this.orderType = orderType;
    }

    /**
     * only relevant for 'suspend' and 'cancel'
     * 
     */
    @JsonProperty("kill")
    public Boolean getKill() {
        return kill;
    }

    /**
     * only relevant for 'suspend' and 'cancel'
     * 
     */
    @JsonProperty("kill")
    public void setKill(Boolean kill) {
        this.kill = kill;
    }

    /**
     * only relevant for 'suspend' and 'cancel'
     * 
     */
    @JsonProperty("deep")
    public Boolean getDeep() {
        return deep;
    }

    /**
     * only relevant for 'suspend' and 'cancel'
     * 
     */
    @JsonProperty("deep")
    public void setDeep(Boolean deep) {
        this.deep = deep;
    }

    /**
     * only relevant for 'suspend'
     * 
     */
    @JsonProperty("reset")
    public Boolean getReset() {
        return reset;
    }

    /**
     * only relevant for 'suspend'
     * 
     */
    @JsonProperty("reset")
    public void setReset(Boolean reset) {
        this.reset = reset;
    }

    /**
     * only relevant for 'resume'; force execution of non-startable jobs after kill
     * 
     */
    @JsonProperty("force")
    public Boolean getForce() {
        return force;
    }

    /**
     * only relevant for 'resume'; force execution of non-startable jobs after kill
     * 
     */
    @JsonProperty("force")
    public void setForce(Boolean force) {
        this.force = force;
    }

    /**
     * only relevant for 'resume'
     * 
     */
    @JsonProperty("fromCurrentBlock")
    public Boolean getFromCurrentBlock() {
        return fromCurrentBlock;
    }

    /**
     * only relevant for 'resume'
     * 
     */
    @JsonProperty("fromCurrentBlock")
    public void setFromCurrentBlock(Boolean fromCurrentBlock) {
        this.fromCurrentBlock = fromCurrentBlock;
    }

    @JsonProperty("position")
    public Object getPosition() {
        if (position != null) {
            if (position instanceof String && ((String) position).isEmpty()) {
                return null;
            }
        }
        return position;
    }

    @JsonProperty("position")
    public void setPosition(Object position) {
        this.position = position;
    }

    @JsonProperty("priority")
    public Integer getPriority() {
        return priority;
    }

    @JsonProperty("priority")
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("variables")
    public Variables getVariables() {
        return variables;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("variables")
    public void setVariables(Variables variables) {
        this.variables = variables;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("cycleEndTime")
    public Long getCycleEndTime() {
        return cycleEndTime;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("cycleEndTime")
    public void setCycleEndTime(Long cycleEndTime) {
        this.cycleEndTime = cycleEndTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("dateFrom", dateFrom).append("dateTo", dateTo).append("timeZone", timeZone).append("orderType", orderType).append("kill", kill).append("deep", deep).append("reset", reset).append("force", force).append("fromCurrentBlock", fromCurrentBlock).append("position", position).append("priority", priority).append("variables", variables).append("cycleEndTime", cycleEndTime).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(orderType).append(deep).append(variables).append(timeZone).append(dateFrom).append(kill).append(priority).append(cycleEndTime).append(dateTo).append(reset).append(force).append(fromCurrentBlock).append(position).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ModifyOrders) == false) {
            return false;
        }
        ModifyOrders rhs = ((ModifyOrders) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(orderType, rhs.orderType).append(deep, rhs.deep).append(variables, rhs.variables).append(timeZone, rhs.timeZone).append(dateFrom, rhs.dateFrom).append(kill, rhs.kill).append(priority, rhs.priority).append(cycleEndTime, rhs.cycleEndTime).append(dateTo, rhs.dateTo).append(reset, rhs.reset).append(force, rhs.force).append(fromCurrentBlock, rhs.fromCurrentBlock).append(position, rhs.position).isEquals();
    }

}
