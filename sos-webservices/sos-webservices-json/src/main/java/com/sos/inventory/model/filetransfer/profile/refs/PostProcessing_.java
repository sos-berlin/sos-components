
package com.sos.inventory.model.filetransfer.profile.refs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "commandAfterOperationOnSuccess",
    "commandAfterOperationOnError",
    "commandAfterOperationFinal"
})
public class PostProcessing_ {

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
     * No args constructor for use in serialization
     * 
     */
    public PostProcessing_() {
    }

    /**
     * 
     * @param commandAfterOperationOnSuccess
     * @param commandAfterOperationFinal
     * @param commandAfterOperationOnError
     */
    public PostProcessing_(String commandAfterOperationOnSuccess, String commandAfterOperationOnError, String commandAfterOperationFinal) {
        super();
        this.commandAfterOperationOnSuccess = commandAfterOperationOnSuccess;
        this.commandAfterOperationOnError = commandAfterOperationOnError;
        this.commandAfterOperationFinal = commandAfterOperationFinal;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("commandAfterOperationOnSuccess", commandAfterOperationOnSuccess).append("commandAfterOperationOnError", commandAfterOperationOnError).append("commandAfterOperationFinal", commandAfterOperationFinal).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(commandAfterOperationOnSuccess).append(commandAfterOperationOnError).append(commandAfterOperationFinal).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PostProcessing_) == false) {
            return false;
        }
        PostProcessing_ rhs = ((PostProcessing_) other);
        return new EqualsBuilder().append(commandAfterOperationOnSuccess, rhs.commandAfterOperationOnSuccess).append(commandAfterOperationOnError, rhs.commandAfterOperationOnError).append(commandAfterOperationFinal, rhs.commandAfterOperationFinal).isEquals();
    }

}
