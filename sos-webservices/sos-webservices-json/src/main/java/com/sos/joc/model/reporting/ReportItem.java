
package com.sos.joc.model.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.report.Frequency;
import com.sos.inventory.model.report.TemplateId;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * report item from REPORT_HISTORY
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "runId",
    "path",
    "title",
    "templateName",
    "frequency",
    "hits",
    "dateFrom",
    "dateTo",
    "created",
    "modified",
    "data"
})
public class ReportItem {

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    private Long id;
    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("runId")
    private Long runId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("path")
    private String path;
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
     * Frequencies for report
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("frequency")
    private Frequency frequency;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("hits")
    private Integer hits;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * (Required)
     * 
     */
    @JsonProperty("dateFrom")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String dateFrom;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * (Required)
     * 
     */
    @JsonProperty("dateTo")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String dateTo;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("created")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date created;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date modified;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("data")
    private List<ReportData> data = new ArrayList<ReportData>();

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("runId")
    public Long getRunId() {
        return runId;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("runId")
    public void setRunId(Long runId) {
        this.runId = runId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
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
     * Frequencies for report
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("frequency")
    public Frequency getFrequency() {
        return frequency;
    }

    /**
     * Frequencies for report
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("frequency")
    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
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

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * (Required)
     * 
     */
    @JsonProperty("dateTo")
    public String getDateTo() {
        return dateTo;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * (Required)
     * 
     */
    @JsonProperty("dateTo")
    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("created")
    public Date getCreated() {
        return created;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("created")
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    public Date getModified() {
        return modified;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("data")
    public List<ReportData> getData() {
        return data;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("data")
    public void setData(List<ReportData> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("runId", runId).append("path", path).append("title", title).append("templateName", templateName).append("frequency", frequency).append("hits", hits).append("dateFrom", dateFrom).append("dateTo", dateTo).append("created", created).append("modified", modified).append("data", data).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(data).append(created).append(title).append(dateFrom).append(frequency).append(hits).append(path).append(templateName).append(dateTo).append(modified).append(id).append(runId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReportItem) == false) {
            return false;
        }
        ReportItem rhs = ((ReportItem) other);
        return new EqualsBuilder().append(data, rhs.data).append(created, rhs.created).append(title, rhs.title).append(dateFrom, rhs.dateFrom).append(frequency, rhs.frequency).append(hits, rhs.hits).append(path, rhs.path).append(templateName, rhs.templateName).append(dateTo, rhs.dateTo).append(modified, rhs.modified).append(id, rhs.id).append(runId, rhs.runId).isEquals();
    }

}
