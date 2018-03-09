
package com.sos.joc.model.yade;

import java.util.ArrayList;
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
 * yade filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "transferIds",
    "compact",
    "regex",
    "profiles",
    "mandator",
    "states",
    "operations",
    "dateFrom",
    "dateTo",
    "timeZone",
    "limit",
    "hasIntervention",
    "isIntervention",
    "sources",
    "targets",
    "sourceFiles",
    "targetFiles"
})
public class TransferFilter {

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("transferIds")
    @JacksonXmlProperty(localName = "transferId")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "transferIds")
    private List<Long> transferIds = new ArrayList<Long>();
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
    @JsonProperty("profiles")
    @JacksonXmlProperty(localName = "profile")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "profiles")
    private List<String> profiles = new ArrayList<String>();
    @JsonProperty("mandator")
    @JacksonXmlProperty(localName = "mandator")
    private String mandator;
    @JsonProperty("states")
    @JacksonXmlProperty(localName = "state")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "states")
    private List<TransferStateText> states = new ArrayList<TransferStateText>();
    @JsonProperty("operations")
    @JacksonXmlProperty(localName = "operation")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "operations")
    private List<Operation> operations = new ArrayList<Operation>();
    @JsonProperty("dateFrom")
    @JacksonXmlProperty(localName = "dateFrom")
    private String dateFrom;
    @JsonProperty("dateTo")
    @JacksonXmlProperty(localName = "dateTo")
    private String dateTo;
    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    @JacksonXmlProperty(localName = "timeZone")
    private String timeZone;
    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JsonPropertyDescription("only for db history urls to restrict the number of responsed records; -1=unlimited")
    @JacksonXmlProperty(localName = "limit")
    private Integer limit = 10000;
    @JsonProperty("hasIntervention")
    @JacksonXmlProperty(localName = "hasIntervention")
    private Boolean hasIntervention;
    @JsonProperty("isIntervention")
    @JacksonXmlProperty(localName = "isIntervention")
    private Boolean isIntervention;
    @JsonProperty("sources")
    @JacksonXmlProperty(localName = "source")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "sources")
    private List<ProtocolFragment> sources = new ArrayList<ProtocolFragment>();
    @JsonProperty("targets")
    @JacksonXmlProperty(localName = "target")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "targets")
    private List<ProtocolFragment> targets = new ArrayList<ProtocolFragment>();
    @JsonProperty("sourceFiles")
    @JacksonXmlProperty(localName = "sourceFile")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "sourceFiles")
    private List<String> sourceFiles = new ArrayList<String>();
    @JsonProperty("targetFiles")
    @JacksonXmlProperty(localName = "targetFile")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "targetFiles")
    private List<String> targetFiles = new ArrayList<String>();

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    @JsonProperty("transferIds")
    @JacksonXmlProperty(localName = "transferId")
    public List<Long> getTransferIds() {
        return transferIds;
    }

    @JsonProperty("transferIds")
    @JacksonXmlProperty(localName = "transferId")
    public void setTransferIds(List<Long> transferIds) {
        this.transferIds = transferIds;
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

    @JsonProperty("profiles")
    @JacksonXmlProperty(localName = "profile")
    public List<String> getProfiles() {
        return profiles;
    }

    @JsonProperty("profiles")
    @JacksonXmlProperty(localName = "profile")
    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }

    @JsonProperty("mandator")
    @JacksonXmlProperty(localName = "mandator")
    public String getMandator() {
        return mandator;
    }

    @JsonProperty("mandator")
    @JacksonXmlProperty(localName = "mandator")
    public void setMandator(String mandator) {
        this.mandator = mandator;
    }

    @JsonProperty("states")
    @JacksonXmlProperty(localName = "state")
    public List<TransferStateText> getStates() {
        return states;
    }

    @JsonProperty("states")
    @JacksonXmlProperty(localName = "state")
    public void setStates(List<TransferStateText> states) {
        this.states = states;
    }

    @JsonProperty("operations")
    @JacksonXmlProperty(localName = "operation")
    public List<Operation> getOperations() {
        return operations;
    }

    @JsonProperty("operations")
    @JacksonXmlProperty(localName = "operation")
    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    @JsonProperty("dateFrom")
    @JacksonXmlProperty(localName = "dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    @JsonProperty("dateFrom")
    @JacksonXmlProperty(localName = "dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    @JsonProperty("dateTo")
    @JacksonXmlProperty(localName = "dateTo")
    public String getDateTo() {
        return dateTo;
    }

    @JsonProperty("dateTo")
    @JacksonXmlProperty(localName = "dateTo")
    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JacksonXmlProperty(localName = "timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JacksonXmlProperty(localName = "timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JacksonXmlProperty(localName = "limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JacksonXmlProperty(localName = "limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @JsonProperty("hasIntervention")
    @JacksonXmlProperty(localName = "hasIntervention")
    public Boolean getHasIntervention() {
        return hasIntervention;
    }

    @JsonProperty("hasIntervention")
    @JacksonXmlProperty(localName = "hasIntervention")
    public void setHasIntervention(Boolean hasIntervention) {
        this.hasIntervention = hasIntervention;
    }

    @JsonProperty("isIntervention")
    @JacksonXmlProperty(localName = "isIntervention")
    public Boolean getIsIntervention() {
        return isIntervention;
    }

    @JsonProperty("isIntervention")
    @JacksonXmlProperty(localName = "isIntervention")
    public void setIsIntervention(Boolean isIntervention) {
        this.isIntervention = isIntervention;
    }

    @JsonProperty("sources")
    @JacksonXmlProperty(localName = "source")
    public List<ProtocolFragment> getSources() {
        return sources;
    }

    @JsonProperty("sources")
    @JacksonXmlProperty(localName = "source")
    public void setSources(List<ProtocolFragment> sources) {
        this.sources = sources;
    }

    @JsonProperty("targets")
    @JacksonXmlProperty(localName = "target")
    public List<ProtocolFragment> getTargets() {
        return targets;
    }

    @JsonProperty("targets")
    @JacksonXmlProperty(localName = "target")
    public void setTargets(List<ProtocolFragment> targets) {
        this.targets = targets;
    }

    @JsonProperty("sourceFiles")
    @JacksonXmlProperty(localName = "sourceFile")
    public List<String> getSourceFiles() {
        return sourceFiles;
    }

    @JsonProperty("sourceFiles")
    @JacksonXmlProperty(localName = "sourceFile")
    public void setSourceFiles(List<String> sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    @JsonProperty("targetFiles")
    @JacksonXmlProperty(localName = "targetFile")
    public List<String> getTargetFiles() {
        return targetFiles;
    }

    @JsonProperty("targetFiles")
    @JacksonXmlProperty(localName = "targetFile")
    public void setTargetFiles(List<String> targetFiles) {
        this.targetFiles = targetFiles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("transferIds", transferIds).append("compact", compact).append("regex", regex).append("profiles", profiles).append("mandator", mandator).append("states", states).append("operations", operations).append("dateFrom", dateFrom).append("dateTo", dateTo).append("timeZone", timeZone).append("limit", limit).append("hasIntervention", hasIntervention).append("isIntervention", isIntervention).append("sources", sources).append("targets", targets).append("sourceFiles", sourceFiles).append("targetFiles", targetFiles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(mandator).append(compact).append(sources).append(profiles).append(timeZone).append(isIntervention).append(dateFrom).append(targets).append(transferIds).append(hasIntervention).append(states).append(regex).append(operations).append(sourceFiles).append(dateTo).append(limit).append(jobschedulerId).append(targetFiles).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TransferFilter) == false) {
            return false;
        }
        TransferFilter rhs = ((TransferFilter) other);
        return new EqualsBuilder().append(mandator, rhs.mandator).append(compact, rhs.compact).append(sources, rhs.sources).append(profiles, rhs.profiles).append(timeZone, rhs.timeZone).append(isIntervention, rhs.isIntervention).append(dateFrom, rhs.dateFrom).append(targets, rhs.targets).append(transferIds, rhs.transferIds).append(hasIntervention, rhs.hasIntervention).append(states, rhs.states).append(regex, rhs.regex).append(operations, rhs.operations).append(sourceFiles, rhs.sourceFiles).append(dateTo, rhs.dateTo).append(limit, rhs.limit).append(jobschedulerId, rhs.jobschedulerId).append(targetFiles, rhs.targetFiles).isEquals();
    }

}
