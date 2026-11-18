
package com.sos.jitl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "category",
    "language",
    "class"
})
public class JobDefinition {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    @JsonProperty("category")
    private String category;
    @JsonProperty("language")
    private String language = "Java";
    @JsonProperty("class")
    private String clazz;

    /**
     * No args constructor for use in serialization
     * 
     */
    public JobDefinition() {
    }

    /**
     * 
     * @param name
     * @param language
     * @param clazz
     * @param category
     */
    public JobDefinition(String name, String category, String language, String clazz) {
        super();
        this.name = name;
        this.category = category;
        this.language = language;
        this.clazz = clazz;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("category")
    public String getCategory() {
        return category;
    }

    @JsonProperty("category")
    public void setCategory(String category) {
        this.category = category;
    }

    @JsonProperty("language")
    public String getLanguage() {
        return language;
    }

    @JsonProperty("language")
    public void setLanguage(String language) {
        this.language = language;
    }

    @JsonProperty("class")
    public String getClazz() {
        return clazz;
    }

    @JsonProperty("class")
    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("category", category).append("language", language).append("class", clazz).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(language).append(clazz).append(category).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobDefinition) == false) {
            return false;
        }
        JobDefinition rhs = ((JobDefinition) other);
        return new EqualsBuilder().append(name, rhs.name).append(language, rhs.language).append(clazz, rhs.clazz).append(category, rhs.category).isEquals();
    }

}
