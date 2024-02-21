
package com.sos.inventory.model.report;

import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.inventory.model.common.IInventoryObject;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.common.IReleaseObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * report
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "version",
    "title",
    "templateId",
    "frequencies",
    "hits",
    "controllerId",
    "monthFrom",
    "monthTo"
})
public class Report implements IInventoryObject, IConfigurationObject, IReleaseObject
{

    /**
     * inventory repository version
     * <p>
     * inventory repository version
     * 
     */
    @JsonProperty("version")
    @JsonPropertyDescription("inventory repository version")
    private String version = "1.6.1";
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
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
    private Set<Frequency> frequencies = null;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("hits")
    private Integer hits;
    @JsonProperty("controllerId")
    private Object controllerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("monthFrom")
    private String monthFrom;
    @JsonProperty("monthTo")
    private String monthTo;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Report() {
    }

    /**
     * 
     * @param hits
     * @param controllerId
     * @param monthFrom
     * @param title
     * @param templateId
     * @param version
     * @param frequencies
     * @param monthTo
     */
    public Report(String version, String title, Integer templateId, Set<Frequency> frequencies, Integer hits, Object controllerId, String monthFrom, String monthTo) {
        super();
        this.version = version;
        this.title = title;
        this.templateId = templateId;
        this.frequencies = frequencies;
        this.hits = hits;
        this.controllerId = controllerId;
        this.monthFrom = monthFrom;
        this.monthTo = monthTo;
    }

    /**
     * inventory repository version
     * <p>
     * inventory repository version
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * inventory repository version
     * <p>
     * inventory repository version
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

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
     * 
     * (Required)
     * 
     */
    @JsonProperty("hits")
    public Integer getHits() {
        return hits;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("hits")
    public void setHits(Integer hits) {
        this.hits = hits;
    }

    @JsonProperty("controllerId")
    public Object getControllerId() {
        return controllerId;
    }

    @JsonProperty("controllerId")
    public void setControllerId(Object controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("monthFrom")
    public String getMonthFrom() {
        return monthFrom;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("monthFrom")
    public void setMonthFrom(String monthFrom) {
        this.monthFrom = monthFrom;
    }

    @JsonProperty("monthTo")
    public String getMonthTo() {
        return monthTo;
    }

    @JsonProperty("monthTo")
    public void setMonthTo(String monthTo) {
        this.monthTo = monthTo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("version", version).append("title", title).append("templateId", templateId).append("frequencies", frequencies).append("hits", hits).append("controllerId", controllerId).append("monthFrom", monthFrom).append("monthTo", monthTo).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(hits).append(controllerId).append(monthFrom).append(title).append(templateId).append(version).append(frequencies).append(monthTo).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Report) == false) {
            return false;
        }
        Report rhs = ((Report) other);
        return new EqualsBuilder().append(hits, rhs.hits).append(controllerId, rhs.controllerId).append(monthFrom, rhs.monthFrom).append(title, rhs.title).append(templateId, rhs.templateId).append(version, rhs.version).append(frequencies, rhs.frequencies).append(monthTo, rhs.monthTo).isEquals();
    }

}
