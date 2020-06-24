
package com.sos.joc.model.joc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.jobscheduler.ConnectionState;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "title",
    "state"
})
public class ControllerConnectionState {

    @JsonProperty("title")
    private String title;
    /**
     * connection state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private ConnectionState state;

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * connection state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public ConnectionState getState() {
        return state;
    }

    /**
     * connection state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(ConnectionState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("title", title).append("state", state).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(title).append(state).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ControllerConnectionState) == false) {
            return false;
        }
        ControllerConnectionState rhs = ((ControllerConnectionState) other);
        return new EqualsBuilder().append(title, rhs.title).append(state, rhs.state).isEquals();
    }

}
