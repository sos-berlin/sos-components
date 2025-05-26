
package com.sos.joc.model.security.foureyes;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.CategoryType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * ApprovalRequest
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "title",
    "approver",
    "unknownApprover",
    "reason",
    "approverState",
    "approverStateDate",
    "requestorState",
    "requestorStateDate"
})
public class ApprovalRequest
    extends ApprovalBase
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
    @JsonProperty("approver")
    private String approver;
    /**
     * An approver was selected for the approval request but is not longer configured as approver
     * 
     */
    @JsonProperty("unknownApprover")
    @JsonPropertyDescription("An approver was selected for the approval request but is not longer configured as approver")
    private Boolean unknownApprover = false;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("reason")
    private String reason;
    /**
     * approver state
     * <p>
     * 
     * 
     */
    @JsonProperty("approverState")
    private ApproverState approverState;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("approverStateDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date approverStateDate;
    /**
     * requestor state
     * <p>
     * 
     * 
     */
    @JsonProperty("requestorState")
    private RequestorState requestorState;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("requestorStateDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date requestorStateDate;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ApprovalRequest() {
    }

    /**
     * 
     * @param approver
     * @param reason
     * @param title
     * @param unknownApprover
     * @param requestor
     * @param approverState
     * @param requestBody
     * @param requestUrl
     * @param requestorState
     * @param requestorStateDate
     * @param id
     * @param category
     * @param approverStateDate
     */
    public ApprovalRequest(Long id, String title, String approver, Boolean unknownApprover, String reason, ApproverState approverState, Date approverStateDate, RequestorState requestorState, Date requestorStateDate, String requestor, String requestUrl, RequestBody requestBody, CategoryType category) {
        super(requestor, requestUrl, requestBody, category);
        this.id = id;
        this.title = title;
        this.approver = approver;
        this.unknownApprover = unknownApprover;
        this.reason = reason;
        this.approverState = approverState;
        this.approverStateDate = approverStateDate;
        this.requestorState = requestorState;
        this.requestorStateDate = requestorStateDate;
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
    @JsonProperty("approver")
    public String getApprover() {
        return approver;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("approver")
    public void setApprover(String approver) {
        this.approver = approver;
    }

    /**
     * An approver was selected for the approval request but is not longer configured as approver
     * 
     */
    @JsonProperty("unknownApprover")
    public Boolean getUnknownApprover() {
        return unknownApprover;
    }

    /**
     * An approver was selected for the approval request but is not longer configured as approver
     * 
     */
    @JsonProperty("unknownApprover")
    public void setUnknownApprover(Boolean unknownApprover) {
        this.unknownApprover = unknownApprover;
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

    /**
     * approver state
     * <p>
     * 
     * 
     */
    @JsonProperty("approverState")
    public ApproverState getApproverState() {
        return approverState;
    }

    /**
     * approver state
     * <p>
     * 
     * 
     */
    @JsonProperty("approverState")
    public void setApproverState(ApproverState approverState) {
        this.approverState = approverState;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("approverStateDate")
    public Date getApproverStateDate() {
        return approverStateDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("approverStateDate")
    public void setApproverStateDate(Date approverStateDate) {
        this.approverStateDate = approverStateDate;
    }

    /**
     * requestor state
     * <p>
     * 
     * 
     */
    @JsonProperty("requestorState")
    public RequestorState getRequestorState() {
        return requestorState;
    }

    /**
     * requestor state
     * <p>
     * 
     * 
     */
    @JsonProperty("requestorState")
    public void setRequestorState(RequestorState requestorState) {
        this.requestorState = requestorState;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("requestorStateDate")
    public Date getRequestorStateDate() {
        return requestorStateDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("requestorStateDate")
    public void setRequestorStateDate(Date requestorStateDate) {
        this.requestorStateDate = requestorStateDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("id", id).append("title", title).append("approver", approver).append("unknownApprover", unknownApprover).append("reason", reason).append("approverState", approverState).append("approverStateDate", approverStateDate).append("requestorState", requestorState).append("requestorStateDate", requestorStateDate).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(approver).append(approverState).append(reason).append(requestorState).append(requestorStateDate).append(id).append(title).append(approverStateDate).append(unknownApprover).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ApprovalRequest) == false) {
            return false;
        }
        ApprovalRequest rhs = ((ApprovalRequest) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(approver, rhs.approver).append(approverState, rhs.approverState).append(reason, rhs.reason).append(requestorState, rhs.requestorState).append(requestorStateDate, rhs.requestorStateDate).append(id, rhs.id).append(title, rhs.title).append(approverStateDate, rhs.approverStateDate).append(unknownApprover, rhs.unknownApprover).isEquals();
    }

}
