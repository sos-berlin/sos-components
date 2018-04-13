
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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * job chain (permanent part)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "surveyDate",
    "path",
    "name",
    "title",
    "maxOrders",
    "distributed",
    "processClass",
    "fileWatchingProcessClass",
    "numOfNodes",
    "nodes",
    "fileOrderSources",
    "endNodes",
    "configurationDate"
})
public class JobChainP {

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
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
    @JsonProperty("title")
    @JacksonXmlProperty(localName = "title")
    private String title;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxOrders")
    @JacksonXmlProperty(localName = "maxOrders")
    private Integer maxOrders;
    @JsonProperty("distributed")
    @JacksonXmlProperty(localName = "distributed")
    private Boolean distributed;
    @JsonProperty("processClass")
    @JacksonXmlProperty(localName = "processClass")
    private String processClass;
    @JsonProperty("fileWatchingProcessClass")
    @JacksonXmlProperty(localName = "fileWatchingProcessClass")
    private String fileWatchingProcessClass;
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
    private List<JobChainNodeP> nodes = new ArrayList<JobChainNodeP>();
    @JsonProperty("fileOrderSources")
    @JacksonXmlProperty(localName = "fileOrderSource")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "fileOrderSources")
    private List<FileWatchingNodeP> fileOrderSources = new ArrayList<FileWatchingNodeP>();
    /**
     * real end nodes or file sink nodes
     * 
     */
    @JsonProperty("endNodes")
    @JsonPropertyDescription("real end nodes or file sink nodes")
    @JacksonXmlProperty(localName = "endNode")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "endNodes")
    private List<EndNode> endNodes = new ArrayList<EndNode>();
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("configurationDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "configurationDate")
    private Date configurationDate;

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("surveyDate")
    @JacksonXmlProperty(localName = "surveyDate")
    public Date getSurveyDate() {
        return surveyDate;
    }

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
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

    @JsonProperty("title")
    @JacksonXmlProperty(localName = "title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    @JacksonXmlProperty(localName = "title")
    public void setTitle(String title) {
        this.title = title;
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

    @JsonProperty("distributed")
    @JacksonXmlProperty(localName = "distributed")
    public Boolean getDistributed() {
        return distributed;
    }

    @JsonProperty("distributed")
    @JacksonXmlProperty(localName = "distributed")
    public void setDistributed(Boolean distributed) {
        this.distributed = distributed;
    }

    @JsonProperty("processClass")
    @JacksonXmlProperty(localName = "processClass")
    public String getProcessClass() {
        return processClass;
    }

    @JsonProperty("processClass")
    @JacksonXmlProperty(localName = "processClass")
    public void setProcessClass(String processClass) {
        this.processClass = processClass;
    }

    @JsonProperty("fileWatchingProcessClass")
    @JacksonXmlProperty(localName = "fileWatchingProcessClass")
    public String getFileWatchingProcessClass() {
        return fileWatchingProcessClass;
    }

    @JsonProperty("fileWatchingProcessClass")
    @JacksonXmlProperty(localName = "fileWatchingProcessClass")
    public void setFileWatchingProcessClass(String fileWatchingProcessClass) {
        this.fileWatchingProcessClass = fileWatchingProcessClass;
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
    public List<JobChainNodeP> getNodes() {
        return nodes;
    }

    @JsonProperty("nodes")
    @JacksonXmlProperty(localName = "node")
    public void setNodes(List<JobChainNodeP> nodes) {
        this.nodes = nodes;
    }

    @JsonProperty("fileOrderSources")
    @JacksonXmlProperty(localName = "fileOrderSource")
    public List<FileWatchingNodeP> getFileOrderSources() {
        return fileOrderSources;
    }

    @JsonProperty("fileOrderSources")
    @JacksonXmlProperty(localName = "fileOrderSource")
    public void setFileOrderSources(List<FileWatchingNodeP> fileOrderSources) {
        this.fileOrderSources = fileOrderSources;
    }

    /**
     * real end nodes or file sink nodes
     * 
     */
    @JsonProperty("endNodes")
    @JacksonXmlProperty(localName = "endNode")
    public List<EndNode> getEndNodes() {
        return endNodes;
    }

    /**
     * real end nodes or file sink nodes
     * 
     */
    @JsonProperty("endNodes")
    @JacksonXmlProperty(localName = "endNode")
    public void setEndNodes(List<EndNode> endNodes) {
        this.endNodes = endNodes;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("configurationDate")
    @JacksonXmlProperty(localName = "configurationDate")
    public Date getConfigurationDate() {
        return configurationDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("configurationDate")
    @JacksonXmlProperty(localName = "configurationDate")
    public void setConfigurationDate(Date configurationDate) {
        this.configurationDate = configurationDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("surveyDate", surveyDate).append("path", path).append("name", name).append("title", title).append("maxOrders", maxOrders).append("distributed", distributed).append("processClass", processClass).append("fileWatchingProcessClass", fileWatchingProcessClass).append("numOfNodes", numOfNodes).append("nodes", nodes).append("fileOrderSources", fileOrderSources).append("endNodes", endNodes).append("configurationDate", configurationDate).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(configurationDate).append(surveyDate).append(distributed).append(processClass).append(title).append(maxOrders).append(path).append(endNodes).append(fileOrderSources).append(nodes).append(name).append(numOfNodes).append(fileWatchingProcessClass).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobChainP) == false) {
            return false;
        }
        JobChainP rhs = ((JobChainP) other);
        return new EqualsBuilder().append(configurationDate, rhs.configurationDate).append(surveyDate, rhs.surveyDate).append(distributed, rhs.distributed).append(processClass, rhs.processClass).append(title, rhs.title).append(maxOrders, rhs.maxOrders).append(path, rhs.path).append(endNodes, rhs.endNodes).append(fileOrderSources, rhs.fileOrderSources).append(nodes, rhs.nodes).append(name, rhs.name).append(numOfNodes, rhs.numOfNodes).append(fileWatchingProcessClass, rhs.fileWatchingProcessClass).isEquals();
    }

}
