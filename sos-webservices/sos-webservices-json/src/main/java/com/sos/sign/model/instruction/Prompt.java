
package com.sos.sign.model.instruction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.instruction.InstructionType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * prompt
 * <p>
 * instruction with fixed property 'TYPE':'Prompt'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "question"
})
public class Prompt
    extends Instruction
{

    @JsonProperty("question")
    private String question;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Prompt() {
    }

    /**
     * 
     * @param question
     * @param tYPE
     */
    public Prompt(String question, InstructionType tYPE) {
        super(tYPE);
        this.question = question;
    }

    @JsonProperty("question")
    public String getQuestion() {
        return question;
    }

    @JsonProperty("question")
    public void setQuestion(String question) {
        this.question = question;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("question", question).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(question).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Prompt) == false) {
            return false;
        }
        Prompt rhs = ((Prompt) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(question, rhs.question).isEquals();
    }

}
