
package com.sos.joc.model.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * cluster state
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "severity",
    "_text",
    "lossNode"
})
public class ClusterState {

    /**
     *  0=ClusterCoupled, 1=ClusterNodesAppointed,ClusterPassiveLost,ClusterSwitchedOver,ClusterFailOver 2=ClusterPreparedToBeCoupled,ClusterEmpty 3=ClusterUnknown
     * (Required)
     * 
     */
    @JsonProperty("severity")
    @JsonPropertyDescription("0=ClusterCoupled, 1=ClusterNodesAppointed,ClusterPassiveLost,ClusterSwitchedOver,ClusterFailOver 2=ClusterPreparedToBeCoupled,ClusterEmpty 3=ClusterUnknown")
    private Integer severity;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    private String _text;
    @JsonProperty("lossNode")
    private String lossNode;

    /**
     *  0=ClusterCoupled, 1=ClusterNodesAppointed,ClusterPassiveLost,ClusterSwitchedOver,ClusterFailOver 2=ClusterPreparedToBeCoupled,ClusterEmpty 3=ClusterUnknown
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public Integer getSeverity() {
        return severity;
    }

    /**
     *  0=ClusterCoupled, 1=ClusterNodesAppointed,ClusterPassiveLost,ClusterSwitchedOver,ClusterFailOver 2=ClusterPreparedToBeCoupled,ClusterEmpty 3=ClusterUnknown
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public String get_text() {
        return _text;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public void set_text(String _text) {
        this._text = _text;
    }
    
    @JsonProperty("lossNode")
    public String getLossNode() {
        return lossNode;
    }
    
    @JsonProperty("lossNode")
    public void setLossNode(String lossNode) {
        this.lossNode = lossNode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("severity", severity).append("_text", _text).append("lossNode", lossNode).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(severity).append(_text).append(lossNode).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ClusterState) == false) {
            return false;
        }
        ClusterState rhs = ((ClusterState) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(_text, rhs._text).append(lossNode, rhs.lossNode).isEquals();
    }

}
