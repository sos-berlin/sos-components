
package com.sos.joc.model.inventory.search;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Inventory search response
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "token",
    "results"
})
public class ResponseQuickSearch {

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
    @JsonProperty("token")
    private String token;
    @JsonProperty("results")
    private List<ResponseBaseSearchItem> results = new ArrayList<ResponseBaseSearchItem>();

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

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("token")
    public String getToken() {
        return token;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("token")
    public void setToken(String token) {
        this.token = token;
    }

    @JsonProperty("results")
    public List<ResponseBaseSearchItem> getResults() {
        return results;
    }

    @JsonProperty("results")
    public void setResults(List<ResponseBaseSearchItem> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("token", token).append("results", results).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(results).append(token).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseQuickSearch) == false) {
            return false;
        }
        ResponseQuickSearch rhs = ((ResponseQuickSearch) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(results, rhs.results).append(token, rhs.token).isEquals();
    }

}
