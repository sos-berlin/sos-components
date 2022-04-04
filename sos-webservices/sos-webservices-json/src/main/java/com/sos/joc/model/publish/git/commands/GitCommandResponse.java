
package com.sos.joc.model.publish.git.commands;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Git Commnad Response
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "command",
    "stdOut",
    "stdErr",
    "exitCode"
})
public class GitCommandResponse {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("command")
    private String command;
    @JsonProperty("stdOut")
    private String stdOut;
    @JsonProperty("stdErr")
    private String stdErr;
    @JsonProperty("exitCode")
    private Integer exitCode;

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

    @JsonProperty("stdOut")
    public String getStdOut() {
        return stdOut;
    }

    @JsonProperty("stdOut")
    public void setStdOut(String stdOut) {
        this.stdOut = stdOut;
    }

    @JsonProperty("stdErr")
    public String getStdErr() {
        return stdErr;
    }

    @JsonProperty("stdErr")
    public void setStdErr(String stdErr) {
        this.stdErr = stdErr;
    }

    @JsonProperty("exitCode")
    public Integer getExitCode() {
        return exitCode;
    }

    @JsonProperty("exitCode")
    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("command", command).append("stdOut", stdOut).append("stdErr", stdErr).append("exitCode", exitCode).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(exitCode).append(stdErr).append(stdOut).append(command).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GitCommandResponse) == false) {
            return false;
        }
        GitCommandResponse rhs = ((GitCommandResponse) other);
        return new EqualsBuilder().append(exitCode, rhs.exitCode).append(stdErr, rhs.stdErr).append(stdOut, rhs.stdOut).append(command, rhs.command).isEquals();
    }

}
