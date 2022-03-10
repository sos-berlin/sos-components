
package com.sos.joc.model.security.configuration.permissions.joc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "manage"
})
public class Cluster {

    @JsonProperty("manage")
    private Boolean manage = false;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Cluster() {
    }

    /**
     * 
     * @param manage
     */
    public Cluster(Boolean manage) {
        super();
        this.manage = manage;
    }

    @JsonProperty("manage")
    public Boolean getManage() {
        return manage;
    }

    @JsonProperty("manage")
    public void setManage(Boolean manage) {
        this.manage = manage;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("manage", manage).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(manage).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Cluster) == false) {
            return false;
        }
        Cluster rhs = ((Cluster) other);
        return new EqualsBuilder().append(manage, rhs.manage).isEquals();
    }

}
