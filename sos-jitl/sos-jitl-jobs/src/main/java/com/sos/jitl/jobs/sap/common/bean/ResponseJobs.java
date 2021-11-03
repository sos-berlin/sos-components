
package com.sos.jitl.jobs.sap.common.bean;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * retrieve jobs
 * <p>
 * e.g. Response 201 of POST /scheduler/jobs
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "total",
    "results",
    "prev_url",
    "next_url"
})
public class ResponseJobs {

    @JsonProperty("total")
    private Integer total;
    @JsonProperty("results")
    private List<ResponseJob> results = new ArrayList<ResponseJob>();
    @JsonProperty("prev_url")
    private String prev_url;
    @JsonProperty("next_url")
    private String next_url;

    @JsonProperty("total")
    public Integer getTotal() {
        return total;
    }

    @JsonProperty("total")
    public void setTotal(Integer total) {
        this.total = total;
    }

    public ResponseJobs withTotal(Integer total) {
        this.total = total;
        return this;
    }

    @JsonProperty("results")
    public List<ResponseJob> getResults() {
        return results;
    }

    @JsonProperty("results")
    public void setResults(List<ResponseJob> results) {
        this.results = results;
    }

    public ResponseJobs withResults(List<ResponseJob> results) {
        this.results = results;
        return this;
    }

    @JsonProperty("prev_url")
    public String getPrev_url() {
        return prev_url;
    }

    @JsonProperty("prev_url")
    public void setPrev_url(String prev_url) {
        this.prev_url = prev_url;
    }

    public ResponseJobs withPrev_url(String prev_url) {
        this.prev_url = prev_url;
        return this;
    }

    @JsonProperty("next_url")
    public String getNext_url() {
        return next_url;
    }

    @JsonProperty("next_url")
    public void setNext_url(String next_url) {
        this.next_url = next_url;
    }

    public ResponseJobs withNext_url(String next_url) {
        this.next_url = next_url;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("total", total).append("results", results).append("prev_url", prev_url).append("next_url", next_url).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(next_url).append(total).append(prev_url).append(results).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseJobs) == false) {
            return false;
        }
        ResponseJobs rhs = ((ResponseJobs) other);
        return new EqualsBuilder().append(next_url, rhs.next_url).append(total, rhs.total).append(prev_url, rhs.prev_url).append(results, rhs.results).isEquals();
    }

}
