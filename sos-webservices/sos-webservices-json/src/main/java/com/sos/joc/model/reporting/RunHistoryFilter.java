
package com.sos.joc.model.reporting;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.inventory.model.report.TemplateId;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * run history filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "templateNames",
    "states"
})
public class RunHistoryFilter
    extends ReportPaths
{

    @JsonProperty("templateNames")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<TemplateId> templateNames = new LinkedHashSet<TemplateId>();
    @JsonProperty("states")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ReportRunStateText> states = new LinkedHashSet<ReportRunStateText>();

    @JsonProperty("templateNames")
    public Set<TemplateId> getTemplateNames() {
        return templateNames;
    }

    @JsonProperty("templateNames")
    public void setTemplateNames(Set<TemplateId> templateNames) {
        this.templateNames = templateNames;
    }

    @JsonProperty("states")
    public Set<ReportRunStateText> getStates() {
        return states;
    }

    @JsonProperty("states")
    public void setStates(Set<ReportRunStateText> states) {
        this.states = states;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("templateNames", templateNames).append("states", states).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(templateNames).append(states).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RunHistoryFilter) == false) {
            return false;
        }
        RunHistoryFilter rhs = ((RunHistoryFilter) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(templateNames, rhs.templateNames).append(states, rhs.states).isEquals();
    }

}
