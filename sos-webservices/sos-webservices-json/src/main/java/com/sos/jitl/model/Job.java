
package com.sos.jitl.model;

import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * job
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "category",
    "language",
    "class",
    "arguments",
    "returnVariables"
})
public class Job {

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
    private String _class;
    @JsonProperty("arguments")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Param> arguments = null;
    @JsonProperty("returnVariables")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Param> returnVariables = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Job() {
    }

    /**
     * 
     * @param returnVariables
     * @param name
     * @param language
     * @param arguments
     * @param _class
     * @param category
     */
    public Job(String name, String category, String language, String _class, Set<Param> arguments, Set<Param> returnVariables) {
        super();
        this.name = name;
        this.category = category;
        this.language = language;
        this._class = _class;
        this.arguments = arguments;
        this.returnVariables = returnVariables;
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
    public String getClass_() {
        return _class;
    }

    @JsonProperty("class")
    public void setClass_(String _class) {
        this._class = _class;
    }

    @JsonProperty("arguments")
    public Set<Param> getArguments() {
        return arguments;
    }

    @JsonProperty("arguments")
    public void setArguments(Set<Param> arguments) {
        this.arguments = arguments;
    }

    @JsonProperty("returnVariables")
    public Set<Param> getReturnVariables() {
        return returnVariables;
    }

    @JsonProperty("returnVariables")
    public void setReturnVariables(Set<Param> returnVariables) {
        this.returnVariables = returnVariables;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("category", category).append("language", language).append("_class", _class).append("arguments", arguments).append("returnVariables", returnVariables).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(returnVariables).append(name).append(language).append(arguments).append(_class).append(category).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Job) == false) {
            return false;
        }
        Job rhs = ((Job) other);
        return new EqualsBuilder().append(returnVariables, rhs.returnVariables).append(name, rhs.name).append(language, rhs.language).append(arguments, rhs.arguments).append(_class, rhs._class).append(category, rhs.category).isEquals();
    }

}
