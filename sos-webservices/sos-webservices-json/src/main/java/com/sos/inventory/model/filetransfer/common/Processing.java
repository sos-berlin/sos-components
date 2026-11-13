
package com.sos.inventory.model.filetransfer.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * SSH processing options.
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "preProcessing",
    "postProcessing",
    "commandDelimiter"
})
public class Processing {

    @JsonProperty("preProcessing")
    private PreProcessing preProcessing;
    @JsonProperty("postProcessing")
    private PostProcessing postProcessing;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandDelimiter")
    private String commandDelimiter;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Processing() {
    }

    /**
     * 
     * @param commandDelimiter
     * @param postProcessing
     * @param preProcessing
     */
    public Processing(PreProcessing preProcessing, PostProcessing postProcessing, String commandDelimiter) {
        super();
        this.preProcessing = preProcessing;
        this.postProcessing = postProcessing;
        this.commandDelimiter = commandDelimiter;
    }

    @JsonProperty("preProcessing")
    public PreProcessing getPreProcessing() {
        return preProcessing;
    }

    @JsonProperty("preProcessing")
    public void setPreProcessing(PreProcessing preProcessing) {
        this.preProcessing = preProcessing;
    }

    @JsonProperty("postProcessing")
    public PostProcessing getPostProcessing() {
        return postProcessing;
    }

    @JsonProperty("postProcessing")
    public void setPostProcessing(PostProcessing postProcessing) {
        this.postProcessing = postProcessing;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandDelimiter")
    public String getCommandDelimiter() {
        return commandDelimiter;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("commandDelimiter")
    public void setCommandDelimiter(String commandDelimiter) {
        this.commandDelimiter = commandDelimiter;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("preProcessing", preProcessing).append("postProcessing", postProcessing).append("commandDelimiter", commandDelimiter).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(commandDelimiter).append(postProcessing).append(preProcessing).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Processing) == false) {
            return false;
        }
        Processing rhs = ((Processing) other);
        return new EqualsBuilder().append(commandDelimiter, rhs.commandDelimiter).append(postProcessing, rhs.postProcessing).append(preProcessing, rhs.preProcessing).isEquals();
    }

}
