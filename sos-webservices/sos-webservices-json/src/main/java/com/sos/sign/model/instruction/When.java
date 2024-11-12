
package com.sos.sign.model.instruction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * when
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "predicate",
    "then"
})
public class When {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("predicate")
    private String predicate;
    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("then")
    private Instructions then;

    /**
     * No args constructor for use in serialization
     * 
     */
    public When() {
    }

    /**
     * 
     * @param predicate
     * @param then
     */
    public When(String predicate, Instructions then) {
        super();
        this.predicate = predicate;
        this.then = then;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("predicate")
    public String getPredicate() {
        return predicate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("predicate")
    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("then")
    public Instructions getThen() {
        return then;
    }

    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("then")
    public void setThen(Instructions then) {
        this.then = then;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("predicate", predicate).append("then", then).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(predicate).append(then).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof When) == false) {
            return false;
        }
        When rhs = ((When) other);
        return new EqualsBuilder().append(predicate, rhs.predicate).append(then, rhs.then).isEquals();
    }

}
