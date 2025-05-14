
package com.sos.joc.model.security.foureyes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * FourEyesRequestEdit
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
public class FourEyesRequestEdit {

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    private Integer id;
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
    public FourEyesRequestEdit() {
    }

    /**
     * 
     * @param approver
     * @param reason
     * @param id
     * @param title
     */
    public FourEyesRequestEdit(Integer id, String title, String approver, String reason) {
        super();
        this.id = id;
        this.title = title;
        this.approver = approver;
        this.reason = reason;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(Integer id) {
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
        return new ToStringBuilder(this).append("id", id).append("title", title).append("approver", approver).append("reason", reason).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(approver).append(reason).append(id).append(title).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FourEyesRequestEdit) == false) {
            return false;
        }
        FourEyesRequestEdit rhs = ((FourEyesRequestEdit) other);
        return new EqualsBuilder().append(approver, rhs.approver).append(reason, rhs.reason).append(id, rhs.id).append(title, rhs.title).isEquals();
    }

}
