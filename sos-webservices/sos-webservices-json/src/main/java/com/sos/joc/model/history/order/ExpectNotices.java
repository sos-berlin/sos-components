
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
    "exists",
    "notExists"
})
public class ExpectNotices {

    @JsonProperty("exists")
    private String exists;
    @JsonProperty("notExists")
    private List<ExpectNotice> notExists = new ArrayList<ExpectNotice>();

    @JsonProperty("exists")
    public String getExists() {
        return exists;
    }

    @JsonProperty("exists")
    public void setExists(String exists) {
        this.exists = exists;
    }

    @JsonProperty("notExists")
    public List<ExpectNotice> getNotExists() {
        return notExists;
    }

    @JsonProperty("notExists")
    public void setNotExists(List<ExpectNotice> notExists) {
        this.notExists = notExists;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("exists", exists).append("notExists", notExists).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(notExists).append(exists).toHashCode();
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
        return new EqualsBuilder().append(notExists, rhs.notExists).append(exists, rhs.exists).isEquals();
    }

}
