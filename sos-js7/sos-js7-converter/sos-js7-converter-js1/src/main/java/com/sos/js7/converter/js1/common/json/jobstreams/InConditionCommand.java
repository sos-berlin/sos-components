
package com.sos.js7.converter.js1.common.json.jobstreams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * In-Condition-Command
 * <p>
 * In Condition
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "command",
    "commandParam"
})
public class InConditionCommand {

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    private Long id;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("command")
    private String command;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("commandParam")
    private String commandParam;

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("command")
    public String getCommand() {
        return command;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("command")
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("commandParam")
    public String getCommandParam() {
        return commandParam;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("commandParam")
    public void setCommandParam(String commandParam) {
        this.commandParam = commandParam;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("command", command).append("commandParam", commandParam).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(commandParam).append(command).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof InConditionCommand) == false) {
            return false;
        }
        InConditionCommand rhs = ((InConditionCommand) other);
        return new EqualsBuilder().append(id, rhs.id).append(commandParam, rhs.commandParam).append(command, rhs.command).isEquals();
    }

}
