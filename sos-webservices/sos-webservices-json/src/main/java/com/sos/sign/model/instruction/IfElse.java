
package com.sos.sign.model.instruction;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAlias;
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
    "ifThens",
    "else"
})
public class IfElse
    extends Instruction
{

    @JsonProperty("predicate")
    private String predicate;
    /**
     * instructions
     * <p>
     * 
     * 
     */
    @JsonProperty("then")
    private Instructions then;
    @JsonProperty("ifThens")
    @JsonAlias({
        "whens"
    })
    private List<When> ifThens = null;
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
     * @param ifThens
     * @param then
     */
    public IfElse(String predicate, Instructions then, List<When> ifThens, OptionalInstructions _else) {
        this.predicate = predicate;
        this.then = then;
        this.ifThens = ifThens;
        this._else = _else;
    }

    @JsonProperty("predicate")
    public String getPredicate() {
        return predicate;
    }

    @JsonProperty("predicate")
    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    /**
     * instructions
     * <p>
     * 
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
     * 
     */
    @JsonProperty("then")
    public void setThen(Instructions then) {
        this.then = then;
    }

    @JsonProperty("ifThens")
    public List<When> getIfThens() {
        return ifThens;
    }

    @JsonProperty("ifThens")
    public void setIfThens(List<When> ifThens) {
        this.ifThens = ifThens;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("predicate", predicate).append("then", then).append("ifThens", ifThens).append("_else", _else).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(predicate).append(_else).append(then).append(ifThens).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(predicate, rhs.predicate).append(_else, rhs._else).append(then, rhs.then).append(ifThens, rhs.ifThens).isEquals();
    }

}
