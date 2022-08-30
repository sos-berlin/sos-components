
package com.sos.js7.converter.js1.common.json.jobstreams;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ConditionRef
 * <p>
 * In Condition
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "job",
    "expressions"
})
public class ConditionRef {

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    private String job;
    @JsonProperty("expressions")
    private List<String> expressions = new ArrayList<String>();

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    public String getJob() {
        return job;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    public void setJob(String job) {
        this.job = job;
    }

    @JsonProperty("expressions")
    public List<String> getExpressions() {
        return expressions;
    }

    @JsonProperty("expressions")
    public void setExpressions(List<String> expressions) {
        this.expressions = expressions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("job", job).append("expressions", expressions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(job).append(expressions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConditionRef) == false) {
            return false;
        }
        ConditionRef rhs = ((ConditionRef) other);
        return new EqualsBuilder().append(job, rhs.job).append(expressions, rhs.expressions).isEquals();
    }

}
