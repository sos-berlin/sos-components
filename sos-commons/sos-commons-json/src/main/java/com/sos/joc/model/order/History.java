
package com.sos.joc.model.order;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "historyId",
    "steps"
})
public class History {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("historyId")
    @JacksonXmlProperty(localName = "historyId")
    private String historyId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("steps")
    @JacksonXmlProperty(localName = "step")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "steps")
    private List<OrderStepHistoryItem> steps = new ArrayList<OrderStepHistoryItem>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("historyId")
    @JacksonXmlProperty(localName = "historyId")
    public String getHistoryId() {
        return historyId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("historyId")
    @JacksonXmlProperty(localName = "historyId")
    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("steps")
    @JacksonXmlProperty(localName = "step")
    public List<OrderStepHistoryItem> getSteps() {
        return steps;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("steps")
    @JacksonXmlProperty(localName = "step")
    public void setSteps(List<OrderStepHistoryItem> steps) {
        this.steps = steps;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("historyId", historyId).append("steps", steps).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(steps).append(historyId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof History) == false) {
            return false;
        }
        History rhs = ((History) other);
        return new EqualsBuilder().append(steps, rhs.steps).append(historyId, rhs.historyId).isEquals();
    }

}
