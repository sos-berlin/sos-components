
package com.sos.joc.model.security.foureyes;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.CategoryType;
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
    "id",
    "title",
    "approver",
    "reason"
})
public class FourEyesRequest
    extends FourEyesResponse
{

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    private Long id;
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
     * @param requestBody
     * @param requestUrl
     * @param approvers
     * @param id
     * @param title
     * @param deliveryDate
     * @param category
     * @param message
     * @param requestor
     */
    public FourEyesRequest(Long id, String title, String approver, String reason, Date deliveryDate, String requestor, String requestUrl, RequestBody requestBody, CategoryType category, String message, List<Approver> approvers) {
        super(deliveryDate, requestor, requestUrl, requestBody, category, message, approvers);
        this.id = id;
        this.title = title;
        this.approver = approver;
        this.reason = reason;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("id", id).append("title", title).append("approver", approver).append("reason", reason).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(approver).append(reason).append(id).append(title).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(approver, rhs.approver).append(reason, rhs.reason).append(id, rhs.id).append(title, rhs.title).isEquals();
    }

}
