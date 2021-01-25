
package com.sos.joc.model.joc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JOC Meta Information
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jocVersion",
    "inventorySchemaVersion",
    "apiVersion"
})
public class JocMetaInfo {

    @JsonProperty("jocVersion")
    private String jocVersion;
    @JsonProperty("inventorySchemaVersion")
    private String inventorySchemaVersion;
    @JsonProperty("apiVersion")
    private String apiVersion;

    @JsonProperty("jocVersion")
    public String getJocVersion() {
        return jocVersion;
    }

    @JsonProperty("jocVersion")
    public void setJocVersion(String jocVersion) {
        this.jocVersion = jocVersion;
    }

    @JsonProperty("inventorySchemaVersion")
    public String getInventorySchemaVersion() {
        return inventorySchemaVersion;
    }

    @JsonProperty("inventorySchemaVersion")
    public void setInventorySchemaVersion(String inventorySchemaVersion) {
        this.inventorySchemaVersion = inventorySchemaVersion;
    }

    @JsonProperty("apiVersion")
    public String getApiVersion() {
        return apiVersion;
    }

    @JsonProperty("apiVersion")
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jocVersion", jocVersion).append("inventorySchemaVersion", inventorySchemaVersion).append("apiVersion", apiVersion).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(inventorySchemaVersion).append(apiVersion).append(jocVersion).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JocMetaInfo) == false) {
            return false;
        }
        JocMetaInfo rhs = ((JocMetaInfo) other);
        return new EqualsBuilder().append(inventorySchemaVersion, rhs.inventorySchemaVersion).append(apiVersion, rhs.apiVersion).append(jocVersion, rhs.jocVersion).isEquals();
    }

}
