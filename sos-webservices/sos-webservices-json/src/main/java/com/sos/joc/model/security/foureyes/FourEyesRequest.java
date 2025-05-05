
package com.sos.joc.model.security.foureyes;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    "deliveryDate",
    "accountName",
    "requestUrl",
    "requestBody",
    "title",
    "message"
})
public class FourEyesRequest {

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
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountName")
    private String accountName;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("requestUrl")
    private String requestUrl;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("requestBody")
    private RequestBody requestBody;
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
    @JsonProperty("message")
    private String message;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FourEyesRequest() {
    }

    /**
     * 
     * @param accountName
     * @param requestBody
     * @param requestUrl
     * @param deliveryDate
     * @param title
     * @param message
     */
    public FourEyesRequest(Date deliveryDate, String accountName, String requestUrl, RequestBody requestBody, String title, String message) {
        super();
        this.deliveryDate = deliveryDate;
        this.accountName = accountName;
        this.requestUrl = requestUrl;
        this.requestBody = requestBody;
        this.title = title;
        this.message = message;
    }

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
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountName")
    public String getAccountName() {
        return accountName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountName")
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("requestUrl")
    public String getRequestUrl() {
        return requestUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("requestUrl")
    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("requestBody")
    public RequestBody getRequestBody() {
        return requestBody;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("requestBody")
    public void setRequestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
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
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("accountName", accountName).append("requestUrl", requestUrl).append("requestBody", requestBody).append("title", title).append("message", message).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(accountName).append(requestBody).append(requestUrl).append(deliveryDate).append(title).append(message).toHashCode();
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
        return new EqualsBuilder().append(accountName, rhs.accountName).append(requestBody, rhs.requestBody).append(requestUrl, rhs.requestUrl).append(deliveryDate, rhs.deliveryDate).append(title, rhs.title).append(message, rhs.message).isEquals();
    }

}
