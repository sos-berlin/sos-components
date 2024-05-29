
package com.sos.inventory.model.report;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAlias;
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
    "templateName",
    "frequencies",
    "hits",
    "controllerId",
    "monthFrom",
    "monthTo",
    "period",
    "sort"
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
    private String version = "1.7.1";
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
    /**
     * Template identifier for report
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("templateName")
    @JsonAlias({ "templateId" })
    private TemplateId templateName;
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
    private String controllerId;
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
     * report period
     * <p>
     * 
     * 
     */
    @JsonProperty("period")
    private ReportPeriod period;
    @JsonProperty("sort")
    private ReportOrder sort = ReportOrder.fromValue("HIGHEST");

    /**
     * No args constructor for use in serialization
     * 
     */
    public Report() {
    }

    /**
     * 
     * @param hits
     * @param period
     * @param controllerId
     * @param templateName
     * @param monthFrom
     * @param sort
     * @param title
     * @param version
     * @param frequencies
     * @param monthTo
     */
    public Report(String version, String title, TemplateId templateName, Set<Frequency> frequencies, Integer hits, String controllerId, String monthFrom, String monthTo, ReportPeriod period, ReportOrder sort) {
        super();
        this.version = version;
        this.title = title;
        this.templateName = templateName;
        this.frequencies = frequencies;
        this.hits = hits;
        this.controllerId = controllerId;
        this.monthFrom = monthFrom;
        this.monthTo = monthTo;
        this.period = period;
        this.sort = sort;
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
     * Template identifier for report
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("templateName")
    public TemplateId getTemplateName() {
        return templateName;
    }

    /**
     * Template identifier for report
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("templateName")
    public void setTemplateName(TemplateId templateName) {
        this.templateName = templateName;
    }
    
    @JsonProperty("templateId")
    public void setTemplateName(Integer templateId) {
        this.templateName = TemplateId.fromValue(templateId);
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
    public String getControllerId() {
        return controllerId;
    }

    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
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

    /**
     * report period
     * <p>
     * 
     * 
     */
    @JsonProperty("period")
    public ReportPeriod getPeriod() {
        return period;
    }

    /**
     * report period
     * <p>
     * 
     * 
     */
    @JsonProperty("period")
    public void setPeriod(ReportPeriod period) {
        this.period = period;
    }

    @JsonProperty("sort")
    public ReportOrder getSort() {
        return sort;
    }

    @JsonProperty("sort")
    public void setSort(ReportOrder sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("version", version).append("title", title).append("templateName", templateName).append("frequencies", frequencies).append("hits", hits).append("controllerId", controllerId).append("monthFrom", monthFrom).append("monthTo", monthTo).append("period", period).append("sort", sort).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(hits).append(period).append(controllerId).append(templateName).append(monthFrom).append(sort).append(title).append(version).append(frequencies).append(monthTo).toHashCode();
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
        return new EqualsBuilder().append(hits, rhs.hits).append(period, rhs.period).append(controllerId, rhs.controllerId).append(templateName, rhs.templateName).append(monthFrom, rhs.monthFrom).append(sort, rhs.sort).append(title, rhs.title).append(version, rhs.version).append(frequencies, rhs.frequencies).append(monthTo, rhs.monthTo).isEquals();
    }

}
