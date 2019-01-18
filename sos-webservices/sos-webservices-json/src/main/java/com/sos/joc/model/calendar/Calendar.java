
package com.sos.joc.model.calendar;

import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "path",
    "name",
    "basedOn",
    "documentation",
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
    private Long id;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    private String path;
    @JsonProperty("name")
    private String name;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("basedOn")
    private String basedOn;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("documentation")
    private String documentation;
    /**
     * calendar type
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    private CalendarType type = CalendarType.fromValue("WORKING_DAYS");
    @JsonProperty("category")
    private String category;
    @JsonProperty("title")
    private String title;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("from")
    private String from;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("to")
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
     * collections of objects which use calendar
     * <p>
     * 
     * 
     */
    @JsonProperty("usedBy")
    private UsedBy usedBy;

    /**
     * non negative long
     * <p>
     * 
     * 
     * @return
     *     The id
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
     * @param id
     *     The id
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     * @return
     *     The path
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     * @param path
     *     The path
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 
     * @return
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     * @return
     *     The basedOn
     */
    @JsonProperty("basedOn")
    public String getBasedOn() {
        return basedOn;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     * @param basedOn
     *     The basedOn
     */
    @JsonProperty("basedOn")
    public void setBasedOn(String basedOn) {
        this.basedOn = basedOn;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     * @return
     *     The documentation
     */
    @JsonProperty("documentation")
    public String getDocumentation() {
        return documentation;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     * @param documentation
     *     The documentation
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
     * @return
     *     The type
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
     * @param type
     *     The type
     */
    @JsonProperty("type")
    public void setType(CalendarType type) {
        this.type = type;
    }

    /**
     * 
     * @return
     *     The category
     */
    @JsonProperty("category")
    public String getCategory() {
        return category;
    }

    /**
     * 
     * @param category
     *     The category
     */
    @JsonProperty("category")
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * 
     * @return
     *     The title
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * 
     * @param title
     *     The title
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
     * @return
     *     The from
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
     * @param from
     *     The from
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
     * @return
     *     The to
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
     * @param to
     *     The to
     */
    @JsonProperty("to")
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * 
     * @return
     *     The periods
     */
    @JsonProperty("periods")
    public List<Period> getPeriods() {
        return periods;
    }

    /**
     * 
     * @param periods
     *     The periods
     */
    @JsonProperty("periods")
    public void setPeriods(List<Period> periods) {
        this.periods = periods;
    }

    /**
     * frequencies
     * <p>
     * 
     * 
     * @return
     *     The includes
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
     * @param includes
     *     The includes
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
     * @return
     *     The excludes
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
     * @param excludes
     *     The excludes
     */
    @JsonProperty("excludes")
    public void setExcludes(Frequencies excludes) {
        this.excludes = excludes;
    }

    /**
     * collections of objects which use calendar
     * <p>
     * 
     * 
     * @return
     *     The usedBy
     */
    @JsonProperty("usedBy")
    public UsedBy getUsedBy() {
        return usedBy;
    }

    /**
     * collections of objects which use calendar
     * <p>
     * 
     * 
     * @param usedBy
     *     The usedBy
     */
    @JsonProperty("usedBy")
    public void setUsedBy(UsedBy usedBy) {
        this.usedBy = usedBy;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(path).append(name).append(basedOn).append(documentation).append(type).append(category).append(title).append(from).append(to).append(periods).append(includes).append(excludes).append(usedBy).toHashCode();
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
        return new EqualsBuilder().append(id, rhs.id).append(path, rhs.path).append(name, rhs.name).append(basedOn, rhs.basedOn).append(documentation, rhs.documentation).append(type, rhs.type).append(category, rhs.category).append(title, rhs.title).append(from, rhs.from).append(to, rhs.to).append(periods, rhs.periods).append(includes, rhs.includes).append(excludes, rhs.excludes).append(usedBy, rhs.usedBy).isEquals();
    }

}
