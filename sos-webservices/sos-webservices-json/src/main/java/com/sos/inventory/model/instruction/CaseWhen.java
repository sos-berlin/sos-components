
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
    "cases",
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
    @JsonProperty("cases")
    @JsonAlias({
        "ifThens"
    })
    private List<When> cases = null;
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
    public CaseWhen(List<When> cases, OptionalInstructions _else) {
        this.cases = cases;
        this._else = _else;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("cases")
    public List<When> getCases() {
        return cases;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("cases")
    public void setCases(List<When> cases) {
        this.cases = cases;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("cases", cases).append("_else", _else).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(cases).append(_else).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(cases, rhs.cases).append(_else, rhs._else).isEquals();
    }

}
