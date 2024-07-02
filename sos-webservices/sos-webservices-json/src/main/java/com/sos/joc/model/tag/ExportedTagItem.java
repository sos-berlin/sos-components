
package com.sos.joc.model.tag;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Ex-/Import schema for tag items
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "ordering",
    "usedBy"
})
public class ExportedTagItem {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("ordering")
    private Integer ordering;
    @JsonProperty("usedBy")
    private List<ExportedTagItems> usedBy = new ArrayList<ExportedTagItems>();

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("ordering")
    public Integer getOrdering() {
        return ordering;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("ordering")
    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }

    @JsonProperty("usedBy")
    public List<ExportedTagItems> getUsedBy() {
        return usedBy;
    }

    @JsonProperty("usedBy")
    public void setUsedBy(List<ExportedTagItems> usedBy) {
        this.usedBy = usedBy;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("ordering", ordering).append("usedBy", usedBy).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(ordering).append(usedBy).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExportedTagItem) == false) {
            return false;
        }
        ExportedTagItem rhs = ((ExportedTagItem) other);
        return new EqualsBuilder().append(name, rhs.name).append(ordering, rhs.ordering).append(usedBy, rhs.usedBy).isEquals();
    }

}
