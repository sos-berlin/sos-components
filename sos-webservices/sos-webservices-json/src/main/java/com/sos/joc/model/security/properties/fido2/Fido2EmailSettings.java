
package com.sos.joc.model.security.properties.fido2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Fido2 Email Settings
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "bodyRegistration",
    "subjectRegistration",
    "ccRegistration",
    "bccRegistration",
    "bodyAccess",
    "subjectAccess",
    "ccAccess",
    "bccAccess",
    "bodyConfirmed",
    "subjectConfirmed",
    "receiptConfirmed",
    "sendMailToConfirm",
    "sendMailToNotifySuccessfulRegistration",
    "sendMailToNotifyConfirmationReceived",
    "contentType",
    "charset",
    "encoding",
    "priority",
    "nameOfJobResource"
})
public class Fido2EmailSettings {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bodyRegistration")
    private String bodyRegistration;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subjectRegistration")
    private String subjectRegistration;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("ccRegistration")
    private String ccRegistration;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bccRegistration")
    private String bccRegistration;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bodyAccess")
    private String bodyAccess;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subjectAccess")
    private String subjectAccess;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("ccAccess")
    private String ccAccess;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bccAccess")
    private String bccAccess;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bodyConfirmed")
    private String bodyConfirmed;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subjectConfirmed")
    private String subjectConfirmed;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("receiptConfirmed")
    private String receiptConfirmed;
    /**
     * sendMailTo Confirm parameter
     * <p>
     * true if the confirmation email should be sent
     * 
     */
    @JsonProperty("sendMailToConfirm")
    @JsonPropertyDescription("true if the confirmation email should be sent")
    private Boolean sendMailToConfirm;
    /**
     * Successful Registration parameter
     * <p>
     * true if the email for successful registration should be sent
     * 
     */
    @JsonProperty("sendMailToNotifySuccessfulRegistration")
    @JsonPropertyDescription("true if the email for successful registration should be sent")
    private Boolean sendMailToNotifySuccessfulRegistration;
    /**
     * Successful Registration parameter
     * <p>
     * true if the email for successful registration should be sent
     * 
     */
    @JsonProperty("sendMailToNotifyConfirmationReceived")
    @JsonPropertyDescription("true if the email for successful registration should be sent")
    private Boolean sendMailToNotifyConfirmationReceived;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("contentType")
    private String contentType;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("charset")
    private String charset;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("encoding")
    private String encoding;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("priority")
    private String priority;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("nameOfJobResource")
    private String nameOfJobResource;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Fido2EmailSettings() {
    }

    /**
     * 
     * @param bodyRegistration
     * @param sendMailToConfirm
     * @param charset
     * @param subjectRegistration
     * @param bodyConfirmed
     * @param bccRegistration
     * @param subjectAccess
     * @param ccRegistration
     * @param encoding
     * @param priority
     * @param ccAccess
     * @param receiptConfirmed
     * @param sendMailToNotifySuccessfulRegistration
     * @param bccAccess
     * @param subjectConfirmed
     * @param bodyAccess
     * @param sendMailToNotifyConfirmationReceived
     * @param nameOfJobResource
     * @param contentType
     */
    public Fido2EmailSettings(String bodyRegistration, String subjectRegistration, String ccRegistration, String bccRegistration, String bodyAccess, String subjectAccess, String ccAccess, String bccAccess, String bodyConfirmed, String subjectConfirmed, String receiptConfirmed, Boolean sendMailToConfirm, Boolean sendMailToNotifySuccessfulRegistration, Boolean sendMailToNotifyConfirmationReceived, String contentType, String charset, String encoding, String priority, String nameOfJobResource) {
        super();
        this.bodyRegistration = bodyRegistration;
        this.subjectRegistration = subjectRegistration;
        this.ccRegistration = ccRegistration;
        this.bccRegistration = bccRegistration;
        this.bodyAccess = bodyAccess;
        this.subjectAccess = subjectAccess;
        this.ccAccess = ccAccess;
        this.bccAccess = bccAccess;
        this.bodyConfirmed = bodyConfirmed;
        this.subjectConfirmed = subjectConfirmed;
        this.receiptConfirmed = receiptConfirmed;
        this.sendMailToConfirm = sendMailToConfirm;
        this.sendMailToNotifySuccessfulRegistration = sendMailToNotifySuccessfulRegistration;
        this.sendMailToNotifyConfirmationReceived = sendMailToNotifyConfirmationReceived;
        this.contentType = contentType;
        this.charset = charset;
        this.encoding = encoding;
        this.priority = priority;
        this.nameOfJobResource = nameOfJobResource;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bodyRegistration")
    public String getBodyRegistration() {
        return bodyRegistration;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bodyRegistration")
    public void setBodyRegistration(String bodyRegistration) {
        this.bodyRegistration = bodyRegistration;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subjectRegistration")
    public String getSubjectRegistration() {
        return subjectRegistration;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subjectRegistration")
    public void setSubjectRegistration(String subjectRegistration) {
        this.subjectRegistration = subjectRegistration;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("ccRegistration")
    public String getCcRegistration() {
        return ccRegistration;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("ccRegistration")
    public void setCcRegistration(String ccRegistration) {
        this.ccRegistration = ccRegistration;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bccRegistration")
    public String getBccRegistration() {
        return bccRegistration;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bccRegistration")
    public void setBccRegistration(String bccRegistration) {
        this.bccRegistration = bccRegistration;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bodyAccess")
    public String getBodyAccess() {
        return bodyAccess;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bodyAccess")
    public void setBodyAccess(String bodyAccess) {
        this.bodyAccess = bodyAccess;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subjectAccess")
    public String getSubjectAccess() {
        return subjectAccess;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subjectAccess")
    public void setSubjectAccess(String subjectAccess) {
        this.subjectAccess = subjectAccess;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("ccAccess")
    public String getCcAccess() {
        return ccAccess;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("ccAccess")
    public void setCcAccess(String ccAccess) {
        this.ccAccess = ccAccess;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bccAccess")
    public String getBccAccess() {
        return bccAccess;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bccAccess")
    public void setBccAccess(String bccAccess) {
        this.bccAccess = bccAccess;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bodyConfirmed")
    public String getBodyConfirmed() {
        return bodyConfirmed;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("bodyConfirmed")
    public void setBodyConfirmed(String bodyConfirmed) {
        this.bodyConfirmed = bodyConfirmed;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subjectConfirmed")
    public String getSubjectConfirmed() {
        return subjectConfirmed;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subjectConfirmed")
    public void setSubjectConfirmed(String subjectConfirmed) {
        this.subjectConfirmed = subjectConfirmed;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("receiptConfirmed")
    public String getReceiptConfirmed() {
        return receiptConfirmed;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("receiptConfirmed")
    public void setReceiptConfirmed(String receiptConfirmed) {
        this.receiptConfirmed = receiptConfirmed;
    }

    /**
     * sendMailTo Confirm parameter
     * <p>
     * true if the confirmation email should be sent
     * 
     */
    @JsonProperty("sendMailToConfirm")
    public Boolean getSendMailToConfirm() {
        return sendMailToConfirm;
    }

    /**
     * sendMailTo Confirm parameter
     * <p>
     * true if the confirmation email should be sent
     * 
     */
    @JsonProperty("sendMailToConfirm")
    public void setSendMailToConfirm(Boolean sendMailToConfirm) {
        this.sendMailToConfirm = sendMailToConfirm;
    }

    /**
     * Successful Registration parameter
     * <p>
     * true if the email for successful registration should be sent
     * 
     */
    @JsonProperty("sendMailToNotifySuccessfulRegistration")
    public Boolean getSendMailToNotifySuccessfulRegistration() {
        return sendMailToNotifySuccessfulRegistration;
    }

    /**
     * Successful Registration parameter
     * <p>
     * true if the email for successful registration should be sent
     * 
     */
    @JsonProperty("sendMailToNotifySuccessfulRegistration")
    public void setSendMailToNotifySuccessfulRegistration(Boolean sendMailToNotifySuccessfulRegistration) {
        this.sendMailToNotifySuccessfulRegistration = sendMailToNotifySuccessfulRegistration;
    }

    /**
     * Successful Registration parameter
     * <p>
     * true if the email for successful registration should be sent
     * 
     */
    @JsonProperty("sendMailToNotifyConfirmationReceived")
    public Boolean getSendMailToNotifyConfirmationReceived() {
        return sendMailToNotifyConfirmationReceived;
    }

    /**
     * Successful Registration parameter
     * <p>
     * true if the email for successful registration should be sent
     * 
     */
    @JsonProperty("sendMailToNotifyConfirmationReceived")
    public void setSendMailToNotifyConfirmationReceived(Boolean sendMailToNotifyConfirmationReceived) {
        this.sendMailToNotifyConfirmationReceived = sendMailToNotifyConfirmationReceived;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("contentType")
    public String getContentType() {
        return contentType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("contentType")
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("charset")
    public String getCharset() {
        return charset;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("charset")
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("encoding")
    public String getEncoding() {
        return encoding;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("encoding")
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("priority")
    public String getPriority() {
        return priority;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("priority")
    public void setPriority(String priority) {
        this.priority = priority;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("nameOfJobResource")
    public String getNameOfJobResource() {
        return nameOfJobResource;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("nameOfJobResource")
    public void setNameOfJobResource(String nameOfJobResource) {
        this.nameOfJobResource = nameOfJobResource;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("bodyRegistration", bodyRegistration).append("subjectRegistration", subjectRegistration).append("ccRegistration", ccRegistration).append("bccRegistration", bccRegistration).append("bodyAccess", bodyAccess).append("subjectAccess", subjectAccess).append("ccAccess", ccAccess).append("bccAccess", bccAccess).append("bodyConfirmed", bodyConfirmed).append("subjectConfirmed", subjectConfirmed).append("receiptConfirmed", receiptConfirmed).append("sendMailToConfirm", sendMailToConfirm).append("sendMailToNotifySuccessfulRegistration", sendMailToNotifySuccessfulRegistration).append("sendMailToNotifyConfirmationReceived", sendMailToNotifyConfirmationReceived).append("contentType", contentType).append("charset", charset).append("encoding", encoding).append("priority", priority).append("nameOfJobResource", nameOfJobResource).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(bodyRegistration).append(sendMailToConfirm).append(charset).append(subjectRegistration).append(bodyConfirmed).append(bccRegistration).append(subjectAccess).append(ccRegistration).append(encoding).append(priority).append(ccAccess).append(receiptConfirmed).append(sendMailToNotifySuccessfulRegistration).append(bccAccess).append(subjectConfirmed).append(bodyAccess).append(sendMailToNotifyConfirmationReceived).append(nameOfJobResource).append(contentType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Fido2EmailSettings) == false) {
            return false;
        }
        Fido2EmailSettings rhs = ((Fido2EmailSettings) other);
        return new EqualsBuilder().append(bodyRegistration, rhs.bodyRegistration).append(sendMailToConfirm, rhs.sendMailToConfirm).append(charset, rhs.charset).append(subjectRegistration, rhs.subjectRegistration).append(bodyConfirmed, rhs.bodyConfirmed).append(bccRegistration, rhs.bccRegistration).append(subjectAccess, rhs.subjectAccess).append(ccRegistration, rhs.ccRegistration).append(encoding, rhs.encoding).append(priority, rhs.priority).append(ccAccess, rhs.ccAccess).append(receiptConfirmed, rhs.receiptConfirmed).append(sendMailToNotifySuccessfulRegistration, rhs.sendMailToNotifySuccessfulRegistration).append(bccAccess, rhs.bccAccess).append(subjectConfirmed, rhs.subjectConfirmed).append(bodyAccess, rhs.bodyAccess).append(sendMailToNotifyConfirmationReceived, rhs.sendMailToNotifyConfirmationReceived).append(nameOfJobResource, rhs.nameOfJobResource).append(contentType, rhs.contentType).isEquals();
    }

}
