
package com.sos.joc.model.inventory.validate;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * revalidate report
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "validObjs",
    "invalidObjs",
    "erroneousObjs"
})
public class Report {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("validObjs")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ReportItem> validObjs = new LinkedHashSet<ReportItem>();
    @JsonProperty("invalidObjs")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ReportItem> invalidObjs = new LinkedHashSet<ReportItem>();
    @JsonProperty("erroneousObjs")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ReportItem> erroneousObjs = new LinkedHashSet<ReportItem>();

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    @JsonProperty("validObjs")
    public Set<ReportItem> getValidObjs() {
        return validObjs;
    }

    @JsonProperty("validObjs")
    public void setValidObjs(Set<ReportItem> validObjs) {
        this.validObjs = validObjs;
    }

    @JsonProperty("invalidObjs")
    public Set<ReportItem> getInvalidObjs() {
        return invalidObjs;
    }

    @JsonProperty("invalidObjs")
    public void setInvalidObjs(Set<ReportItem> invalidObjs) {
        this.invalidObjs = invalidObjs;
    }

    @JsonProperty("erroneousObjs")
    public Set<ReportItem> getErroneousObjs() {
        return erroneousObjs;
    }

    @JsonProperty("erroneousObjs")
    public void setErroneousObjs(Set<ReportItem> erroneousObjs) {
        this.erroneousObjs = erroneousObjs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("validObjs", validObjs).append("invalidObjs", invalidObjs).append("erroneousObjs", erroneousObjs).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(validObjs).append(deliveryDate).append(invalidObjs).append(erroneousObjs).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Report) == false) {
            return false;
        }
        Report rhs = ((Report) other);
        return new EqualsBuilder().append(validObjs, rhs.validObjs).append(deliveryDate, rhs.deliveryDate).append(invalidObjs, rhs.invalidObjs).append(erroneousObjs, rhs.erroneousObjs).isEquals();
    }

}
