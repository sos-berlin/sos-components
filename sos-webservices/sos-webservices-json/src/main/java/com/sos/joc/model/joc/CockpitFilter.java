
package com.sos.joc.model.joc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * joc cockpit request filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "onlyApiServer",
    "onlyNonApiServer"
})
public class CockpitFilter {

    @JsonProperty("onlyApiServer")
    private Boolean onlyApiServer = false;
    @JsonProperty("onlyNonApiServer")
    private Boolean onlyNonApiServer = false;

    @JsonProperty("onlyApiServer")
    public Boolean getOnlyApiServer() {
        return onlyApiServer;
    }

    @JsonProperty("onlyApiServer")
    public void setOnlyApiServer(Boolean onlyApiServer) {
        this.onlyApiServer = onlyApiServer;
    }

    @JsonProperty("onlyNonApiServer")
    public Boolean getOnlyNonApiServer() {
        return onlyNonApiServer;
    }

    @JsonProperty("onlyNonApiServer")
    public void setOnlyNonApiServer(Boolean onlyNonApiServer) {
        this.onlyNonApiServer = onlyNonApiServer;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("onlyApiServer", onlyApiServer).append("onlyNonApiServer", onlyNonApiServer).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(onlyApiServer).append(onlyNonApiServer).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CockpitFilter) == false) {
            return false;
        }
        CockpitFilter rhs = ((CockpitFilter) other);
        return new EqualsBuilder().append(onlyApiServer, rhs.onlyApiServer).append(onlyNonApiServer, rhs.onlyNonApiServer).isEquals();
    }

}
