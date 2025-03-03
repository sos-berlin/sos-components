
package com.sos.joc.model.plan;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * PlanId
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "noticeSpaceKey",
    "planSchemaId"
})
public class PlanId {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("noticeSpaceKey")
    private String noticeSpaceKey;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("planKey")
    private String planKey;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("planSchemaId")
    private String planSchemaId;

    /**
     * No args constructor for use in serialization
     * 
     */
    public PlanId() {
    }

    /**
     * 
     * @param planSchemaId
     * @param noticeSpaceKey
     */
    public PlanId(String noticeSpaceKey, String planSchemaId) {
        super();
        this.noticeSpaceKey = noticeSpaceKey;
        this.planSchemaId = planSchemaId;
    }
    
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("planKey")
    public String getPlanKey() {
        return planKey;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("noticeSpaceKey")
    public String getNoticeSpaceKey() {
        return noticeSpaceKey;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("noticeSpaceKey")
    public void setNoticeSpaceKey(String noticeSpaceKey) {
        this.noticeSpaceKey = noticeSpaceKey;
        this.planKey = noticeSpaceKey;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("planSchemaId")
    public String getPlanSchemaId() {
        return planSchemaId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("planSchemaId")
    public void setPlanSchemaId(String planSchemaId) {
        this.planSchemaId = planSchemaId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("noticeSpaceKey", noticeSpaceKey).append("planSchemaId", planSchemaId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(planSchemaId).append(noticeSpaceKey).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PlanId) == false) {
            return false;
        }
        PlanId rhs = ((PlanId) other);
        return new EqualsBuilder().append(planSchemaId, rhs.planSchemaId).append(noticeSpaceKey, rhs.noticeSpaceKey).isEquals();
    }

}
