
package com.sos.joc.model.inventory.release;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.common.RequestFilter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * filter for release
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "onlyValidObjects",
    "withoutDrafts",
    "withoutReleased"
})
public class ReleasableFilter
    extends RequestFilter
{

    @JsonProperty("onlyValidObjects")
    private Boolean onlyValidObjects = false;
    @JsonProperty("withoutDrafts")
    private Boolean withoutDrafts = false;
    @JsonProperty("withoutReleased")
    private Boolean withoutReleased = false;

    @JsonProperty("onlyValidObjects")
    public Boolean getOnlyValidObjects() {
        return onlyValidObjects;
    }

    @JsonProperty("onlyValidObjects")
    public void setOnlyValidObjects(Boolean onlyValidObjects) {
        this.onlyValidObjects = onlyValidObjects;
    }

    @JsonProperty("withoutDrafts")
    public Boolean getWithoutDrafts() {
        return withoutDrafts;
    }

    @JsonProperty("withoutDrafts")
    public void setWithoutDrafts(Boolean withoutDrafts) {
        this.withoutDrafts = withoutDrafts;
    }

    @JsonProperty("withoutReleased")
    public Boolean getWithoutReleased() {
        return withoutReleased;
    }

    @JsonProperty("withoutReleased")
    public void setWithoutReleased(Boolean withoutReleased) {
        this.withoutReleased = withoutReleased;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("onlyValidObjects", onlyValidObjects).append("withoutDrafts", withoutDrafts).append("withoutReleased", withoutReleased).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(withoutDrafts).append(onlyValidObjects).append(withoutReleased).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReleasableFilter) == false) {
            return false;
        }
        ReleasableFilter rhs = ((ReleasableFilter) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(withoutDrafts, rhs.withoutDrafts).append(onlyValidObjects, rhs.onlyValidObjects).append(withoutReleased, rhs.withoutReleased).isEquals();
    }

}
