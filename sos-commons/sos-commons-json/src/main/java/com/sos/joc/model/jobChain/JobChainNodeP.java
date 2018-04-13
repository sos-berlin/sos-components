
package com.sos.joc.model.jobChain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * jobChainNode (permanent part)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "nextNode",
    "errorNode",
    "job",
    "jobChain",
    "level",
    "onError",
    "delay",
    "remove",
    "move"
})
public class JobChainNodeP {

    @JsonProperty("name")
    @JacksonXmlProperty(localName = "name")
    private String name;
    @JsonProperty("nextNode")
    @JacksonXmlProperty(localName = "nextNode")
    private String nextNode;
    @JsonProperty("errorNode")
    @JacksonXmlProperty(localName = "errorNode")
    private String errorNode;
    @JsonProperty("job")
    @JacksonXmlProperty(localName = "job")
    private JobChainNodeJobP job;
    /**
     * job chain object is included in nestedJobChains collection
     * 
     */
    @JsonProperty("jobChain")
    @JsonPropertyDescription("job chain object is included in nestedJobChains collection")
    @JacksonXmlProperty(localName = "jobChain")
    private JobChainNodeJobChainP jobChain;
    /**
     * Only relevant for job chain with splits and syncs. For example to imagine splits/sync in the job chain list view with different indents
     * 
     */
    @JsonProperty("level")
    @JsonPropertyDescription("Only relevant for job chain with splits and syncs. For example to imagine splits/sync in the job chain list view with different indents")
    @JacksonXmlProperty(localName = "level")
    private Integer level;
    /**
     * possible values are 'suspend', 'setback' or it isn't set
     * 
     */
    @JsonProperty("onError")
    @JsonPropertyDescription("possible values are 'suspend', 'setback' or it isn't set")
    @JacksonXmlProperty(localName = "onError")
    private String onError;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("delay")
    @JacksonXmlProperty(localName = "delay")
    private Integer delay;
    /**
     * for file order sink
     * 
     */
    @JsonProperty("remove")
    @JsonPropertyDescription("for file order sink")
    @JacksonXmlProperty(localName = "remove")
    private Boolean remove;
    /**
     * for file order sink, a directory path is expected
     * 
     */
    @JsonProperty("move")
    @JsonPropertyDescription("for file order sink, a directory path is expected")
    @JacksonXmlProperty(localName = "move")
    private String move;

    @JsonProperty("name")
    @JacksonXmlProperty(localName = "name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    @JacksonXmlProperty(localName = "name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("nextNode")
    @JacksonXmlProperty(localName = "nextNode")
    public String getNextNode() {
        return nextNode;
    }

    @JsonProperty("nextNode")
    @JacksonXmlProperty(localName = "nextNode")
    public void setNextNode(String nextNode) {
        this.nextNode = nextNode;
    }

    @JsonProperty("errorNode")
    @JacksonXmlProperty(localName = "errorNode")
    public String getErrorNode() {
        return errorNode;
    }

    @JsonProperty("errorNode")
    @JacksonXmlProperty(localName = "errorNode")
    public void setErrorNode(String errorNode) {
        this.errorNode = errorNode;
    }

    @JsonProperty("job")
    @JacksonXmlProperty(localName = "job")
    public JobChainNodeJobP getJob() {
        return job;
    }

    @JsonProperty("job")
    @JacksonXmlProperty(localName = "job")
    public void setJob(JobChainNodeJobP job) {
        this.job = job;
    }

    /**
     * job chain object is included in nestedJobChains collection
     * 
     */
    @JsonProperty("jobChain")
    @JacksonXmlProperty(localName = "jobChain")
    public JobChainNodeJobChainP getJobChain() {
        return jobChain;
    }

    /**
     * job chain object is included in nestedJobChains collection
     * 
     */
    @JsonProperty("jobChain")
    @JacksonXmlProperty(localName = "jobChain")
    public void setJobChain(JobChainNodeJobChainP jobChain) {
        this.jobChain = jobChain;
    }

    /**
     * Only relevant for job chain with splits and syncs. For example to imagine splits/sync in the job chain list view with different indents
     * 
     */
    @JsonProperty("level")
    @JacksonXmlProperty(localName = "level")
    public Integer getLevel() {
        return level;
    }

    /**
     * Only relevant for job chain with splits and syncs. For example to imagine splits/sync in the job chain list view with different indents
     * 
     */
    @JsonProperty("level")
    @JacksonXmlProperty(localName = "level")
    public void setLevel(Integer level) {
        this.level = level;
    }

    /**
     * possible values are 'suspend', 'setback' or it isn't set
     * 
     */
    @JsonProperty("onError")
    @JacksonXmlProperty(localName = "onError")
    public String getOnError() {
        return onError;
    }

    /**
     * possible values are 'suspend', 'setback' or it isn't set
     * 
     */
    @JsonProperty("onError")
    @JacksonXmlProperty(localName = "onError")
    public void setOnError(String onError) {
        this.onError = onError;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("delay")
    @JacksonXmlProperty(localName = "delay")
    public Integer getDelay() {
        return delay;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("delay")
    @JacksonXmlProperty(localName = "delay")
    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    /**
     * for file order sink
     * 
     */
    @JsonProperty("remove")
    @JacksonXmlProperty(localName = "remove")
    public Boolean getRemove() {
        return remove;
    }

    /**
     * for file order sink
     * 
     */
    @JsonProperty("remove")
    @JacksonXmlProperty(localName = "remove")
    public void setRemove(Boolean remove) {
        this.remove = remove;
    }

    /**
     * for file order sink, a directory path is expected
     * 
     */
    @JsonProperty("move")
    @JacksonXmlProperty(localName = "move")
    public String getMove() {
        return move;
    }

    /**
     * for file order sink, a directory path is expected
     * 
     */
    @JsonProperty("move")
    @JacksonXmlProperty(localName = "move")
    public void setMove(String move) {
        this.move = move;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("nextNode", nextNode).append("errorNode", errorNode).append("job", job).append("jobChain", jobChain).append("level", level).append("onError", onError).append("delay", delay).append("remove", remove).append("move", move).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(move).append(onError).append(delay).append(level).append(nextNode).append(name).append(jobChain).append(errorNode).append(job).append(remove).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobChainNodeP) == false) {
            return false;
        }
        JobChainNodeP rhs = ((JobChainNodeP) other);
        return new EqualsBuilder().append(move, rhs.move).append(onError, rhs.onError).append(delay, rhs.delay).append(level, rhs.level).append(nextNode, rhs.nextNode).append(name, rhs.name).append(jobChain, rhs.jobChain).append(errorNode, rhs.errorNode).append(job, rhs.job).append(remove, rhs.remove).isEquals();
    }

}
