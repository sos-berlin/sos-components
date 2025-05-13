
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
    "approver",
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
     * (Required)
     * 
     */
    @JsonProperty("approver")
    private String approver;
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
     * @param approver
     * @param reason
     * @param accountName
     * @param title
     * @param message
     * @param objectType
     * @param requestBody
     * @param numOfObjects
     * @param requestUrl
     * @param action
     * @param objectName
     * @param deliveryDate
     * @param category
     */
    public FourEyesRequest(String title, String approver, String reason, Date deliveryDate, String accountName, String requestUrl, RequestBody requestBody, CategoryType category, String action, TreeType objectType, String objectName, Integer numOfObjects, String message) {
        super(deliveryDate, accountName, requestUrl, requestBody, category, action, objectType, objectName, numOfObjects, message);
        this.title = title;
        this.approver = approver;
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
     * (Required)
     * 
     */
    @JsonProperty("approver")
    public String getApprover() {
        return approver;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("approver")
    public void setApprover(String approver) {
        this.approver = approver;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("title", title).append("approver", approver).append("reason", reason).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(approver).append(reason).append(title).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(approver, rhs.approver).append(reason, rhs.reason).append(title, rhs.title).isEquals();
    }

}
