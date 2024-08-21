
package com.sos.sign.model.job;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.job.AdmissionTimeScheme;
import com.sos.inventory.model.job.Environment;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * job
 * <p>
 * returnCodeMeaning is deprecated: moved to ShellScriptExecutable
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "agentPath",
    "subagentSelectionId",
    "subagentSelectionIdExpr",
    "executable",
    "admissionTimeScheme",
    "skipIfNoAdmissionStartForOrderDay",
    "returnCodeMeaning",
    "processLimit",
    "timeout",
    "sigkillDelay",
    "failOnErrWritten",
    "defaultArguments",
    "jobResourcePaths",
    "isNotRestartable"
})
public class Job {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentPath")
    @JsonAlias({
        "agentId",
        "agentName"
    })
    private String agentPath;
    @JsonProperty("subagentSelectionId")
    @JsonAlias({
        "subagentClusterId"
    })
    private String subagentSelectionId;
    @JsonProperty("subagentSelectionIdExpr")
    @JsonAlias({
        "subagentClusterIdExpr"
    })
    private String subagentSelectionIdExpr;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("executable")
    private Executable executable;
    /**
     * admission time scheme
     * <p>
     * 
     * 
     */
    @JsonProperty("admissionTimeScheme")
    private AdmissionTimeScheme admissionTimeScheme;
    @JsonProperty("skipIfNoAdmissionStartForOrderDay")
    @JsonAlias({
        "skipIfNoAdmissionForOrderDay"
    })
    private Boolean skipIfNoAdmissionStartForOrderDay = false;
    /**
     * job return code meaning
     * <p>
     * 
     * 
     */
    @JsonProperty("returnCodeMeaning")
    private JobReturnCode returnCodeMeaning;
    @JsonProperty("processLimit")
    @JsonAlias({
        "parallelism",
        "taskLimit"
    })
    private Integer processLimit = 1;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("timeout")
    private Integer timeout;
    /**
     * in seconds
     * 
     */
    @JsonProperty("sigkillDelay")
    @JsonPropertyDescription("in seconds")
    @JsonAlias({
        "graceTimeout"
    })
    private Integer sigkillDelay = 15;
    @JsonProperty("failOnErrWritten")
    private Boolean failOnErrWritten = false;
    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("defaultArguments")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Environment defaultArguments;
    @JsonProperty("jobResourcePaths")
    @JsonAlias({
        "jobResourceNames"
    })
    private List<String> jobResourcePaths = null;
    /**
     * default -> false
     * 
     */
    @JsonProperty("isNotRestartable")
    @JsonPropertyDescription("default -> false")
    private Boolean isNotRestartable;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Job() {
    }

    /**
     * 
     * @param subagentSelectionId
     * @param sigkillDelay
     * @param failOnErrWritten
     * @param executable
     * @param timeout
     * @param admissionTimeScheme
     * @param agentPath
     * @param subagentSelectionIdExpr
     * @param returnCodeMeaning
     * @param processLimit
     * @param defaultArguments
     * @param jobResourcePaths
     * @param skipIfNoAdmissionStartForOrderDay
     * @param isNotRestartable
     */
    public Job(String agentPath, String subagentSelectionId, String subagentSelectionIdExpr, Executable executable, AdmissionTimeScheme admissionTimeScheme, Boolean skipIfNoAdmissionStartForOrderDay, JobReturnCode returnCodeMeaning, Integer processLimit, Integer timeout, Integer sigkillDelay, Boolean failOnErrWritten, Environment defaultArguments, List<String> jobResourcePaths, Boolean isNotRestartable) {
        super();
        this.agentPath = agentPath;
        this.subagentSelectionId = subagentSelectionId;
        this.subagentSelectionIdExpr = subagentSelectionIdExpr;
        this.executable = executable;
        this.admissionTimeScheme = admissionTimeScheme;
        this.skipIfNoAdmissionStartForOrderDay = skipIfNoAdmissionStartForOrderDay;
        this.returnCodeMeaning = returnCodeMeaning;
        this.processLimit = processLimit;
        this.timeout = timeout;
        this.sigkillDelay = sigkillDelay;
        this.failOnErrWritten = failOnErrWritten;
        this.defaultArguments = defaultArguments;
        this.jobResourcePaths = jobResourcePaths;
        this.isNotRestartable = isNotRestartable;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentPath")
    public String getAgentPath() {
        return agentPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentPath")
    public void setAgentPath(String agentPath) {
        this.agentPath = agentPath;
    }

    @JsonProperty("subagentSelectionId")
    public String getSubagentSelectionId() {
        return subagentSelectionId;
    }

    @JsonProperty("subagentSelectionId")
    public void setSubagentSelectionId(String subagentSelectionId) {
        this.subagentSelectionId = subagentSelectionId;
    }

    @JsonProperty("subagentSelectionIdExpr")
    public String getSubagentSelectionIdExpr() {
        return subagentSelectionIdExpr;
    }

    @JsonProperty("subagentSelectionIdExpr")
    public void setSubagentSelectionIdExpr(String subagentSelectionIdExpr) {
        this.subagentSelectionIdExpr = subagentSelectionIdExpr;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("executable")
    public Executable getExecutable() {
        return executable;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("executable")
    public void setExecutable(Executable executable) {
        this.executable = executable;
    }

    /**
     * admission time scheme
     * <p>
     * 
     * 
     */
    @JsonProperty("admissionTimeScheme")
    public AdmissionTimeScheme getAdmissionTimeScheme() {
        return admissionTimeScheme;
    }

    /**
     * admission time scheme
     * <p>
     * 
     * 
     */
    @JsonProperty("admissionTimeScheme")
    public void setAdmissionTimeScheme(AdmissionTimeScheme admissionTimeScheme) {
        this.admissionTimeScheme = admissionTimeScheme;
    }

    @JsonProperty("skipIfNoAdmissionStartForOrderDay")
    public Boolean getSkipIfNoAdmissionStartForOrderDay() {
        return skipIfNoAdmissionStartForOrderDay;
    }

    @JsonProperty("skipIfNoAdmissionStartForOrderDay")
    public void setSkipIfNoAdmissionStartForOrderDay(Boolean skipIfNoAdmissionStartForOrderDay) {
        this.skipIfNoAdmissionStartForOrderDay = skipIfNoAdmissionStartForOrderDay;
    }

    /**
     * job return code meaning
     * <p>
     * 
     * 
     */
    @JsonProperty("returnCodeMeaning")
    public JobReturnCode getReturnCodeMeaning() {
        return returnCodeMeaning;
    }

    /**
     * job return code meaning
     * <p>
     * 
     * 
     */
    @JsonProperty("returnCodeMeaning")
    public void setReturnCodeMeaning(JobReturnCode returnCodeMeaning) {
        this.returnCodeMeaning = returnCodeMeaning;
    }

    @JsonProperty("processLimit")
    public Integer getProcessLimit() {
        return processLimit;
    }

    @JsonProperty("processLimit")
    public void setProcessLimit(Integer processLimit) {
        this.processLimit = processLimit;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("timeout")
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("timeout")
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * in seconds
     * 
     */
    @JsonProperty("sigkillDelay")
    public Integer getSigkillDelay() {
        return sigkillDelay;
    }

    /**
     * in seconds
     * 
     */
    @JsonProperty("sigkillDelay")
    public void setSigkillDelay(Integer sigkillDelay) {
        this.sigkillDelay = sigkillDelay;
    }

    @JsonProperty("failOnErrWritten")
    public Boolean getFailOnErrWritten() {
        return failOnErrWritten;
    }

    @JsonProperty("failOnErrWritten")
    public void setFailOnErrWritten(Boolean failOnErrWritten) {
        this.failOnErrWritten = failOnErrWritten;
    }

    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("defaultArguments")
    public Environment getDefaultArguments() {
        return defaultArguments;
    }

    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("defaultArguments")
    public void setDefaultArguments(Environment defaultArguments) {
        this.defaultArguments = defaultArguments;
    }

    @JsonProperty("jobResourcePaths")
    public List<String> getJobResourcePaths() {
        return jobResourcePaths;
    }

    @JsonProperty("jobResourcePaths")
    public void setJobResourcePaths(List<String> jobResourcePaths) {
        this.jobResourcePaths = jobResourcePaths;
    }

    /**
     * default -> false
     * 
     */
    @JsonProperty("isNotRestartable")
    public Boolean getIsNotRestartable() {
        return isNotRestartable;
    }

    /**
     * default -> false
     * 
     */
    @JsonProperty("isNotRestartable")
    public void setIsNotRestartable(Boolean isNotRestartable) {
        this.isNotRestartable = isNotRestartable;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentPath", agentPath).append("subagentSelectionId", subagentSelectionId).append("subagentSelectionIdExpr", subagentSelectionIdExpr).append("executable", executable).append("admissionTimeScheme", admissionTimeScheme).append("skipIfNoAdmissionStartForOrderDay", skipIfNoAdmissionStartForOrderDay).append("returnCodeMeaning", returnCodeMeaning).append("processLimit", processLimit).append("timeout", timeout).append("sigkillDelay", sigkillDelay).append("failOnErrWritten", failOnErrWritten).append("defaultArguments", defaultArguments).append("jobResourcePaths", jobResourcePaths).append("isNotRestartable", isNotRestartable).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(subagentSelectionId).append(sigkillDelay).append(failOnErrWritten).append(executable).append(timeout).append(admissionTimeScheme).append(agentPath).append(subagentSelectionIdExpr).append(returnCodeMeaning).append(processLimit).append(defaultArguments).append(jobResourcePaths).append(skipIfNoAdmissionStartForOrderDay).append(isNotRestartable).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Job) == false) {
            return false;
        }
        Job rhs = ((Job) other);
        return new EqualsBuilder().append(subagentSelectionId, rhs.subagentSelectionId).append(sigkillDelay, rhs.sigkillDelay).append(failOnErrWritten, rhs.failOnErrWritten).append(executable, rhs.executable).append(timeout, rhs.timeout).append(admissionTimeScheme, rhs.admissionTimeScheme).append(agentPath, rhs.agentPath).append(subagentSelectionIdExpr, rhs.subagentSelectionIdExpr).append(returnCodeMeaning, rhs.returnCodeMeaning).append(processLimit, rhs.processLimit).append(defaultArguments, rhs.defaultArguments).append(jobResourcePaths, rhs.jobResourcePaths).append(skipIfNoAdmissionStartForOrderDay, rhs.skipIfNoAdmissionStartForOrderDay).append(isNotRestartable, rhs.isNotRestartable).isEquals();
    }

}
