
package com.sos.joc.model.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    "documentation",
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
    private Date surveyDate;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String path;
    @JsonProperty("isOrderJob")
    private Boolean isOrderJob;
    @JsonProperty("isShellJob")
    private Boolean isShellJob;
    @JsonProperty("name")
    private String name;
    @JsonProperty("title")
    private String title;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("estimatedDuration")
    private Integer estimatedDuration;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("processClass")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String processClass;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxTasks")
    private Integer maxTasks;
    /**
     * job locks (permanent)
     * <p>
     * 
     * 
     */
    @JsonProperty("locks")
    private List<LockUseP> locks = new ArrayList<LockUseP>();
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("usedInJobChains")
    private Integer usedInJobChains;
    /**
     * Only relevant for order jobs when called /jobs/p/... or job/p/...
     * 
     */
    @JsonProperty("jobChains")
    @JsonPropertyDescription("Only relevant for order jobs when called /jobs/p/... or job/p/...")
    private List<String> jobChains = new ArrayList<String>();
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("documentation")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String documentation;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("configurationDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date configurationDate;

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("surveyDate")
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
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty("isOrderJob")
    public Boolean getIsOrderJob() {
        return isOrderJob;
    }

    @JsonProperty("isOrderJob")
    public void setIsOrderJob(Boolean isOrderJob) {
        this.isOrderJob = isOrderJob;
    }

    @JsonProperty("isShellJob")
    public Boolean getIsShellJob() {
        return isShellJob;
    }

    @JsonProperty("isShellJob")
    public void setIsShellJob(Boolean isShellJob) {
        this.isShellJob = isShellJob;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
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
    public void setEstimatedDuration(Integer estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("processClass")
    public String getProcessClass() {
        return processClass;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("processClass")
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
    public void setUsedInJobChains(Integer usedInJobChains) {
        this.usedInJobChains = usedInJobChains;
    }

    /**
     * Only relevant for order jobs when called /jobs/p/... or job/p/...
     * 
     */
    @JsonProperty("jobChains")
    public List<String> getJobChains() {
        return jobChains;
    }

    /**
     * Only relevant for order jobs when called /jobs/p/... or job/p/...
     * 
     */
    @JsonProperty("jobChains")
    public void setJobChains(List<String> jobChains) {
        this.jobChains = jobChains;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("documentation")
    public String getDocumentation() {
        return documentation;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("documentation")
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("configurationDate")
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
    public void setConfigurationDate(Date configurationDate) {
        this.configurationDate = configurationDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("surveyDate", surveyDate).append("path", path).append("isOrderJob", isOrderJob).append("isShellJob", isShellJob).append("name", name).append("title", title).append("estimatedDuration", estimatedDuration).append("processClass", processClass).append("maxTasks", maxTasks).append("locks", locks).append("usedInJobChains", usedInJobChains).append("jobChains", jobChains).append("documentation", documentation).append("configurationDate", configurationDate).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(configurationDate).append(usedInJobChains).append(surveyDate).append(maxTasks).append(documentation).append(processClass).append(title).append(estimatedDuration).append(locks).append(isOrderJob).append(path).append(isShellJob).append(name).append(jobChains).toHashCode();
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
        return new EqualsBuilder().append(configurationDate, rhs.configurationDate).append(usedInJobChains, rhs.usedInJobChains).append(surveyDate, rhs.surveyDate).append(maxTasks, rhs.maxTasks).append(documentation, rhs.documentation).append(processClass, rhs.processClass).append(title, rhs.title).append(estimatedDuration, rhs.estimatedDuration).append(locks, rhs.locks).append(isOrderJob, rhs.isOrderJob).append(path, rhs.path).append(isShellJob, rhs.isShellJob).append(name, rhs.name).append(jobChains, rhs.jobChains).isEquals();
    }

}
