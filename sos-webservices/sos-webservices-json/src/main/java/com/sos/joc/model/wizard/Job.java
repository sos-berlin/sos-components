
package com.sos.joc.model.wizard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.jobtemplate.Parameters;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * job wizard
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "docPath",
    "docName",
    "assignReference",
    "title",
    "javaClass",
    "params",
    "arguments"
})
public class Job {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("docPath")
    @JsonPropertyDescription("absolute path of an object.")
    private String docPath;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("docName")
    private String docName;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("assignReference")
    private String assignReference;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("javaClass")
    private String javaClass;
    /**
     * deprecated: use 'arguments'
     * 
     */
    @JsonProperty("params")
    @JsonPropertyDescription("deprecated: use 'arguments'")
    private List<Param> params = new ArrayList<Param>();
    /**
     * parameters
     * <p>
     * 
     * 
     */
    @JsonProperty("arguments")
    private Parameters arguments;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("docPath")
    public String getDocPath() {
        return docPath;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("docPath")
    public void setDocPath(String docPath) {
        this.docPath = docPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("docName")
    public String getDocName() {
        return docName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("docName")
    public void setDocName(String docName) {
        this.docName = docName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("assignReference")
    public String getAssignReference() {
        return assignReference;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("assignReference")
    public void setAssignReference(String assignReference) {
        this.assignReference = assignReference;
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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("javaClass")
    public String getJavaClass() {
        return javaClass;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("javaClass")
    public void setJavaClass(String javaClass) {
        this.javaClass = javaClass;
    }

    /**
     * deprecated: use 'arguments'
     * 
     */
    @JsonProperty("params")
    public List<Param> getParams() {
        return params;
    }

    /**
     * deprecated: use 'arguments'
     * 
     */
    @JsonProperty("params")
    public void setParams(List<Param> params) {
        this.params = params;
    }

    /**
     * parameters
     * <p>
     * 
     * 
     */
    @JsonProperty("arguments")
    public Parameters getArguments() {
        return arguments;
    }

    /**
     * parameters
     * <p>
     * 
     * 
     */
    @JsonProperty("arguments")
    public void setArguments(Parameters arguments) {
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("docPath", docPath).append("docName", docName).append("assignReference", assignReference).append("title", title).append("javaClass", javaClass).append("params", params).append("arguments", arguments).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(assignReference).append(docName).append(javaClass).append(arguments).append(deliveryDate).append(title).append(params).append(docPath).toHashCode();
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
        return new EqualsBuilder().append(assignReference, rhs.assignReference).append(docName, rhs.docName).append(javaClass, rhs.javaClass).append(arguments, rhs.arguments).append(deliveryDate, rhs.deliveryDate).append(title, rhs.title).append(params, rhs.params).append(docPath, rhs.docPath).isEquals();
    }

}
