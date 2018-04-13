
package com.sos.joc.model.jobscheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.joc.model.common.ConfigurationState;
import com.sos.joc.model.processClass.Process;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * agent cluster
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "surveyDate",
    "path",
    "name",
    "maxProcesses",
    "_type",
    "state",
    "numOfAgents",
    "agents",
    "numOfProcesses",
    "processes",
    "configurationStatus"
})
public class AgentCluster {

    /**
     * survey date of the JobScheduler Master/Agent
     * <p>
     * Current date of the JobScheduler Master/Agent. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Current date of the JobScheduler Master/Agent. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    @JacksonXmlProperty(localName = "surveyDate")
    private Date surveyDate;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "path")
    private String path;
    @JsonProperty("name")
    @JacksonXmlProperty(localName = "name")
    private String name;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxProcesses")
    @JacksonXmlProperty(localName = "maxProcesses")
    private Integer maxProcesses;
    /**
     * agent cluster type
     * <p>
     * the type of agent cluster
     * 
     */
    @JsonProperty("_type")
    @JsonPropertyDescription("the type of agent cluster")
    @JacksonXmlProperty(localName = "_type")
    private AgentClusterType _type;
    /**
     * agent cluster state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    private AgentClusterState state;
    /**
     * num of agents
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfAgents")
    @JacksonXmlProperty(localName = "numOfAgents")
    private NumOfAgentsInCluster numOfAgents;
    @JsonProperty("agents")
    @JacksonXmlProperty(localName = "agent")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "agents")
    private List<AgentOfCluster> agents = new ArrayList<AgentOfCluster>();
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfProcesses")
    @JacksonXmlProperty(localName = "numOfProcesses")
    private Integer numOfProcesses;
    @JsonProperty("processes")
    @JacksonXmlProperty(localName = "process")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "processes")
    private List<Process> processes = new ArrayList<Process>();
    /**
     * configuration status
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationStatus")
    @JacksonXmlProperty(localName = "configurationStatus")
    private ConfigurationState configurationStatus;

    /**
     * survey date of the JobScheduler Master/Agent
     * <p>
     * Current date of the JobScheduler Master/Agent. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("surveyDate")
    @JacksonXmlProperty(localName = "surveyDate")
    public Date getSurveyDate() {
        return surveyDate;
    }

    /**
     * survey date of the JobScheduler Master/Agent
     * <p>
     * Current date of the JobScheduler Master/Agent. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("surveyDate")
    @JacksonXmlProperty(localName = "surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    @JacksonXmlProperty(localName = "path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    @JacksonXmlProperty(localName = "path")
    public void setPath(String path) {
        this.path = path;
    }

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

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxProcesses")
    @JacksonXmlProperty(localName = "maxProcesses")
    public Integer getMaxProcesses() {
        return maxProcesses;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxProcesses")
    @JacksonXmlProperty(localName = "maxProcesses")
    public void setMaxProcesses(Integer maxProcesses) {
        this.maxProcesses = maxProcesses;
    }

    /**
     * agent cluster type
     * <p>
     * the type of agent cluster
     * 
     */
    @JsonProperty("_type")
    @JacksonXmlProperty(localName = "_type")
    public AgentClusterType get_type() {
        return _type;
    }

    /**
     * agent cluster type
     * <p>
     * the type of agent cluster
     * 
     */
    @JsonProperty("_type")
    @JacksonXmlProperty(localName = "_type")
    public void set_type(AgentClusterType _type) {
        this._type = _type;
    }

    /**
     * agent cluster state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    public AgentClusterState getState() {
        return state;
    }

    /**
     * agent cluster state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    public void setState(AgentClusterState state) {
        this.state = state;
    }

    /**
     * num of agents
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfAgents")
    @JacksonXmlProperty(localName = "numOfAgents")
    public NumOfAgentsInCluster getNumOfAgents() {
        return numOfAgents;
    }

    /**
     * num of agents
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfAgents")
    @JacksonXmlProperty(localName = "numOfAgents")
    public void setNumOfAgents(NumOfAgentsInCluster numOfAgents) {
        this.numOfAgents = numOfAgents;
    }

    @JsonProperty("agents")
    @JacksonXmlProperty(localName = "agent")
    public List<AgentOfCluster> getAgents() {
        return agents;
    }

    @JsonProperty("agents")
    @JacksonXmlProperty(localName = "agent")
    public void setAgents(List<AgentOfCluster> agents) {
        this.agents = agents;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfProcesses")
    @JacksonXmlProperty(localName = "numOfProcesses")
    public Integer getNumOfProcesses() {
        return numOfProcesses;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfProcesses")
    @JacksonXmlProperty(localName = "numOfProcesses")
    public void setNumOfProcesses(Integer numOfProcesses) {
        this.numOfProcesses = numOfProcesses;
    }

    @JsonProperty("processes")
    @JacksonXmlProperty(localName = "process")
    public List<Process> getProcesses() {
        return processes;
    }

    @JsonProperty("processes")
    @JacksonXmlProperty(localName = "process")
    public void setProcesses(List<Process> processes) {
        this.processes = processes;
    }

    /**
     * configuration status
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationStatus")
    @JacksonXmlProperty(localName = "configurationStatus")
    public ConfigurationState getConfigurationStatus() {
        return configurationStatus;
    }

    /**
     * configuration status
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationStatus")
    @JacksonXmlProperty(localName = "configurationStatus")
    public void setConfigurationStatus(ConfigurationState configurationStatus) {
        this.configurationStatus = configurationStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("surveyDate", surveyDate).append("path", path).append("name", name).append("maxProcesses", maxProcesses).append("_type", _type).append("state", state).append("numOfAgents", numOfAgents).append("agents", agents).append("numOfProcesses", numOfProcesses).append("processes", processes).append("configurationStatus", configurationStatus).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(maxProcesses).append(path).append(processes).append(surveyDate).append(configurationStatus).append(name).append(_type).append(numOfProcesses).append(state).append(numOfAgents).append(agents).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentCluster) == false) {
            return false;
        }
        AgentCluster rhs = ((AgentCluster) other);
        return new EqualsBuilder().append(maxProcesses, rhs.maxProcesses).append(path, rhs.path).append(processes, rhs.processes).append(surveyDate, rhs.surveyDate).append(configurationStatus, rhs.configurationStatus).append(name, rhs.name).append(_type, rhs._type).append(numOfProcesses, rhs.numOfProcesses).append(state, rhs.state).append(numOfAgents, rhs.numOfAgents).append(agents, rhs.agents).isEquals();
    }

}
