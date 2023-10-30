
package com.sos.inventory.model.job;

import java.util.List;
import java.util.SortedSet;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


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
public class JobReturnCodeWarning extends JobReturnCodeHelper {

    @JsonProperty("warning")
    private String warning;

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
    public JobReturnCodeWarning(String warning) {
        super();
        this.warning = getCodes(warning, TYPE.WARNING);
    }
    
    public JobReturnCodeWarning(List<Integer> warning) {
        super();
        this.warning = getCodes(warning, TYPE.WARNING);
    }
    
    public JobReturnCodeWarning(Object warning) {
        super();
        this.warning = getCodes(warning, TYPE.WARNING);
    }

    @JsonProperty("warning")
    public String getWarning() {
        return warning;
    }

    @JsonProperty("warning")
    public void setWarning(Object warning) {
        this.warning = getCodes(warning, TYPE.WARNING);
    }
    
    @JsonIgnore
    public void setWarning(String warning) {
        this.warning = getCodes(warning, TYPE.WARNING);
    }
    
    @JsonIgnore
    public void setWarning(List<Integer> warning) {
        this.warning = getCodes(warning, TYPE.WARNING);
    }
    
    @JsonIgnore
    public boolean isInWarnings(Integer warning) {
        return isInReturnCodes(warning, TYPE.WARNING);
    }
    
    @JsonIgnore
    public List<SortedSet<Integer>> getNormalizedAsList() {
        return normalizedAsList(TYPE.WARNING);
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
