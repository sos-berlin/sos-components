
package com.sos.joc.model.security.foureyes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.CategoryType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * FourEyesResponse
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "requestor",
    "requestUrl",
    "requestBody",
    "category",
    "message",
    "approvers"
})
public class FourEyesResponse {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("requestor")
    private String requestor;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("requestUrl")
    private String requestUrl;
    @JsonProperty("requestBody")
    private RequestBody requestBody;
    /**
     * Tree object type
     * <p>
     * 
     * 
     */
    @JsonProperty("category")
    private CategoryType category;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("message")
    private String message;
    @JsonProperty("approvers")
    private List<Approver> approvers = new ArrayList<Approver>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public FourEyesResponse() {
    }

    /**
     * 
     * @param requestBody
     * @param requestUrl
     * @param approvers
     * @param deliveryDate
     * @param category
     * @param message
     * @param requestor
     */
    public FourEyesResponse(Date deliveryDate, String requestor, String requestUrl, RequestBody requestBody, CategoryType category, String message, List<Approver> approvers) {
        super();
        this.deliveryDate = deliveryDate;
        this.requestor = requestor;
        this.requestUrl = requestUrl;
        this.requestBody = requestBody;
        this.category = category;
        this.message = message;
        this.approvers = approvers;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
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
     * 
     */
    @JsonProperty("requestor")
    public String getRequestor() {
        return requestor;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("requestor")
    public void setRequestor(String requestor) {
        this.requestor = requestor;
    }

    /**
     * string without < and >
     * <p>
     * 
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
     * 
     */
    @JsonProperty("requestUrl")
    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    @JsonProperty("requestBody")
    public RequestBody getRequestBody() {
        return requestBody;
    }

    @JsonProperty("requestBody")
    public void setRequestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
    }

    /**
     * Tree object type
     * <p>
     * 
     * 
     */
    @JsonProperty("category")
    public CategoryType getCategory() {
        return category;
    }

    /**
     * Tree object type
     * <p>
     * 
     * 
     */
    @JsonProperty("category")
    public void setCategory(CategoryType category) {
        this.category = category;
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

    @JsonProperty("approvers")
    public List<Approver> getApprovers() {
        return approvers;
    }

    @JsonProperty("approvers")
    public void setApprovers(List<Approver> approvers) {
        this.approvers = approvers;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("requestor", requestor).append("requestUrl", requestUrl).append("requestBody", requestBody).append("category", category).append("message", message).append("approvers", approvers).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(requestBody).append(requestUrl).append(approvers).append(deliveryDate).append(category).append(message).append(requestor).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FourEyesResponse) == false) {
            return false;
        }
        FourEyesResponse rhs = ((FourEyesResponse) other);
        return new EqualsBuilder().append(requestBody, rhs.requestBody).append(requestUrl, rhs.requestUrl).append(approvers, rhs.approvers).append(deliveryDate, rhs.deliveryDate).append(category, rhs.category).append(message, rhs.message).append(requestor, rhs.requestor).isEquals();
    }

}
