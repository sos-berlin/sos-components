
package com.sos.inventory.model.instruction;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * if
 * <p>
 * instruction with fixed property 'TYPE':'If'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "predicate",
    "then",
    "else"
})
public class IfElse
    extends Instruction
{

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
     * instructions
     * <p>
     * only for the validation, not used as pojo
     * 
     */
    @JsonProperty("else")
    @JsonPropertyDescription("only for the validation, not used as pojo")
    private OptionalInstructions _else;

    /**
     * No args constructor for use in serialization
     * 
     */
    public IfElse() {
    }

    /**
     * 
     * @param predicate
     * @param _else
     * @param then
     * 
     */
    public IfElse(String predicate, Instructions then, OptionalInstructions _else) {
        super();
        this.predicate = predicate;
        this.then = then;
        this._else = _else;
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

    /**
     * instructions
     * <p>
     * only for the validation, not used as pojo
     * 
     */
    @JsonProperty("else")
    public OptionalInstructions getElse() {
        return _else;
    }

    /**
     * instructions
     * <p>
     * only for the validation, not used as pojo
     * 
     */
    @JsonProperty("else")
    public void setElse(OptionalInstructions _else) {
        this._else = _else;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("predicate", predicate).append("then", then).append("_else", _else).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(predicate).append(_else).append(then).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof IfElse) == false) {
            return false;
        }
        IfElse rhs = ((IfElse) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(predicate, rhs.predicate).append(_else, rhs._else).append(then, rhs.then).isEquals();
    }

}
