
package com.sos.joc.model.security.foureyes;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.tree.TreeType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * FourEyesRequest
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "title",
    "reason"
})
public class FourEyesRequest
    extends FourEyesResponse
{

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("title")
    private String title;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("reason")
    private String reason;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FourEyesRequest() {
    }

    /**
     * 
     * @param reason
     * @param accountName
     * @param requestBody
     * @param numOfObjects
     * @param requestUrl
     * @param action
     * @param objectName
     * @param title
     * @param deliveryDate
     * @param category
     * @param message
     * @param objectType
     */
    public FourEyesRequest(String title, String reason, Date deliveryDate, String accountName, String requestUrl, RequestBody requestBody, CategoryType category, String action, TreeType objectType, String objectName, Integer numOfObjects, String message) {
        super(deliveryDate, accountName, requestUrl, requestBody, category, action, objectType, objectName, numOfObjects, message);
        this.title = title;
        this.reason = reason;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("reason")
    public String getReason() {
        return reason;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("reason")
    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("title", title).append("reason", reason).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(title).append(reason).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FourEyesRequest) == false) {
            return false;
        }
        FourEyesRequest rhs = ((FourEyesRequest) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(title, rhs.title).append(reason, rhs.reason).isEquals();
    }

}
