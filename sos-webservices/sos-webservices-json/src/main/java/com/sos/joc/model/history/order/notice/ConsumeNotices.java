
package com.sos.joc.model.history.order.notice;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ConsumeNotices
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "consumed",
    "consuming"
})
public class ConsumeNotices {

    @JsonProperty("consumed")
    private Boolean consumed;
    @JsonProperty("consuming")
    private List<BaseNotice> consuming = new ArrayList<BaseNotice>();

    @JsonProperty("consumed")
    public Boolean getConsumed() {
        return consumed;
    }

    @JsonProperty("consumed")
    public void setConsumed(Boolean consumed) {
        this.consumed = consumed;
    }

    @JsonProperty("consuming")
    public List<BaseNotice> getConsuming() {
        return consuming;
    }

    @JsonProperty("consuming")
    public void setConsuming(List<BaseNotice> consuming) {
        this.consuming = consuming;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("consumed", consumed).append("consuming", consuming).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(consumed).append(consuming).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConsumeNotices) == false) {
            return false;
        }
        ConsumeNotices rhs = ((ConsumeNotices) other);
        return new EqualsBuilder().append(consumed, rhs.consumed).append(consuming, rhs.consuming).isEquals();
    }

}
