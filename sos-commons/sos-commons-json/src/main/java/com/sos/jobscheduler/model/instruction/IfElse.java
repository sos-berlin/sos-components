
package com.sos.jobscheduler.model.instruction;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.jobscheduler.model.common.IInstruction;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * if
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
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
    @JsonProperty("TYPE")
    @JacksonXmlProperty(localName = "TYPE")
    private InstructionType tYPE = InstructionType.fromValue("If");
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("predicate")
    @JacksonXmlProperty(localName = "predicate")
    private String predicate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("then")
    @JacksonXmlProperty(localName = "then")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "then")
    private List<IInstruction> then = null;
    @JsonProperty("else")
    @JacksonXmlProperty(localName = "else")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "else")
    private List<IInstruction> _else = null;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JacksonXmlProperty(localName = "TYPE")
    public InstructionType getTYPE() {
        return tYPE;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JacksonXmlProperty(localName = "TYPE")
    public void setTYPE(InstructionType tYPE) {
        this.tYPE = tYPE;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("predicate")
    @JacksonXmlProperty(localName = "predicate")
    public String getPredicate() {
        return predicate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("predicate")
    @JacksonXmlProperty(localName = "predicate")
    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("then")
    @JacksonXmlProperty(localName = "then")
    public List<IInstruction> getThen() {
        return then;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("then")
    @JacksonXmlProperty(localName = "then")
    public void setThen(List<IInstruction> then) {
        this.then = then;
    }

    @JsonProperty("else")
    @JacksonXmlProperty(localName = "else")
    public List<IInstruction> getElse() {
        return _else;
    }

    @JsonProperty("else")
    @JacksonXmlProperty(localName = "else")
    public void setElse(List<IInstruction> _else) {
        this._else = _else;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("tYPE", tYPE).append("predicate", predicate).append("then", then).append("_else", _else).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(predicate).append(_else).append(then).append(tYPE).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(predicate, rhs.predicate).append(_else, rhs._else).append(then, rhs.then).append(tYPE, rhs.tYPE).isEquals();
    }

}
