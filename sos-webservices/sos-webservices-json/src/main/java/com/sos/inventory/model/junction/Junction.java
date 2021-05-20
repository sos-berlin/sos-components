
package com.sos.inventory.model.junction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.common.IDeployObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * junction
 * <p>
 * deploy object with fixed property 'TYPE':'Junction'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "TYPE",
    "lifetime",
    "orderId",
    "documentationName",
    "title"
})
public class Junction implements IConfigurationObject, IDeployObject
{

    /**
     * deployType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    private DeployType tYPE = DeployType.JUNCTION;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("lifetime")
    private Integer lifetime;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("orderId")
    private String orderId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    private String documentationName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Junction() {
    }

    /**
     * 
     * @param orderId
     * @param lifetime
     * @param documentationName
     * 
     * @param title
     */
    public Junction(Integer lifetime, String orderId, String documentationName, String title) {
        super();
        this.lifetime = lifetime;
        this.orderId = orderId;
        this.documentationName = documentationName;
        this.title = title;
    }

    /**
     * deployType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    public DeployType getTYPE() {
        return tYPE;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("lifetime")
    public Integer getLifetime() {
        return lifetime;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("lifetime")
    public void setLifetime(Integer lifetime) {
        this.lifetime = lifetime;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    public String getDocumentationName() {
        return documentationName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    public void setDocumentationName(String documentationName) {
        this.documentationName = documentationName;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("lifetime", lifetime).append("orderId", orderId).append("documentationName", documentationName).append("title", title).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(lifetime).append(documentationName).append(tYPE).append(title).append(orderId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Junction) == false) {
            return false;
        }
        Junction rhs = ((Junction) other);
        return new EqualsBuilder().append(lifetime, rhs.lifetime).append(documentationName, rhs.documentationName).append(tYPE, rhs.tYPE).append(title, rhs.title).append(orderId, rhs.orderId).isEquals();
    }

}
