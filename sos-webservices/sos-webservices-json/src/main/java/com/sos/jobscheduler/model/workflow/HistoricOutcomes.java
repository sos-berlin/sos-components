
package com.sos.jobscheduler.model.workflow;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * HistoricOutcomes
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "historicOutcomes"
})
public class HistoricOutcomes {

    @JsonProperty("historicOutcomes")
    private List<Integer> historicOutcomes = new ArrayList<Integer>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public HistoricOutcomes() {
    }

    /**
     * 
     * @param historicOutcomes
     */
    public HistoricOutcomes(List<Integer> historicOutcomes) {
        super();
        this.historicOutcomes = historicOutcomes;
    }

    @JsonProperty("historicOutcomes")
    public List<Integer> getHistoricOutcomes() {
        return historicOutcomes;
    }

    @JsonProperty("historicOutcomes")
    public void setHistoricOutcomes(List<Integer> historicOutcomes) {
        this.historicOutcomes = historicOutcomes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("historicOutcomes", historicOutcomes).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(historicOutcomes).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof HistoricOutcomes) == false) {
            return false;
        }
        HistoricOutcomes rhs = ((HistoricOutcomes) other);
        return new EqualsBuilder().append(historicOutcomes, rhs.historicOutcomes).isEquals();
    }

}
