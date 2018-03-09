
package com.sos.joc.model.schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * schedule (permant part)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "surveyDate",
    "path",
    "name",
    "title",
    "substitute",
    "substitutedBy",
    "usedByOrders",
    "usedByJobs",
    "configurationDate"
})
public class ScheduleP {

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    @JacksonXmlProperty(localName = "surveyDate")
    private Date surveyDate;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "path")
    private String path;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JacksonXmlProperty(localName = "name")
    private String name;
    @JsonProperty("title")
    @JacksonXmlProperty(localName = "title")
    private String title;
    /**
     * substitute
     * <p>
     * 
     * 
     */
    @JsonProperty("substitute")
    @JacksonXmlProperty(localName = "substitute")
    private Substitute substitute;
    @JsonProperty("substitutedBy")
    @JacksonXmlProperty(localName = "substitutedBy")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "substitutedBy")
    private List<Substitute> substitutedBy = new ArrayList<Substitute>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("usedByOrders")
    @JacksonXmlProperty(localName = "usedByOrder")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "usedByOrders")
    private List<UsedByOrder> usedByOrders = new ArrayList<UsedByOrder>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("usedByJobs")
    @JacksonXmlProperty(localName = "usedByJob")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "usedByJobs")
    private List<UsedByJob> usedByJobs = new ArrayList<UsedByJob>();
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("configurationDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "configurationDate")
    private Date configurationDate;

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    @JacksonXmlProperty(localName = "surveyDate")
    public Date getSurveyDate() {
        return surveyDate;
    }

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    @JacksonXmlProperty(localName = "surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JacksonXmlProperty(localName = "path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JacksonXmlProperty(localName = "path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JacksonXmlProperty(localName = "name")
    public String getName() {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JacksonXmlProperty(localName = "name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("title")
    @JacksonXmlProperty(localName = "title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    @JacksonXmlProperty(localName = "title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * substitute
     * <p>
     * 
     * 
     */
    @JsonProperty("substitute")
    @JacksonXmlProperty(localName = "substitute")
    public Substitute getSubstitute() {
        return substitute;
    }

    /**
     * substitute
     * <p>
     * 
     * 
     */
    @JsonProperty("substitute")
    @JacksonXmlProperty(localName = "substitute")
    public void setSubstitute(Substitute substitute) {
        this.substitute = substitute;
    }

    @JsonProperty("substitutedBy")
    @JacksonXmlProperty(localName = "substitutedBy")
    public List<Substitute> getSubstitutedBy() {
        return substitutedBy;
    }

    @JsonProperty("substitutedBy")
    @JacksonXmlProperty(localName = "substitutedBy")
    public void setSubstitutedBy(List<Substitute> substitutedBy) {
        this.substitutedBy = substitutedBy;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("usedByOrders")
    @JacksonXmlProperty(localName = "usedByOrder")
    public List<UsedByOrder> getUsedByOrders() {
        return usedByOrders;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("usedByOrders")
    @JacksonXmlProperty(localName = "usedByOrder")
    public void setUsedByOrders(List<UsedByOrder> usedByOrders) {
        this.usedByOrders = usedByOrders;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("usedByJobs")
    @JacksonXmlProperty(localName = "usedByJob")
    public List<UsedByJob> getUsedByJobs() {
        return usedByJobs;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("usedByJobs")
    @JacksonXmlProperty(localName = "usedByJob")
    public void setUsedByJobs(List<UsedByJob> usedByJobs) {
        this.usedByJobs = usedByJobs;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("configurationDate")
    @JacksonXmlProperty(localName = "configurationDate")
    public Date getConfigurationDate() {
        return configurationDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("configurationDate")
    @JacksonXmlProperty(localName = "configurationDate")
    public void setConfigurationDate(Date configurationDate) {
        this.configurationDate = configurationDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("surveyDate", surveyDate).append("path", path).append("name", name).append("title", title).append("substitute", substitute).append("substitutedBy", substitutedBy).append("usedByOrders", usedByOrders).append("usedByJobs", usedByJobs).append("configurationDate", configurationDate).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(configurationDate).append(path).append(substitutedBy).append(surveyDate).append(name).append(usedByJobs).append(title).append(usedByOrders).append(substitute).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ScheduleP) == false) {
            return false;
        }
        ScheduleP rhs = ((ScheduleP) other);
        return new EqualsBuilder().append(configurationDate, rhs.configurationDate).append(path, rhs.path).append(substitutedBy, rhs.substitutedBy).append(surveyDate, rhs.surveyDate).append(name, rhs.name).append(usedByJobs, rhs.usedByJobs).append(title, rhs.title).append(usedByOrders, rhs.usedByOrders).append(substitute, rhs.substitute).isEquals();
    }

}
