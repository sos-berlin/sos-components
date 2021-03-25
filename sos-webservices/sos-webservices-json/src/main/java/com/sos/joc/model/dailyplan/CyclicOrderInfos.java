
package com.sos.joc.model.dailyplan;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Cyclic Order
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "count",
    "firstOrderId",
    "lastOrderId",
    "firstStart",
    "lastStart"
})
public class CyclicOrderInfos {

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("count")
    private Integer count;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("firstOrderId")
    private String firstOrderId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("lastOrderId")
    private String lastOrderId;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("firstStart")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date firstStart;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("lastStart")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date lastStart;

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("count")
    public Integer getCount() {
        return count;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("count")
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("firstOrderId")
    public String getFirstOrderId() {
        return firstOrderId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("firstOrderId")
    public void setFirstOrderId(String firstOrderId) {
        this.firstOrderId = firstOrderId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("lastOrderId")
    public String getLastOrderId() {
        return lastOrderId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("lastOrderId")
    public void setLastOrderId(String lastOrderId) {
        this.lastOrderId = lastOrderId;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("firstStart")
    public Date getFirstStart() {
        return firstStart;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("firstStart")
    public void setFirstStart(Date firstStart) {
        this.firstStart = firstStart;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("lastStart")
    public Date getLastStart() {
        return lastStart;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("lastStart")
    public void setLastStart(Date lastStart) {
        this.lastStart = lastStart;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("count", count).append("firstOrderId", firstOrderId).append("lastOrderId", lastOrderId).append("firstStart", firstStart).append("lastStart", lastStart).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(count).append(lastOrderId).append(firstStart).append(lastStart).append(firstOrderId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CyclicOrderInfos) == false) {
            return false;
        }
        CyclicOrderInfos rhs = ((CyclicOrderInfos) other);
        return new EqualsBuilder().append(count, rhs.count).append(lastOrderId, rhs.lastOrderId).append(firstStart, rhs.firstStart).append(lastStart, rhs.lastStart).append(firstOrderId, rhs.firstOrderId).isEquals();
    }

}
