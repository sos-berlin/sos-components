
package com.sos.joc.model.wizard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * job
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "docPath",
    "docName",
    "title",
    "javaClass",
    "params"
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("docName")
    private String docName;
    @JsonProperty("title")
    private String title;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("javaClass")
    private String javaClass;
    @JsonProperty("params")
    private List<Param> params = new ArrayList<Param>();

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
     * 
     * (Required)
     * 
     */
    @JsonProperty("docName")
    public String getDocName() {
        return docName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("docName")
    public void setDocName(String docName) {
        this.docName = docName;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("javaClass")
    public String getJavaClass() {
        return javaClass;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("javaClass")
    public void setJavaClass(String javaClass) {
        this.javaClass = javaClass;
    }

    @JsonProperty("params")
    public List<Param> getParams() {
        return params;
    }

    @JsonProperty("params")
    public void setParams(List<Param> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("docPath", docPath).append("docName", docName).append("title", title).append("javaClass", javaClass).append("params", params).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(docName).append(javaClass).append(deliveryDate).append(title).append(params).append(docPath).toHashCode();
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
        return new EqualsBuilder().append(docName, rhs.docName).append(javaClass, rhs.javaClass).append(deliveryDate, rhs.deliveryDate).append(title, rhs.title).append(params, rhs.params).append(docPath, rhs.docPath).isEquals();
    }

}
