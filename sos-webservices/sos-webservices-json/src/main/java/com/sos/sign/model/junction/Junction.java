
package com.sos.sign.model.junction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.deploy.DeployType;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "path",
    "versionId",
    "lifetime",
    "orderId"
})
public class Junction implements IDeployObject
{

    /**
     * deployType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    private DeployType tYPE = DeployType.fromValue("Workflow");
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    private String path;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("versionId")
    private String versionId;
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
     * No args constructor for use in serialization
     * 
     */
    public Junction() {
    }

    /**
     * 
     * @param path
     * @param versionId
     * @param orderId
     * @param lifetime
     */
    public Junction(String path, String versionId, Integer lifetime, String orderId) {
        super();
        this.path = path;
        this.versionId = versionId;
        this.lifetime = lifetime;
        this.orderId = orderId;
    }

    /**
     * deployType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public DeployType getTYPE() {
        return tYPE;
    }

    /**
     * deployType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(DeployType tYPE) {
        this.tYPE = tYPE;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("versionId")
    public String getVersionId() {
        return versionId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("versionId")
    public void setVersionId(String versionId) {
        this.versionId = versionId;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("path", path).append("versionId", versionId).append("lifetime", lifetime).append("orderId", orderId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(lifetime).append(path).append(versionId).append(tYPE).append(orderId).toHashCode();
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
        return new EqualsBuilder().append(lifetime, rhs.lifetime).append(path, rhs.path).append(versionId, rhs.versionId).append(tYPE, rhs.tYPE).append(orderId, rhs.orderId).isEquals();
    }

}
