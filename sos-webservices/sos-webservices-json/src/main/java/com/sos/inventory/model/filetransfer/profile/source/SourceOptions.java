
package com.sos.inventory.model.filetransfer.profile.source;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Source Options
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "disableErrorOnNoFilesFound",
    "checkSteadyState",
    "checkIntegrityHash",
    "maxFiles",
    "minFileAge",
    "maxFileAge",
    "minFileSize",
    "maxFileSize",
    "polling",
    "resultSet",
    "transferZeroByteFiles"
})
public class SourceOptions {

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("disableErrorOnNoFilesFound")
    private String disableErrorOnNoFilesFound;
    /**
     * Check Steady State
     * <p>
     * 
     * 
     */
    @JsonProperty("checkSteadyState")
    private CheckSteadyState checkSteadyState;
    /**
     * Check Integrity Hash
     * <p>
     * 
     * 
     */
    @JsonProperty("checkIntegrityHash")
    private CheckIntegrityHash checkIntegrityHash;
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
     * Transfer zero-byte files: 'true', 'false', 'strict', 'relaxed', or a single variable reference.
     * <p>
     * 
     * 
     */
    @JsonProperty("transferZeroByteFiles")
    private String transferZeroByteFiles;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SourceOptions() {
    }

    /**
     * 
     * @param disableErrorOnNoFilesFound
     * @param minFileSize
     * @param minFileAge
     * @param maxFileSize
     * @param polling
     * @param maxFileAge
     * @param transferZeroByteFiles
     * @param checkIntegrityHash
     * @param maxFiles
     * @param resultSet
     * @param checkSteadyState
     */
    public SourceOptions(String disableErrorOnNoFilesFound, CheckSteadyState checkSteadyState, CheckIntegrityHash checkIntegrityHash, String maxFiles, String minFileAge, String maxFileAge, String minFileSize, String maxFileSize, Polling polling, ResultSet resultSet, String transferZeroByteFiles) {
        super();
        this.disableErrorOnNoFilesFound = disableErrorOnNoFilesFound;
        this.checkSteadyState = checkSteadyState;
        this.checkIntegrityHash = checkIntegrityHash;
        this.maxFiles = maxFiles;
        this.minFileAge = minFileAge;
        this.maxFileAge = maxFileAge;
        this.minFileSize = minFileSize;
        this.maxFileSize = maxFileSize;
        this.polling = polling;
        this.resultSet = resultSet;
        this.transferZeroByteFiles = transferZeroByteFiles;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("disableErrorOnNoFilesFound")
    public String getDisableErrorOnNoFilesFound() {
        return disableErrorOnNoFilesFound;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("disableErrorOnNoFilesFound")
    public void setDisableErrorOnNoFilesFound(String disableErrorOnNoFilesFound) {
        this.disableErrorOnNoFilesFound = disableErrorOnNoFilesFound;
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
     * Transfer zero-byte files: 'true', 'false', 'strict', 'relaxed', or a single variable reference.
     * <p>
     * 
     * 
     */
    @JsonProperty("transferZeroByteFiles")
    public String getTransferZeroByteFiles() {
        return transferZeroByteFiles;
    }

    /**
     * Transfer zero-byte files: 'true', 'false', 'strict', 'relaxed', or a single variable reference.
     * <p>
     * 
     * 
     */
    @JsonProperty("transferZeroByteFiles")
    public void setTransferZeroByteFiles(String transferZeroByteFiles) {
        this.transferZeroByteFiles = transferZeroByteFiles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("disableErrorOnNoFilesFound", disableErrorOnNoFilesFound).append("checkSteadyState", checkSteadyState).append("checkIntegrityHash", checkIntegrityHash).append("maxFiles", maxFiles).append("minFileAge", minFileAge).append("maxFileAge", maxFileAge).append("minFileSize", minFileSize).append("maxFileSize", maxFileSize).append("polling", polling).append("resultSet", resultSet).append("transferZeroByteFiles", transferZeroByteFiles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(disableErrorOnNoFilesFound).append(minFileSize).append(minFileAge).append(maxFileSize).append(polling).append(maxFileAge).append(transferZeroByteFiles).append(checkIntegrityHash).append(maxFiles).append(resultSet).append(checkSteadyState).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SourceOptions) == false) {
            return false;
        }
        SourceOptions rhs = ((SourceOptions) other);
        return new EqualsBuilder().append(disableErrorOnNoFilesFound, rhs.disableErrorOnNoFilesFound).append(minFileSize, rhs.minFileSize).append(minFileAge, rhs.minFileAge).append(maxFileSize, rhs.maxFileSize).append(polling, rhs.polling).append(maxFileAge, rhs.maxFileAge).append(transferZeroByteFiles, rhs.transferZeroByteFiles).append(checkIntegrityHash, rhs.checkIntegrityHash).append(maxFiles, rhs.maxFiles).append(resultSet, rhs.resultSet).append(checkSteadyState, rhs.checkSteadyState).isEquals();
    }

}
