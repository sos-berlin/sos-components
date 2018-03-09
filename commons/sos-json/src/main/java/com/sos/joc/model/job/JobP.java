
package com.sos.joc.model.job;

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
 * job object (permanent part)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "surveyDate",
    "path",
    "isOrderJob",
    "isShellJob",
    "name",
    "title",
    "estimatedDuration",
    "processClass",
    "maxTasks",
    "locks",
    "usedInJobChains",
    "jobChains",
    "hasDescription",
    "configurationDate"
})
public class JobP {

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
    @JsonProperty("isOrderJob")
    @JacksonXmlProperty(localName = "isOrderJob")
    private Boolean isOrderJob;
    @JsonProperty("isShellJob")
    @JacksonXmlProperty(localName = "isShellJob")
    private Boolean isShellJob;
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
    @JsonProperty("estimatedDuration")
    @JacksonXmlProperty(localName = "estimatedDuration")
    private Integer estimatedDuration;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("processClass")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "processClass")
    private String processClass;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxTasks")
    @JacksonXmlProperty(localName = "maxTasks")
    private Integer maxTasks;
    /**
     * job locks (permanent)
     * <p>
     * 
     * 
     */
    @JsonProperty("locks")
    @JacksonXmlProperty(localName = "lock")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "locks")
    private List<LockUseP> locks = new ArrayList<LockUseP>();
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("usedInJobChains")
    @JacksonXmlProperty(localName = "usedInJobChains")
    private Integer usedInJobChains;
    /**
     * Only relevant for order jobs when called /jobs/p/... or job/p/...
     * 
     */
    @JsonProperty("jobChains")
    @JsonPropertyDescription("Only relevant for order jobs when called /jobs/p/... or job/p/...")
    @JacksonXmlProperty(localName = "jobChain")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "jobChains")
    private List<String> jobChains = new ArrayList<String>();
    @JsonProperty("hasDescription")
    @JacksonXmlProperty(localName = "hasDescription")
    private Boolean hasDescription;
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

    @JsonProperty("isOrderJob")
    @JacksonXmlProperty(localName = "isOrderJob")
    public Boolean getIsOrderJob() {
        return isOrderJob;
    }

    @JsonProperty("isOrderJob")
    @JacksonXmlProperty(localName = "isOrderJob")
    public void setIsOrderJob(Boolean isOrderJob) {
        this.isOrderJob = isOrderJob;
    }

    @JsonProperty("isShellJob")
    @JacksonXmlProperty(localName = "isShellJob")
    public Boolean getIsShellJob() {
        return isShellJob;
    }

    @JsonProperty("isShellJob")
    @JacksonXmlProperty(localName = "isShellJob")
    public void setIsShellJob(Boolean isShellJob) {
        this.isShellJob = isShellJob;
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
    @JsonProperty("estimatedDuration")
    @JacksonXmlProperty(localName = "estimatedDuration")
    public Integer getEstimatedDuration() {
        return estimatedDuration;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("estimatedDuration")
    @JacksonXmlProperty(localName = "estimatedDuration")
    public void setEstimatedDuration(Integer estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("processClass")
    @JacksonXmlProperty(localName = "processClass")
    public String getProcessClass() {
        return processClass;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("processClass")
    @JacksonXmlProperty(localName = "processClass")
    public void setProcessClass(String processClass) {
        this.processClass = processClass;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxTasks")
    @JacksonXmlProperty(localName = "maxTasks")
    public Integer getMaxTasks() {
        return maxTasks;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxTasks")
    @JacksonXmlProperty(localName = "maxTasks")
    public void setMaxTasks(Integer maxTasks) {
        this.maxTasks = maxTasks;
    }

    /**
     * job locks (permanent)
     * <p>
     * 
     * 
     */
    @JsonProperty("locks")
    @JacksonXmlProperty(localName = "lock")
    public List<LockUseP> getLocks() {
        return locks;
    }

    /**
     * job locks (permanent)
     * <p>
     * 
     * 
     */
    @JsonProperty("locks")
    @JacksonXmlProperty(localName = "lock")
    public void setLocks(List<LockUseP> locks) {
        this.locks = locks;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("usedInJobChains")
    @JacksonXmlProperty(localName = "usedInJobChains")
    public Integer getUsedInJobChains() {
        return usedInJobChains;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("usedInJobChains")
    @JacksonXmlProperty(localName = "usedInJobChains")
    public void setUsedInJobChains(Integer usedInJobChains) {
        this.usedInJobChains = usedInJobChains;
    }

    /**
     * Only relevant for order jobs when called /jobs/p/... or job/p/...
     * 
     */
    @JsonProperty("jobChains")
    @JacksonXmlProperty(localName = "jobChain")
    public List<String> getJobChains() {
        return jobChains;
    }

    /**
     * Only relevant for order jobs when called /jobs/p/... or job/p/...
     * 
     */
    @JsonProperty("jobChains")
    @JacksonXmlProperty(localName = "jobChain")
    public void setJobChains(List<String> jobChains) {
        this.jobChains = jobChains;
    }

    @JsonProperty("hasDescription")
    @JacksonXmlProperty(localName = "hasDescription")
    public Boolean getHasDescription() {
        return hasDescription;
    }

    @JsonProperty("hasDescription")
    @JacksonXmlProperty(localName = "hasDescription")
    public void setHasDescription(Boolean hasDescription) {
        this.hasDescription = hasDescription;
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
        return new ToStringBuilder(this).append("surveyDate", surveyDate).append("path", path).append("isOrderJob", isOrderJob).append("isShellJob", isShellJob).append("name", name).append("title", title).append("estimatedDuration", estimatedDuration).append("processClass", processClass).append("maxTasks", maxTasks).append("locks", locks).append("usedInJobChains", usedInJobChains).append("jobChains", jobChains).append("hasDescription", hasDescription).append("configurationDate", configurationDate).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(configurationDate).append(usedInJobChains).append(surveyDate).append(maxTasks).append(hasDescription).append(processClass).append(title).append(estimatedDuration).append(locks).append(isOrderJob).append(path).append(isShellJob).append(name).append(jobChains).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobP) == false) {
            return false;
        }
        JobP rhs = ((JobP) other);
        return new EqualsBuilder().append(configurationDate, rhs.configurationDate).append(usedInJobChains, rhs.usedInJobChains).append(surveyDate, rhs.surveyDate).append(maxTasks, rhs.maxTasks).append(hasDescription, rhs.hasDescription).append(processClass, rhs.processClass).append(title, rhs.title).append(estimatedDuration, rhs.estimatedDuration).append(locks, rhs.locks).append(isOrderJob, rhs.isOrderJob).append(path, rhs.path).append(isShellJob, rhs.isShellJob).append(name, rhs.name).append(jobChains, rhs.jobChains).isEquals();
    }

}
