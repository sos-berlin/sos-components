
package com.sos.joc.model.history.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * order history log entry
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "instruction",
    "job"
})
public class OrderLogEntryInstruction {

    @JsonProperty("instruction")
    private String instruction;
    @JsonProperty("job")
    private String job;

    @JsonProperty("instruction")
    public String getInstruction() {
        return instruction;
    }

    @JsonProperty("instruction")
    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    @JsonProperty("job")
    public String getJob() {
        return job;
    }

    @JsonProperty("job")
    public void setJob(String job) {
        this.job = job;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("instruction", instruction).append("job", job).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(job).append(instruction).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderLogEntryInstruction) == false) {
            return false;
        }
        OrderLogEntryInstruction rhs = ((OrderLogEntryInstruction) other);
        return new EqualsBuilder().append(job, rhs.job).append(instruction, rhs.instruction).isEquals();
    }

}
