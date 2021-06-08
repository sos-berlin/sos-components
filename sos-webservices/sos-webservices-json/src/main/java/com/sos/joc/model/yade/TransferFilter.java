
package com.sos.joc.model.yade;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    "controllerId",
    "transferIds",
    "compact",
    "regex",
    "profiles",
    "states",
    "operations",
    "dateFrom",
    "dateTo",
    "timeZone",
    "limit",
    "sources",
    "targets",
    "sourceFile",
    "targetFile",
    "sourceFiles",
    "targetFiles"
})
public class TransferFilter {

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("transferIds")
    private List<Long> transferIds = new ArrayList<Long>();
    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JsonPropertyDescription("controls if the object's data is compact or detailed")
    private Boolean compact = false;
    /**
     * filter with regex
     * <p>
     * regular expression to filter Controller objects by matching the path
     * 
     */
    @JsonProperty("regex")
    @JsonPropertyDescription("regular expression to filter Controller objects by matching the path")
    private String regex;
    @JsonProperty("profiles")
    private List<String> profiles = new ArrayList<String>();
    @JsonProperty("states")
    private List<TransferStateText> states = new ArrayList<TransferStateText>();
    @JsonProperty("operations")
    private List<Operation> operations = new ArrayList<Operation>();
    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFrom")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String dateFrom;
    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateTo")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String dateTo;
    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timeZone;
    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JsonPropertyDescription("only for db history urls to restrict the number of responsed records; -1=unlimited")
    private Integer limit = 10000;
    @JsonProperty("sources")
    private List<ProtocolFragment> sources = new ArrayList<ProtocolFragment>();
    @JsonProperty("targets")
    private List<ProtocolFragment> targets = new ArrayList<ProtocolFragment>();
    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * 
     */
    @JsonProperty("sourceFile")
    @JsonPropertyDescription("pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character")
    private String sourceFile;
    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * 
     */
    @JsonProperty("targetFile")
    @JsonPropertyDescription("pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character")
    private String targetFile;
    @JsonProperty("sourceFiles")
    private List<String> sourceFiles = new ArrayList<String>();
    @JsonProperty("targetFiles")
    private List<String> targetFiles = new ArrayList<String>();

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @JsonProperty("transferIds")
    public List<Long> getTransferIds() {
        return transferIds;
    }

    @JsonProperty("transferIds")
    public void setTransferIds(List<Long> transferIds) {
        this.transferIds = transferIds;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public Boolean getCompact() {
        return compact;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public void setCompact(Boolean compact) {
        this.compact = compact;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter Controller objects by matching the path
     * 
     */
    @JsonProperty("regex")
    public String getRegex() {
        return regex;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter Controller objects by matching the path
     * 
     */
    @JsonProperty("regex")
    public void setRegex(String regex) {
        this.regex = regex;
    }

    @JsonProperty("profiles")
    public List<String> getProfiles() {
        return profiles;
    }

    @JsonProperty("profiles")
    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }

    @JsonProperty("states")
    public List<TransferStateText> getStates() {
        return states;
    }

    @JsonProperty("states")
    public void setStates(List<TransferStateText> states) {
        this.states = states;
    }

    @JsonProperty("operations")
    public List<Operation> getOperations() {
        return operations;
    }

    @JsonProperty("operations")
    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateTo")
    public String getDateTo() {
        return dateTo;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateTo")
    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @JsonProperty("sources")
    public List<ProtocolFragment> getSources() {
        return sources;
    }

    @JsonProperty("sources")
    public void setSources(List<ProtocolFragment> sources) {
        this.sources = sources;
    }

    @JsonProperty("targets")
    public List<ProtocolFragment> getTargets() {
        return targets;
    }

    @JsonProperty("targets")
    public void setTargets(List<ProtocolFragment> targets) {
        this.targets = targets;
    }

    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * 
     */
    @JsonProperty("sourceFile")
    public String getSourceFile() {
        return sourceFile;
    }

    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * 
     */
    @JsonProperty("sourceFile")
    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * 
     */
    @JsonProperty("targetFile")
    public String getTargetFile() {
        return targetFile;
    }

    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * 
     */
    @JsonProperty("targetFile")
    public void setTargetFile(String targetFile) {
        this.targetFile = targetFile;
    }

    @JsonProperty("sourceFiles")
    public List<String> getSourceFiles() {
        return sourceFiles;
    }

    @JsonProperty("sourceFiles")
    public void setSourceFiles(List<String> sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    @JsonProperty("targetFiles")
    public List<String> getTargetFiles() {
        return targetFiles;
    }

    @JsonProperty("targetFiles")
    public void setTargetFiles(List<String> targetFiles) {
        this.targetFiles = targetFiles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("transferIds", transferIds).append("compact", compact).append("regex", regex).append("profiles", profiles).append("states", states).append("operations", operations).append("dateFrom", dateFrom).append("dateTo", dateTo).append("timeZone", timeZone).append("limit", limit).append("sources", sources).append("targets", targets).append("sourceFile", sourceFile).append("targetFile", targetFile).append("sourceFiles", sourceFiles).append("targetFiles", targetFiles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(compact).append(sources).append(profiles).append(timeZone).append(dateFrom).append(targets).append(sourceFile).append(transferIds).append(states).append(regex).append(operations).append(sourceFiles).append(targetFile).append(dateTo).append(limit).append(targetFiles).toHashCode();
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
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(compact, rhs.compact).append(sources, rhs.sources).append(profiles, rhs.profiles).append(timeZone, rhs.timeZone).append(dateFrom, rhs.dateFrom).append(targets, rhs.targets).append(sourceFile, rhs.sourceFile).append(transferIds, rhs.transferIds).append(states, rhs.states).append(regex, rhs.regex).append(operations, rhs.operations).append(sourceFiles, rhs.sourceFiles).append(targetFile, rhs.targetFile).append(dateTo, rhs.dateTo).append(limit, rhs.limit).append(targetFiles, rhs.targetFiles).isEquals();
    }

}
