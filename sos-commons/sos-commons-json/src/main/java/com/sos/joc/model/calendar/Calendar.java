
package com.sos.joc.model.calendar;

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
 * calendar
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "path",
    "name",
    "basedOn",
    "type",
    "category",
    "title",
    "from",
    "to",
    "periods",
    "includes",
    "excludes",
    "usedBy"
})
public class Calendar {

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    private Long id;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "path")
    private String path;
    @JsonProperty("name")
    @JacksonXmlProperty(localName = "name")
    private String name;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("basedOn")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "basedOn")
    private String basedOn;
    /**
     * calendar type
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    @JacksonXmlProperty(localName = "type")
    private CalendarType type = CalendarType.fromValue("WORKING_DAYS");
    @JsonProperty("category")
    @JacksonXmlProperty(localName = "category")
    private String category;
    @JsonProperty("title")
    @JacksonXmlProperty(localName = "title")
    private String title;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("from")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    @JacksonXmlProperty(localName = "from")
    private String from;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("to")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    @JacksonXmlProperty(localName = "to")
    private String to;
    @JsonProperty("periods")
    @JacksonXmlProperty(localName = "period")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "periods")
    private List<Period> periods = null;
    /**
     * frequencies
     * <p>
     * 
     * 
     */
    @JsonProperty("includes")
    @JacksonXmlProperty(localName = "includes")
    private Frequencies includes;
    /**
     * frequencies
     * <p>
     * 
     * 
     */
    @JsonProperty("excludes")
    @JacksonXmlProperty(localName = "excludes")
    private Frequencies excludes;
    /**
     * collections of objects which use calendar
     * <p>
     * 
     * 
     */
    @JsonProperty("usedBy")
    @JacksonXmlProperty(localName = "usedBy")
    private UsedBy usedBy;

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    public Long getId() {
        return id;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
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
     * 
     */
    @JsonProperty("path")
    @JacksonXmlProperty(localName = "path")
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty("name")
    @JacksonXmlProperty(localName = "name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    @JacksonXmlProperty(localName = "name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("basedOn")
    @JacksonXmlProperty(localName = "basedOn")
    public String getBasedOn() {
        return basedOn;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("basedOn")
    @JacksonXmlProperty(localName = "basedOn")
    public void setBasedOn(String basedOn) {
        this.basedOn = basedOn;
    }

    /**
     * calendar type
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    @JacksonXmlProperty(localName = "type")
    public CalendarType getType() {
        return type;
    }

    /**
     * calendar type
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    @JacksonXmlProperty(localName = "type")
    public void setType(CalendarType type) {
        this.type = type;
    }

    @JsonProperty("category")
    @JacksonXmlProperty(localName = "category")
    public String getCategory() {
        return category;
    }

    @JsonProperty("category")
    @JacksonXmlProperty(localName = "category")
    public void setCategory(String category) {
        this.category = category;
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
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("from")
    @JacksonXmlProperty(localName = "from")
    public String getFrom() {
        return from;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("from")
    @JacksonXmlProperty(localName = "from")
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("to")
    @JacksonXmlProperty(localName = "to")
    public String getTo() {
        return to;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("to")
    @JacksonXmlProperty(localName = "to")
    public void setTo(String to) {
        this.to = to;
    }

    @JsonProperty("periods")
    @JacksonXmlProperty(localName = "period")
    public List<Period> getPeriods() {
        return periods;
    }

    @JsonProperty("periods")
    @JacksonXmlProperty(localName = "period")
    public void setPeriods(List<Period> periods) {
        this.periods = periods;
    }

    /**
     * frequencies
     * <p>
     * 
     * 
     */
    @JsonProperty("includes")
    @JacksonXmlProperty(localName = "includes")
    public Frequencies getIncludes() {
        return includes;
    }

    /**
     * frequencies
     * <p>
     * 
     * 
     */
    @JsonProperty("includes")
    @JacksonXmlProperty(localName = "includes")
    public void setIncludes(Frequencies includes) {
        this.includes = includes;
    }

    /**
     * frequencies
     * <p>
     * 
     * 
     */
    @JsonProperty("excludes")
    @JacksonXmlProperty(localName = "excludes")
    public Frequencies getExcludes() {
        return excludes;
    }

    /**
     * frequencies
     * <p>
     * 
     * 
     */
    @JsonProperty("excludes")
    @JacksonXmlProperty(localName = "excludes")
    public void setExcludes(Frequencies excludes) {
        this.excludes = excludes;
    }

    /**
     * collections of objects which use calendar
     * <p>
     * 
     * 
     */
    @JsonProperty("usedBy")
    @JacksonXmlProperty(localName = "usedBy")
    public UsedBy getUsedBy() {
        return usedBy;
    }

    /**
     * collections of objects which use calendar
     * <p>
     * 
     * 
     */
    @JsonProperty("usedBy")
    @JacksonXmlProperty(localName = "usedBy")
    public void setUsedBy(UsedBy usedBy) {
        this.usedBy = usedBy;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("path", path).append("name", name).append("basedOn", basedOn).append("type", type).append("category", category).append("title", title).append("from", from).append("to", to).append("periods", periods).append("includes", includes).append("excludes", excludes).append("usedBy", usedBy).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(excludes).append(includes).append(type).append(title).append(path).append(name).append(periods).append(from).append(id).append(to).append(category).append(basedOn).append(usedBy).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Calendar) == false) {
            return false;
        }
        Calendar rhs = ((Calendar) other);
        return new EqualsBuilder().append(excludes, rhs.excludes).append(includes, rhs.includes).append(type, rhs.type).append(title, rhs.title).append(path, rhs.path).append(name, rhs.name).append(periods, rhs.periods).append(from, rhs.from).append(id, rhs.id).append(to, rhs.to).append(category, rhs.category).append(basedOn, rhs.basedOn).append(usedBy, rhs.usedBy).isEquals();
    }

}
