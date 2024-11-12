
package com.sos.inventory.model.instruction;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * CaseWhen
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "whens",
    "else"
})
public class CaseWhen
    extends Instruction
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("whens")
    @JsonAlias({
        "ifThens"
    })
    private List<When> whens = null;
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
    public CaseWhen() {
    }

    /**
     * 
     * @param _else
     * @param whens
     */
    public CaseWhen(List<When> whens, OptionalInstructions _else) {
        this.whens = whens;
        this._else = _else;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("whens")
    public List<When> getWhens() {
        return whens;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("whens")
    public void setWhens(List<When> whens) {
        this.whens = whens;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("whens", whens).append("_else", _else).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(whens).append(_else).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CaseWhen) == false) {
            return false;
        }
        CaseWhen rhs = ((CaseWhen) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(whens, rhs.whens).append(_else, rhs._else).isEquals();
    }

}
