
package com.sos.jobscheduler.model.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sos.jobscheduler.model.common.ClassHelper;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * abstract command
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE"
})
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME, 
		include = JsonTypeInfo.As.PROPERTY, 
		property = "TYPE",
		visible = true)
@JsonSubTypes({ 
    @JsonSubTypes.Type(value = JSBatchCommands.class, name = "Batch"),
    @JsonSubTypes.Type(value = CancelOrder.class, name = "CancelOrder"),
    @JsonSubTypes.Type(value = Abort.class, name = "EmercencyStop"),
	@JsonSubTypes.Type(value = Terminate.class, name = "Terminate"),
	@JsonSubTypes.Type(value = ReplaceRepo.class, name = "ReplaceRepo"),
	@JsonSubTypes.Type(value = UpdateRepo.class, name = "UpdateRepo")})
public abstract class Command
    extends ClassHelper
{

    /**
     * commandType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JsonIgnore
    private CommandType tYPE;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Command() {
    }

    /**
     * 
     * @param tYPE
     */
    public Command(CommandType tYPE) {
        super();
        this.tYPE = tYPE;
    }

    /**
     * commandType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JsonIgnore
    public CommandType getTYPE() {
        return tYPE;
    }

    /**
     * commandType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(CommandType tYPE) {
        this.tYPE = tYPE;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("tYPE", tYPE).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(tYPE).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Command) == false) {
            return false;
        }
        Command rhs = ((Command) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(tYPE, rhs.tYPE).isEquals();
    }

}
