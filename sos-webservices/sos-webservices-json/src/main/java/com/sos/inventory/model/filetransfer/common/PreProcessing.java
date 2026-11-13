
package com.sos.inventory.model.filetransfer.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "commandBeforeFile",
    "commandBeforeOperation"
})
public class PreProcessing {

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandBeforeFile")
    private String commandBeforeFile;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandBeforeOperation")
    private String commandBeforeOperation;

    /**
     * No args constructor for use in serialization
     * 
     */
    public PreProcessing() {
    }

    /**
     * 
     * @param commandBeforeFile
     * @param commandBeforeOperation
     */
    public PreProcessing(String commandBeforeFile, String commandBeforeOperation) {
        super();
        this.commandBeforeFile = commandBeforeFile;
        this.commandBeforeOperation = commandBeforeOperation;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandBeforeFile")
    public String getCommandBeforeFile() {
        return commandBeforeFile;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandBeforeFile")
    public void setCommandBeforeFile(String commandBeforeFile) {
        this.commandBeforeFile = commandBeforeFile;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandBeforeOperation")
    public String getCommandBeforeOperation() {
        return commandBeforeOperation;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandBeforeOperation")
    public void setCommandBeforeOperation(String commandBeforeOperation) {
        this.commandBeforeOperation = commandBeforeOperation;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("commandBeforeFile", commandBeforeFile).append("commandBeforeOperation", commandBeforeOperation).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(commandBeforeFile).append(commandBeforeOperation).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PreProcessing) == false) {
            return false;
        }
        PreProcessing rhs = ((PreProcessing) other);
        return new EqualsBuilder().append(commandBeforeFile, rhs.commandBeforeFile).append(commandBeforeOperation, rhs.commandBeforeOperation).isEquals();
    }

}
