
package com.sos.joc.model.job;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.NameValuePair;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * start job command
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "job",
    "at",
    "timeZone",
    "params",
    "environment"
})
public class StartJob {

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("job")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String job;
    /**
     * timestamp with now
     * <p>
     * ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS
     * 
     */
    @JsonProperty("at")
    @JsonPropertyDescription("ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS")
    private String at;
    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timeZone;
    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("params")
    private List<NameValuePair> params = new ArrayList<NameValuePair>();
    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("environment")
    private List<NameValuePair> environment = new ArrayList<NameValuePair>();

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("job")
    public String getJob() {
        return job;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("job")
    public void setJob(String job) {
        this.job = job;
    }

    /**
     * timestamp with now
     * <p>
     * ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS
     * 
     */
    @JsonProperty("at")
    public String getAt() {
        return at;
    }

    /**
     * timestamp with now
     * <p>
     * ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS
     * 
     */
    @JsonProperty("at")
    public void setAt(String at) {
        this.at = at;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
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

    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("environment")
    public List<NameValuePair> getEnvironment() {
        return environment;
    }

    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("environment")
    public void setEnvironment(List<NameValuePair> environment) {
        this.environment = environment;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("job", job).append("at", at).append("timeZone", timeZone).append("params", params).append("environment", environment).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(timeZone).append(environment).append(at).append(job).append(params).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StartJob) == false) {
            return false;
        }
        StartJob rhs = ((StartJob) other);
        return new EqualsBuilder().append(timeZone, rhs.timeZone).append(environment, rhs.environment).append(at, rhs.at).append(job, rhs.job).append(params, rhs.params).isEquals();
    }

}
