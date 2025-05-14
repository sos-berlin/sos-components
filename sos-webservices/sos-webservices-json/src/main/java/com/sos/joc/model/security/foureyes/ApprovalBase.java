
package com.sos.joc.model.security.foureyes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.CategoryType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * ApprovalBase
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "requestor",
    "requestUrl",
    "requestBody",
    "category"
})
public class ApprovalBase {

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
     * No args constructor for use in serialization
     * 
     */
    public ApprovalBase() {
    }

    /**
     * 
     * @param requestBody
     * @param requestUrl
     * @param category
     * @param requestor
     */
    public ApprovalBase(String requestor, String requestUrl, RequestBody requestBody, CategoryType category) {
        super();
        this.requestor = requestor;
        this.requestUrl = requestUrl;
        this.requestBody = requestBody;
        this.category = category;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("requestor", requestor).append("requestUrl", requestUrl).append("requestBody", requestBody).append("category", category).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(category).append(requestBody).append(requestor).append(requestUrl).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ApprovalBase) == false) {
            return false;
        }
        ApprovalBase rhs = ((ApprovalBase) other);
        return new EqualsBuilder().append(category, rhs.category).append(requestBody, rhs.requestBody).append(requestor, rhs.requestor).append(requestUrl, rhs.requestUrl).isEquals();
    }

}
