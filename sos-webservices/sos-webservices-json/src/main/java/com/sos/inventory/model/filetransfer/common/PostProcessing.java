
package com.sos.inventory.model.filetransfer.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "commandAfterFile",
    "commandAfterOperationOnSuccess",
    "commandAfterOperationOnError",
    "commandAfterOperationFinal",
    "commandBeforeRename"
})
public class PostProcessing {

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandAfterFile")
    private String commandAfterFile;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandAfterOperationOnSuccess")
    private String commandAfterOperationOnSuccess;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandAfterOperationOnError")
    private String commandAfterOperationOnError;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandAfterOperationFinal")
    private String commandAfterOperationFinal;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandBeforeRename")
    private String commandBeforeRename;

    /**
     * No args constructor for use in serialization
     * 
     */
    public PostProcessing() {
    }

    /**
     * 
     * @param commandAfterOperationOnSuccess
     * @param commandAfterOperationFinal
     * @param commandAfterFile
     * @param commandBeforeRename
     * @param commandAfterOperationOnError
     */
    public PostProcessing(String commandAfterFile, String commandAfterOperationOnSuccess, String commandAfterOperationOnError, String commandAfterOperationFinal, String commandBeforeRename) {
        super();
        this.commandAfterFile = commandAfterFile;
        this.commandAfterOperationOnSuccess = commandAfterOperationOnSuccess;
        this.commandAfterOperationOnError = commandAfterOperationOnError;
        this.commandAfterOperationFinal = commandAfterOperationFinal;
        this.commandBeforeRename = commandBeforeRename;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandAfterFile")
    public String getCommandAfterFile() {
        return commandAfterFile;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandAfterFile")
    public void setCommandAfterFile(String commandAfterFile) {
        this.commandAfterFile = commandAfterFile;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandAfterOperationOnSuccess")
    public String getCommandAfterOperationOnSuccess() {
        return commandAfterOperationOnSuccess;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandAfterOperationOnSuccess")
    public void setCommandAfterOperationOnSuccess(String commandAfterOperationOnSuccess) {
        this.commandAfterOperationOnSuccess = commandAfterOperationOnSuccess;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandAfterOperationOnError")
    public String getCommandAfterOperationOnError() {
        return commandAfterOperationOnError;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandAfterOperationOnError")
    public void setCommandAfterOperationOnError(String commandAfterOperationOnError) {
        this.commandAfterOperationOnError = commandAfterOperationOnError;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandAfterOperationFinal")
    public String getCommandAfterOperationFinal() {
        return commandAfterOperationFinal;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandAfterOperationFinal")
    public void setCommandAfterOperationFinal(String commandAfterOperationFinal) {
        this.commandAfterOperationFinal = commandAfterOperationFinal;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandBeforeRename")
    public String getCommandBeforeRename() {
        return commandBeforeRename;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandBeforeRename")
    public void setCommandBeforeRename(String commandBeforeRename) {
        this.commandBeforeRename = commandBeforeRename;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("commandAfterFile", commandAfterFile).append("commandAfterOperationOnSuccess", commandAfterOperationOnSuccess).append("commandAfterOperationOnError", commandAfterOperationOnError).append("commandAfterOperationFinal", commandAfterOperationFinal).append("commandBeforeRename", commandBeforeRename).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(commandAfterFile).append(commandAfterOperationOnSuccess).append(commandBeforeRename).append(commandAfterOperationOnError).append(commandAfterOperationFinal).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PostProcessing) == false) {
            return false;
        }
        PostProcessing rhs = ((PostProcessing) other);
        return new EqualsBuilder().append(commandAfterFile, rhs.commandAfterFile).append(commandAfterOperationOnSuccess, rhs.commandAfterOperationOnSuccess).append(commandBeforeRename, rhs.commandBeforeRename).append(commandAfterOperationOnError, rhs.commandAfterOperationOnError).append(commandAfterOperationFinal, rhs.commandAfterOperationFinal).isEquals();
    }

}
