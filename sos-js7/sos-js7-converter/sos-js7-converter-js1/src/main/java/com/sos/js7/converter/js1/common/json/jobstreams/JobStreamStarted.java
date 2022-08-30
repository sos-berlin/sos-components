
package com.sos.js7.converter.js1.common.json.jobstreams;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.js7.converter.js1.common.json.NameValuePair;


/**
 * JobStreamStartJob
 * <p>
 * Reset Workflow, unconsume expressions
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobStreamStarterId",
    "starterName",
    "starterExists",
    "session",
    "params"
})
public class JobStreamStarted {

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStreamStarterId")
    private Long jobStreamStarterId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("starterName")
    private String starterName;
    @JsonProperty("starterExists")
    private Boolean starterExists;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("session")
    private String session;
    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("params")
    private List<NameValuePair> params = new ArrayList<NameValuePair>();

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStreamStarterId")
    public Long getJobStreamStarterId() {
        return jobStreamStarterId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStreamStarterId")
    public void setJobStreamStarterId(Long jobStreamStarterId) {
        this.jobStreamStarterId = jobStreamStarterId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("starterName")
    public String getStarterName() {
        return starterName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("starterName")
    public void setStarterName(String starterName) {
        this.starterName = starterName;
    }

    @JsonProperty("starterExists")
    public Boolean getStarterExists() {
        return starterExists;
    }

    @JsonProperty("starterExists")
    public void setStarterExists(Boolean starterExists) {
        this.starterExists = starterExists;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("session")
    public String getSession() {
        return session;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("session")
    public void setSession(String session) {
        this.session = session;
    }

    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("params")
    public List<NameValuePair> getParams() {
        return params;
    }

    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("params")
    public void setParams(List<NameValuePair> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobStreamStarterId", jobStreamStarterId).append("starterName", starterName).append("starterExists", starterExists).append("session", session).append("params", params).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobStreamStarterId).append(starterExists).append(params).append(starterName).append(session).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobStreamStarted) == false) {
            return false;
        }
        JobStreamStarted rhs = ((JobStreamStarted) other);
        return new EqualsBuilder().append(jobStreamStarterId, rhs.jobStreamStarterId).append(starterExists, rhs.starterExists).append(params, rhs.params).append(starterName, rhs.starterName).append(session, rhs.session).isEquals();
    }

}
