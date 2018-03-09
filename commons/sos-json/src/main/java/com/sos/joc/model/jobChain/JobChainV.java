
package com.sos.joc.model.jobChain;

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
import com.sos.joc.model.order.OrderV;
import com.sos.joc.model.order.OrdersSummary;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * job chain (volatile part)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "surveyDate",
    "path",
    "name",
    "state",
    "numOfNodes",
    "nodes",
    "fileOrderSources",
    "blacklist",
    "numOfOrders",
    "configurationStatus",
    "ordersSummary"
})
public class JobChainV {

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
     * jobChain state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    private JobChainState state;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfNodes")
    @JacksonXmlProperty(localName = "numOfNodes")
    private Integer numOfNodes;
    @JsonProperty("nodes")
    @JacksonXmlProperty(localName = "node")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "nodes")
    private List<JobChainNodeV> nodes = new ArrayList<JobChainNodeV>();
    @JsonProperty("fileOrderSources")
    @JacksonXmlProperty(localName = "fileOrderSource")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "fileOrderSources")
    private List<FileWatchingNodeV> fileOrderSources = new ArrayList<FileWatchingNodeV>();
    @JsonProperty("blacklist")
    @JacksonXmlProperty(localName = "blacklist")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "blacklist")
    private List<OrderV> blacklist = new ArrayList<OrderV>();
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfOrders")
    @JacksonXmlProperty(localName = "numOfOrders")
    private Integer numOfOrders;
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
     * job chain order summary
     * <p>
     * only relevant for order jobs and is empty if job's order queue is empty
     * 
     */
    @JsonProperty("ordersSummary")
    @JsonPropertyDescription("only relevant for order jobs and is empty if job's order queue is empty")
    @JacksonXmlProperty(localName = "ordersSummary")
    private OrdersSummary ordersSummary;

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
     * jobChain state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    public JobChainState getState() {
        return state;
    }

    /**
     * jobChain state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    public void setState(JobChainState state) {
        this.state = state;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfNodes")
    @JacksonXmlProperty(localName = "numOfNodes")
    public Integer getNumOfNodes() {
        return numOfNodes;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfNodes")
    @JacksonXmlProperty(localName = "numOfNodes")
    public void setNumOfNodes(Integer numOfNodes) {
        this.numOfNodes = numOfNodes;
    }

    @JsonProperty("nodes")
    @JacksonXmlProperty(localName = "node")
    public List<JobChainNodeV> getNodes() {
        return nodes;
    }

    @JsonProperty("nodes")
    @JacksonXmlProperty(localName = "node")
    public void setNodes(List<JobChainNodeV> nodes) {
        this.nodes = nodes;
    }

    @JsonProperty("fileOrderSources")
    @JacksonXmlProperty(localName = "fileOrderSource")
    public List<FileWatchingNodeV> getFileOrderSources() {
        return fileOrderSources;
    }

    @JsonProperty("fileOrderSources")
    @JacksonXmlProperty(localName = "fileOrderSource")
    public void setFileOrderSources(List<FileWatchingNodeV> fileOrderSources) {
        this.fileOrderSources = fileOrderSources;
    }

    @JsonProperty("blacklist")
    @JacksonXmlProperty(localName = "blacklist")
    public List<OrderV> getBlacklist() {
        return blacklist;
    }

    @JsonProperty("blacklist")
    @JacksonXmlProperty(localName = "blacklist")
    public void setBlacklist(List<OrderV> blacklist) {
        this.blacklist = blacklist;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfOrders")
    @JacksonXmlProperty(localName = "numOfOrders")
    public Integer getNumOfOrders() {
        return numOfOrders;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfOrders")
    @JacksonXmlProperty(localName = "numOfOrders")
    public void setNumOfOrders(Integer numOfOrders) {
        this.numOfOrders = numOfOrders;
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

    /**
     * job chain order summary
     * <p>
     * only relevant for order jobs and is empty if job's order queue is empty
     * 
     */
    @JsonProperty("ordersSummary")
    @JacksonXmlProperty(localName = "ordersSummary")
    public OrdersSummary getOrdersSummary() {
        return ordersSummary;
    }

    /**
     * job chain order summary
     * <p>
     * only relevant for order jobs and is empty if job's order queue is empty
     * 
     */
    @JsonProperty("ordersSummary")
    @JacksonXmlProperty(localName = "ordersSummary")
    public void setOrdersSummary(OrdersSummary ordersSummary) {
        this.ordersSummary = ordersSummary;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("surveyDate", surveyDate).append("path", path).append("name", name).append("state", state).append("numOfNodes", numOfNodes).append("nodes", nodes).append("fileOrderSources", fileOrderSources).append("blacklist", blacklist).append("numOfOrders", numOfOrders).append("configurationStatus", configurationStatus).append("ordersSummary", ordersSummary).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(fileOrderSources).append(nodes).append(surveyDate).append(configurationStatus).append(numOfOrders).append(name).append(blacklist).append(state).append(numOfNodes).append(ordersSummary).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobChainV) == false) {
            return false;
        }
        JobChainV rhs = ((JobChainV) other);
        return new EqualsBuilder().append(path, rhs.path).append(fileOrderSources, rhs.fileOrderSources).append(nodes, rhs.nodes).append(surveyDate, rhs.surveyDate).append(configurationStatus, rhs.configurationStatus).append(numOfOrders, rhs.numOfOrders).append(name, rhs.name).append(blacklist, rhs.blacklist).append(state, rhs.state).append(numOfNodes, rhs.numOfNodes).append(ordersSummary, rhs.ordersSummary).isEquals();
    }

}
