
package com.sos.joc.model.calendar;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.ICalendarObject;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.common.CalendarType;
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
    "documentation",
    "type",
    "title",
    "from",
    "to",
    "periods",
    "includes",
    "excludes"
})
public class Calendar implements ICalendarObject, IConfigurationObject
{

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    private Long id;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String path;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("basedOn")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String basedOn;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("documentation")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String documentation;
    /**
     * calendar type
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    private CalendarType type;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("from")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String from;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("to")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String to;
    @JsonProperty("periods")
    private List<Period> periods = null;
    /**
     * frequencies
     * <p>
     * 
     * 
     */
    @JsonProperty("includes")
    private Frequencies includes;
    /**
     * frequencies
     * <p>
     * 
     * 
     */
    @JsonProperty("excludes")
    private Frequencies excludes;

    /**
     * non negative long
     * <p>
     * 
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
     * 
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
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
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("basedOn")
    public String getBasedOn() {
        return basedOn;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("basedOn")
    public void setBasedOn(String basedOn) {
        this.basedOn = basedOn;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("documentation")
    public String getDocumentation() {
        return documentation;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("documentation")
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    /**
     * calendar type
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
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
    public void setType(CalendarType type) {
        this.type = type;
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
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("from")
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
    public void setTo(String to) {
        this.to = to;
    }

    @JsonProperty("periods")
    public List<Period> getPeriods() {
        return periods;
    }

    @JsonProperty("periods")
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
    public void setExcludes(Frequencies excludes) {
        this.excludes = excludes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("path", path).append("name", name).append("basedOn", basedOn).append("documentation", documentation).append("type", type).append("title", title).append("from", from).append("to", to).append("periods", periods).append("includes", includes).append("excludes", excludes).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(excludes).append(documentation).append(includes).append(type).append(title).append(path).append(name).append(periods).append(from).append(id).append(to).append(basedOn).toHashCode();
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
        return new EqualsBuilder().append(excludes, rhs.excludes).append(documentation, rhs.documentation).append(includes, rhs.includes).append(type, rhs.type).append(title, rhs.title).append(path, rhs.path).append(name, rhs.name).append(periods, rhs.periods).append(from, rhs.from).append(id, rhs.id).append(to, rhs.to).append(basedOn, rhs.basedOn).isEquals();
    }

}
