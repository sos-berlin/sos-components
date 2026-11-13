
package com.sos.inventory.model.filetransfer.profile.source;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Source File Options
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "selection",
    "checkSteadyState",
    "directives",
    "polling",
    "resultSet",
    "maxFiles",
    "minFileAge",
    "maxFileAge",
    "minFileSize",
    "maxFileSize",
    "checkIntegrityHash"
})
public class SourceFileOptions {

    /**
     * Selection
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("selection")
    private Selection selection;
    /**
     * Check Steady State
     * <p>
     * 
     * 
     */
    @JsonProperty("checkSteadyState")
    private CheckSteadyState checkSteadyState;
    /**
     * Directives
     * <p>
     * 
     * 
     */
    @JsonProperty("directives")
    private Directives directives;
    /**
     * Polling
     * <p>
     * 
     * 
     */
    @JsonProperty("polling")
    private Polling polling;
    /**
     * Result Set
     * <p>
     * 
     * 
     */
    @JsonProperty("resultSet")
    private ResultSet resultSet;
    /**
     * A non-negative integer, -1, or a single variable reference such as ${VAR}. Used for MaxFiles.
     * <p>
     * 
     * 
     */
    @JsonProperty("maxFiles")
    private String maxFiles;
    /**
     * Seconds, day/second duration (e.g. 1d 30m), ISO-8601 datetime. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("minFileAge")
    private String minFileAge;
    /**
     * Seconds, day/second duration (e.g. 1d 30m), ISO-8601 datetime. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("maxFileAge")
    private String maxFileAge;
    /**
     * Byte count, human-readable file size (e.g. 1KB, 1.5MB, 1,5GB). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("minFileSize")
    private String minFileSize;
    /**
     * Byte count, human-readable file size (e.g. 1KB, 1.5MB, 1,5GB). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("maxFileSize")
    private String maxFileSize;
    /**
     * Check Integrity Hash
     * <p>
     * 
     * 
     */
    @JsonProperty("checkIntegrityHash")
    private CheckIntegrityHash checkIntegrityHash;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SourceFileOptions() {
    }

    /**
     * 
     * @param selection
     * @param directives
     * @param minFileSize
     * @param minFileAge
     * @param polling
     * @param maxFileSize
     * @param maxFileAge
     * @param resultSet
     * @param maxFiles
     * @param checkIntegrityHash
     * @param checkSteadyState
     */
    public SourceFileOptions(Selection selection, CheckSteadyState checkSteadyState, Directives directives, Polling polling, ResultSet resultSet, String maxFiles, String minFileAge, String maxFileAge, String minFileSize, String maxFileSize, CheckIntegrityHash checkIntegrityHash) {
        super();
        this.selection = selection;
        this.checkSteadyState = checkSteadyState;
        this.directives = directives;
        this.polling = polling;
        this.resultSet = resultSet;
        this.maxFiles = maxFiles;
        this.minFileAge = minFileAge;
        this.maxFileAge = maxFileAge;
        this.minFileSize = minFileSize;
        this.maxFileSize = maxFileSize;
        this.checkIntegrityHash = checkIntegrityHash;
    }

    /**
     * Selection
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("selection")
    public Selection getSelection() {
        return selection;
    }

    /**
     * Selection
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("selection")
    public void setSelection(Selection selection) {
        this.selection = selection;
    }

    /**
     * Check Steady State
     * <p>
     * 
     * 
     */
    @JsonProperty("checkSteadyState")
    public CheckSteadyState getCheckSteadyState() {
        return checkSteadyState;
    }

    /**
     * Check Steady State
     * <p>
     * 
     * 
     */
    @JsonProperty("checkSteadyState")
    public void setCheckSteadyState(CheckSteadyState checkSteadyState) {
        this.checkSteadyState = checkSteadyState;
    }

    /**
     * Directives
     * <p>
     * 
     * 
     */
    @JsonProperty("directives")
    public Directives getDirectives() {
        return directives;
    }

    /**
     * Directives
     * <p>
     * 
     * 
     */
    @JsonProperty("directives")
    public void setDirectives(Directives directives) {
        this.directives = directives;
    }

    /**
     * Polling
     * <p>
     * 
     * 
     */
    @JsonProperty("polling")
    public Polling getPolling() {
        return polling;
    }

    /**
     * Polling
     * <p>
     * 
     * 
     */
    @JsonProperty("polling")
    public void setPolling(Polling polling) {
        this.polling = polling;
    }

    /**
     * Result Set
     * <p>
     * 
     * 
     */
    @JsonProperty("resultSet")
    public ResultSet getResultSet() {
        return resultSet;
    }

    /**
     * Result Set
     * <p>
     * 
     * 
     */
    @JsonProperty("resultSet")
    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    /**
     * A non-negative integer, -1, or a single variable reference such as ${VAR}. Used for MaxFiles.
     * <p>
     * 
     * 
     */
    @JsonProperty("maxFiles")
    public String getMaxFiles() {
        return maxFiles;
    }

    /**
     * A non-negative integer, -1, or a single variable reference such as ${VAR}. Used for MaxFiles.
     * <p>
     * 
     * 
     */
    @JsonProperty("maxFiles")
    public void setMaxFiles(String maxFiles) {
        this.maxFiles = maxFiles;
    }

    /**
     * Seconds, day/second duration (e.g. 1d 30m), ISO-8601 datetime. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("minFileAge")
    public String getMinFileAge() {
        return minFileAge;
    }

    /**
     * Seconds, day/second duration (e.g. 1d 30m), ISO-8601 datetime. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("minFileAge")
    public void setMinFileAge(String minFileAge) {
        this.minFileAge = minFileAge;
    }

    /**
     * Seconds, day/second duration (e.g. 1d 30m), ISO-8601 datetime. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("maxFileAge")
    public String getMaxFileAge() {
        return maxFileAge;
    }

    /**
     * Seconds, day/second duration (e.g. 1d 30m), ISO-8601 datetime. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("maxFileAge")
    public void setMaxFileAge(String maxFileAge) {
        this.maxFileAge = maxFileAge;
    }

    /**
     * Byte count, human-readable file size (e.g. 1KB, 1.5MB, 1,5GB). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("minFileSize")
    public String getMinFileSize() {
        return minFileSize;
    }

    /**
     * Byte count, human-readable file size (e.g. 1KB, 1.5MB, 1,5GB). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("minFileSize")
    public void setMinFileSize(String minFileSize) {
        this.minFileSize = minFileSize;
    }

    /**
     * Byte count, human-readable file size (e.g. 1KB, 1.5MB, 1,5GB). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("maxFileSize")
    public String getMaxFileSize() {
        return maxFileSize;
    }

    /**
     * Byte count, human-readable file size (e.g. 1KB, 1.5MB, 1,5GB). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("maxFileSize")
    public void setMaxFileSize(String maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    /**
     * Check Integrity Hash
     * <p>
     * 
     * 
     */
    @JsonProperty("checkIntegrityHash")
    public CheckIntegrityHash getCheckIntegrityHash() {
        return checkIntegrityHash;
    }

    /**
     * Check Integrity Hash
     * <p>
     * 
     * 
     */
    @JsonProperty("checkIntegrityHash")
    public void setCheckIntegrityHash(CheckIntegrityHash checkIntegrityHash) {
        this.checkIntegrityHash = checkIntegrityHash;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("selection", selection).append("checkSteadyState", checkSteadyState).append("directives", directives).append("polling", polling).append("resultSet", resultSet).append("maxFiles", maxFiles).append("minFileAge", minFileAge).append("maxFileAge", maxFileAge).append("minFileSize", minFileSize).append("maxFileSize", maxFileSize).append("checkIntegrityHash", checkIntegrityHash).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(selection).append(directives).append(minFileSize).append(minFileAge).append(polling).append(maxFileSize).append(maxFileAge).append(resultSet).append(maxFiles).append(checkIntegrityHash).append(checkSteadyState).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SourceFileOptions) == false) {
            return false;
        }
        SourceFileOptions rhs = ((SourceFileOptions) other);
        return new EqualsBuilder().append(selection, rhs.selection).append(directives, rhs.directives).append(minFileSize, rhs.minFileSize).append(minFileAge, rhs.minFileAge).append(polling, rhs.polling).append(maxFileSize, rhs.maxFileSize).append(maxFileAge, rhs.maxFileAge).append(resultSet, rhs.resultSet).append(maxFiles, rhs.maxFiles).append(checkIntegrityHash, rhs.checkIntegrityHash).append(checkSteadyState, rhs.checkSteadyState).isEquals();
    }

}
