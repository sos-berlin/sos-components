
package com.sos.jobscheduler.model.order;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "immediately",
    "position"
})
public class Kill {

    @JsonProperty("immediately")
    private Boolean immediately = false;
    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("position")
    @JsonPropertyDescription("Actually, each even item is a string, each odd item is an integer")
    private List<Object> position = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Kill() {
    }

    /**
     * 
     * @param immediately
     * @param position
     */
    public Kill(Boolean immediately, List<Object> position) {
        super();
        this.immediately = immediately;
        this.position = position;
    }

    @JsonProperty("immediately")
    public Boolean getImmediately() {
        return immediately;
    }

    @JsonProperty("immediately")
    public void setImmediately(Boolean immediately) {
        this.immediately = immediately;
    }

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("position")
    public List<Object> getPosition() {
        return position;
    }

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("position")
    public void setPosition(List<Object> position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("immediately", immediately).append("position", position).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(immediately).append(position).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Kill) == false) {
            return false;
        }
        Kill rhs = ((Kill) other);
        return new EqualsBuilder().append(immediately, rhs.immediately).append(position, rhs.position).isEquals();
    }

}
