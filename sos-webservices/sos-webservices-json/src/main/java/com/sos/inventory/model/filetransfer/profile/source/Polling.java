
package com.sos.inventory.model.filetransfer.profile.source;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Polling
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "pollInterval",
    "pollTimeout",
    "minFiles",
    "waitForSourceFolder",
    "pollingServer",
    "pollingServerDuration",
    "pollForever"
})
public class Polling {

    /**
     * Duration in seconds, human-readable format (e.g. 30s, 1m, 1h 30m). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("pollInterval")
    private String pollInterval;
    /**
     * Duration expressed in minutes/hours. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("pollTimeout")
    private String pollTimeout;
    /**
     * A non-negative integer (digits only). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("minFiles")
    private String minFiles;
    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("waitForSourceFolder")
    private String waitForSourceFolder;
    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("pollingServer")
    private String pollingServer;
    /**
     * Duration in seconds, human-readable format (e.g. 30s, 1m, 1h 30m). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("pollingServerDuration")
    private String pollingServerDuration;
    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("pollForever")
    private String pollForever;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Polling() {
    }

    /**
     * 
     * @param pollForever
     * @param pollInterval
     * @param waitForSourceFolder
     * @param pollTimeout
     * @param minFiles
     * @param pollingServer
     * @param pollingServerDuration
     */
    public Polling(String pollInterval, String pollTimeout, String minFiles, String waitForSourceFolder, String pollingServer, String pollingServerDuration, String pollForever) {
        super();
        this.pollInterval = pollInterval;
        this.pollTimeout = pollTimeout;
        this.minFiles = minFiles;
        this.waitForSourceFolder = waitForSourceFolder;
        this.pollingServer = pollingServer;
        this.pollingServerDuration = pollingServerDuration;
        this.pollForever = pollForever;
    }

    /**
     * Duration in seconds, human-readable format (e.g. 30s, 1m, 1h 30m). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("pollInterval")
    public String getPollInterval() {
        return pollInterval;
    }

    /**
     * Duration in seconds, human-readable format (e.g. 30s, 1m, 1h 30m). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("pollInterval")
    public void setPollInterval(String pollInterval) {
        this.pollInterval = pollInterval;
    }

    /**
     * Duration expressed in minutes/hours. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("pollTimeout")
    public String getPollTimeout() {
        return pollTimeout;
    }

    /**
     * Duration expressed in minutes/hours. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("pollTimeout")
    public void setPollTimeout(String pollTimeout) {
        this.pollTimeout = pollTimeout;
    }

    /**
     * A non-negative integer (digits only). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("minFiles")
    public String getMinFiles() {
        return minFiles;
    }

    /**
     * A non-negative integer (digits only). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("minFiles")
    public void setMinFiles(String minFiles) {
        this.minFiles = minFiles;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("waitForSourceFolder")
    public String getWaitForSourceFolder() {
        return waitForSourceFolder;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("waitForSourceFolder")
    public void setWaitForSourceFolder(String waitForSourceFolder) {
        this.waitForSourceFolder = waitForSourceFolder;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("pollingServer")
    public String getPollingServer() {
        return pollingServer;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("pollingServer")
    public void setPollingServer(String pollingServer) {
        this.pollingServer = pollingServer;
    }

    /**
     * Duration in seconds, human-readable format (e.g. 30s, 1m, 1h 30m). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("pollingServerDuration")
    public String getPollingServerDuration() {
        return pollingServerDuration;
    }

    /**
     * Duration in seconds, human-readable format (e.g. 30s, 1m, 1h 30m). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("pollingServerDuration")
    public void setPollingServerDuration(String pollingServerDuration) {
        this.pollingServerDuration = pollingServerDuration;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("pollForever")
    public String getPollForever() {
        return pollForever;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("pollForever")
    public void setPollForever(String pollForever) {
        this.pollForever = pollForever;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("pollInterval", pollInterval).append("pollTimeout", pollTimeout).append("minFiles", minFiles).append("waitForSourceFolder", waitForSourceFolder).append("pollingServer", pollingServer).append("pollingServerDuration", pollingServerDuration).append("pollForever", pollForever).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(pollForever).append(pollInterval).append(waitForSourceFolder).append(pollTimeout).append(minFiles).append(pollingServer).append(pollingServerDuration).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Polling) == false) {
            return false;
        }
        Polling rhs = ((Polling) other);
        return new EqualsBuilder().append(pollForever, rhs.pollForever).append(pollInterval, rhs.pollInterval).append(waitForSourceFolder, rhs.waitForSourceFolder).append(pollTimeout, rhs.pollTimeout).append(minFiles, rhs.minFiles).append(pollingServer, rhs.pollingServer).append(pollingServerDuration, rhs.pollingServerDuration).isEquals();
    }

}
