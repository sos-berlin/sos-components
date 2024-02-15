
package com.sos.joc.model.reporting;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * parameter for a report run
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "templateId",
    "frequencies",
    "size",
    "dateFrom"
})
public class RunFilter {

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("templateId")
    private Integer templateId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("frequencies")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Frequency> frequencies = new LinkedHashSet<Frequency>();
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("size")
    private Integer size;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dateFrom")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String dateFrom;

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("templateId")
    public Integer getTemplateId() {
        return templateId;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("templateId")
    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("frequencies")
    public Set<Frequency> getFrequencies() {
        return frequencies;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("frequencies")
    public void setFrequencies(Set<Frequency> frequencies) {
        this.frequencies = frequencies;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("size")
    public Integer getSize() {
        return size;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("size")
    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("templateId", templateId).append("frequencies", frequencies).append("size", size).append("dateFrom", dateFrom).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(templateId).append(size).append(dateFrom).append(frequencies).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RunFilter) == false) {
            return false;
        }
        RunFilter rhs = ((RunFilter) other);
        return new EqualsBuilder().append(templateId, rhs.templateId).append(size, rhs.size).append(dateFrom, rhs.dateFrom).append(frequencies, rhs.frequencies).isEquals();
    }

}
