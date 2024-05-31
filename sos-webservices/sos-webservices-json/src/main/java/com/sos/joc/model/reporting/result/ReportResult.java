
package com.sos.joc.model.reporting.result;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * report result
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "type",
    "data"
})
public class ReportResult {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    private String type;
    @JsonProperty("data")
    private List<ReportResultData> data = new ArrayList<ReportResultData>();

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("data")
    public List<ReportResultData> getData() {
        return data;
    }

    @JsonProperty("data")
    public void setData(List<ReportResultData> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("type", type).append("data", data).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(type).append(data).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReportResult) == false) {
            return false;
        }
        ReportResult rhs = ((ReportResult) other);
        return new EqualsBuilder().append(type, rhs.type).append(data, rhs.data).isEquals();
    }

}
