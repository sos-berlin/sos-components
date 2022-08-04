
package com.sos.inventory.model.job;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * job return code warning
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "warning"
})
public class JobReturnCodeWarning {

    @JsonProperty("warning")
    private List<Integer> warning = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public JobReturnCodeWarning() {
    }

    /**
     * 
     * @param warning
     */
    public JobReturnCodeWarning(List<Integer> warning) {
        super();
        this.warning = warning;
    }

    @JsonProperty("warning")
    public List<Integer> getWarning() {
        return warning;
    }

    @JsonProperty("warning")
    public void setWarning(List<Integer> warning) {
        this.warning = warning;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("warning", warning).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(warning).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobReturnCodeWarning) == false) {
            return false;
        }
        JobReturnCodeWarning rhs = ((JobReturnCodeWarning) other);
        return new EqualsBuilder().append(warning, rhs.warning).isEquals();
    }

}
