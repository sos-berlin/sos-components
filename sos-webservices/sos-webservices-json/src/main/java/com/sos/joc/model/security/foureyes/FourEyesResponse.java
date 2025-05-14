
package com.sos.joc.model.security.foureyes;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.tree.TreeType;
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
    "action",
    "objectType",
    "objectName",
    "numOfObjects",
    "message"
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
    @JsonProperty("action")
    private String action;
    /**
     * Tree object type
     * <p>
     * 
     * 
     */
    @JsonProperty("objectType")
    private TreeType objectType;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("objectName")
    private String objectName;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfObjects")
    private Integer numOfObjects;
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
    public FourEyesResponse() {
    }

    /**
     * 
     * @param requestBody
     * @param numOfObjects
     * @param requestUrl
     * @param action
     * @param objectName
     * @param deliveryDate
     * @param category
     * @param message
     * @param requestor
     * @param objectType
     */
    public FourEyesResponse(Date deliveryDate, String requestor, String requestUrl, RequestBody requestBody, CategoryType category, String action, TreeType objectType, String objectName, Integer numOfObjects, String message) {
        super();
        this.deliveryDate = deliveryDate;
        this.requestor = requestor;
        this.requestUrl = requestUrl;
        this.requestBody = requestBody;
        this.category = category;
        this.action = action;
        this.objectType = objectType;
        this.objectName = objectName;
        this.numOfObjects = numOfObjects;
        this.message = message;
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
    @JsonProperty("action")
    public String getAction() {
        return action;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("action")
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Tree object type
     * <p>
     * 
     * 
     */
    @JsonProperty("objectType")
    public TreeType getObjectType() {
        return objectType;
    }

    /**
     * Tree object type
     * <p>
     * 
     * 
     */
    @JsonProperty("objectType")
    public void setObjectType(TreeType objectType) {
        this.objectType = objectType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("objectName")
    public String getObjectName() {
        return objectName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("objectName")
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfObjects")
    public Integer getNumOfObjects() {
        return numOfObjects;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfObjects")
    public void setNumOfObjects(Integer numOfObjects) {
        this.numOfObjects = numOfObjects;
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
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("requestor", requestor).append("requestUrl", requestUrl).append("requestBody", requestBody).append("category", category).append("action", action).append("objectType", objectType).append("objectName", objectName).append("numOfObjects", numOfObjects).append("message", message).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(requestBody).append(numOfObjects).append(requestUrl).append(action).append(objectName).append(deliveryDate).append(category).append(message).append(requestor).append(objectType).toHashCode();
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
        return new EqualsBuilder().append(requestBody, rhs.requestBody).append(numOfObjects, rhs.numOfObjects).append(requestUrl, rhs.requestUrl).append(action, rhs.action).append(objectName, rhs.objectName).append(deliveryDate, rhs.deliveryDate).append(category, rhs.category).append(message, rhs.message).append(requestor, rhs.requestor).append(objectType, rhs.objectType).isEquals();
    }

}
