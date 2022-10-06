
package com.sos.joc.model.history.order;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "consumed",
    "waitingFor"
})
public class ExpectNotices {

    @JsonProperty("consumed")
    private String consumed;
    @JsonProperty("waitingFor")
    private List<ExpectNotice> waitingFor = new ArrayList<ExpectNotice>();

    @JsonProperty("consumed")
    public String getConsumed() {
        return consumed;
    }

    @JsonProperty("consumed")
    public void setConsumed(String consumed) {
        this.consumed = consumed;
    }

    @JsonProperty("waitingFor")
    public List<ExpectNotice> getWaitingFor() {
        return waitingFor;
    }

    @JsonProperty("waitingFor")
    public void setWaitingFor(List<ExpectNotice> waitingFor) {
        this.waitingFor = waitingFor;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("consumed", consumed).append("waitingFor", waitingFor).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(consumed).append(waitingFor).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExpectNotices) == false) {
            return false;
        }
        ExpectNotices rhs = ((ExpectNotices) other);
        return new EqualsBuilder().append(consumed, rhs.consumed).append(waitingFor, rhs.waitingFor).isEquals();
    }

}
