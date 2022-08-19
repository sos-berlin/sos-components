
package com.sos.joc.model.jobtemplate.propagate;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.job.Environment;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "addRequiredArguments",
    "deleteArguments",
    "changes"
})
public class Actions {

    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("addRequiredArguments")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Environment addRequiredArguments;
    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("deleteArguments")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Environment deleteArguments;
    @JsonProperty("changes")
    private List<String> changes = new ArrayList<String>();

    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("addRequiredArguments")
    public Environment getAddRequiredArguments() {
        return addRequiredArguments;
    }

    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("addRequiredArguments")
    public void setAddRequiredArguments(Environment addRequiredArguments) {
        this.addRequiredArguments = addRequiredArguments;
    }

    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("deleteArguments")
    public Environment getDeleteArguments() {
        return deleteArguments;
    }

    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("deleteArguments")
    public void setDeleteArguments(Environment deleteArguments) {
        this.deleteArguments = deleteArguments;
    }

    @JsonProperty("changes")
    public List<String> getChanges() {
        return changes;
    }

    @JsonProperty("changes")
    public void setChanges(List<String> changes) {
        this.changes = changes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("addRequiredArguments", addRequiredArguments).append("deleteArguments", deleteArguments).append("changes", changes).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(changes).append(addRequiredArguments).append(deleteArguments).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Actions) == false) {
            return false;
        }
        Actions rhs = ((Actions) other);
        return new EqualsBuilder().append(changes, rhs.changes).append(addRequiredArguments, rhs.addRequiredArguments).append(deleteArguments, rhs.deleteArguments).isEquals();
    }

}
