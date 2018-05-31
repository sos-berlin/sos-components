
package com.sos.jobscheduler.model.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * terminate (and restart)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "withRestart"
})
public class Terminate
    extends Command
    implements ICommand
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JacksonXmlProperty(localName = "TYPE")
    private CommandType tYPE = CommandType.fromValue("Terminate");
    @JsonProperty("withRestart")
    @JacksonXmlProperty(localName = "withRestart")
    private Boolean withRestart;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JacksonXmlProperty(localName = "TYPE")
    public CommandType getTYPE() {
        return tYPE;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JacksonXmlProperty(localName = "TYPE")
    public void setTYPE(CommandType tYPE) {
        this.tYPE = tYPE;
    }

    @JsonProperty("withRestart")
    @JacksonXmlProperty(localName = "withRestart")
    public Boolean getWithRestart() {
        return withRestart;
    }

    @JsonProperty("withRestart")
    @JacksonXmlProperty(localName = "withRestart")
    public void setWithRestart(Boolean withRestart) {
        this.withRestart = withRestart;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("tYPE", tYPE).append("withRestart", withRestart).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(tYPE).append(withRestart).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Terminate) == false) {
            return false;
        }
        Terminate rhs = ((Terminate) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(tYPE, rhs.tYPE).append(withRestart, rhs.withRestart).isEquals();
    }

}
