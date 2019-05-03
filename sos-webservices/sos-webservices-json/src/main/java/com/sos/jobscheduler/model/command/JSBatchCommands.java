
package com.sos.jobscheduler.model.command;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * commands
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "commands"
})
public class JSBatchCommands
    extends Command
    implements com.sos.jobscheduler.model.command.ICommand
{

    @JsonProperty("commands")
    private List<com.sos.jobscheduler.model.command.ICommand> commands = null;

    @JsonProperty("commands")
    public List<com.sos.jobscheduler.model.command.ICommand> getCommands() {
        return commands;
    }

    @JsonProperty("commands")
    public void setCommands(List<com.sos.jobscheduler.model.command.ICommand> commands) {
        this.commands = commands;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("commands", commands).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(commands).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JSBatchCommands) == false) {
            return false;
        }
        JSBatchCommands rhs = ((JSBatchCommands) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(commands, rhs.commands).isEquals();
    }

}
