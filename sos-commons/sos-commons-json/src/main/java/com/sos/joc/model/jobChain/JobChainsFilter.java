
package com.sos.joc.model.jobChain;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.joc.model.common.Folder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * jobChainsFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "jobChains",
    "compact",
    "regex",
    "folders",
    "states",
    "close",
    "maxOrders"
})
public class JobChainsFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("jobChains")
    @JacksonXmlProperty(localName = "jobChain")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "jobChains")
    private List<JobChainPath> jobChains = new ArrayList<JobChainPath>();
    /**
     * compact parameter
     * <p>
     * controls if the object view is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JsonPropertyDescription("controls if the object view is compact or detailed")
    @JacksonXmlProperty(localName = "compact")
    private Boolean compact = false;
    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    @JsonPropertyDescription("regular expression to filter JobScheduler objects by matching the path")
    @JacksonXmlProperty(localName = "regex")
    private String regex;
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    @JacksonXmlProperty(localName = "folder")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "folders")
    private List<Folder> folders = new ArrayList<Folder>();
    @JsonProperty("states")
    @JacksonXmlProperty(localName = "state")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "states")
    private List<JobChainStateText> states = new ArrayList<JobChainStateText>();
    /**
     * concerns only events
     * 
     */
    @JsonProperty("close")
    @JsonPropertyDescription("concerns only events")
    @JacksonXmlProperty(localName = "close")
    private Boolean close = false;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxOrders")
    @JacksonXmlProperty(localName = "maxOrders")
    private Integer maxOrders;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    @JsonProperty("jobChains")
    @JacksonXmlProperty(localName = "jobChain")
    public List<JobChainPath> getJobChains() {
        return jobChains;
    }

    @JsonProperty("jobChains")
    @JacksonXmlProperty(localName = "jobChain")
    public void setJobChains(List<JobChainPath> jobChains) {
        this.jobChains = jobChains;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object view is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JacksonXmlProperty(localName = "compact")
    public Boolean getCompact() {
        return compact;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object view is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JacksonXmlProperty(localName = "compact")
    public void setCompact(Boolean compact) {
        this.compact = compact;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    @JacksonXmlProperty(localName = "regex")
    public String getRegex() {
        return regex;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    @JacksonXmlProperty(localName = "regex")
    public void setRegex(String regex) {
        this.regex = regex;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    @JacksonXmlProperty(localName = "folder")
    public List<Folder> getFolders() {
        return folders;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    @JacksonXmlProperty(localName = "folder")
    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    @JsonProperty("states")
    @JacksonXmlProperty(localName = "state")
    public List<JobChainStateText> getStates() {
        return states;
    }

    @JsonProperty("states")
    @JacksonXmlProperty(localName = "state")
    public void setStates(List<JobChainStateText> states) {
        this.states = states;
    }

    /**
     * concerns only events
     * 
     */
    @JsonProperty("close")
    @JacksonXmlProperty(localName = "close")
    public Boolean getClose() {
        return close;
    }

    /**
     * concerns only events
     * 
     */
    @JsonProperty("close")
    @JacksonXmlProperty(localName = "close")
    public void setClose(Boolean close) {
        this.close = close;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxOrders")
    @JacksonXmlProperty(localName = "maxOrders")
    public Integer getMaxOrders() {
        return maxOrders;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxOrders")
    @JacksonXmlProperty(localName = "maxOrders")
    public void setMaxOrders(Integer maxOrders) {
        this.maxOrders = maxOrders;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("jobChains", jobChains).append("compact", compact).append("regex", regex).append("folders", folders).append("states", states).append("close", close).append("maxOrders", maxOrders).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(regex).append(folders).append(compact).append(jobChains).append(jobschedulerId).append(maxOrders).append(close).append(states).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobChainsFilter) == false) {
            return false;
        }
        JobChainsFilter rhs = ((JobChainsFilter) other);
        return new EqualsBuilder().append(regex, rhs.regex).append(folders, rhs.folders).append(compact, rhs.compact).append(jobChains, rhs.jobChains).append(jobschedulerId, rhs.jobschedulerId).append(maxOrders, rhs.maxOrders).append(close, rhs.close).append(states, rhs.states).isEquals();
    }

}
