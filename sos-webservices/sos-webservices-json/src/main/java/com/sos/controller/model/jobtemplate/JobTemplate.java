
package com.sos.controller.model.jobtemplate;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.job.AdmissionTimeScheme;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.job.Executable;
import com.sos.inventory.model.job.JobCriticality;
import com.sos.inventory.model.job.notification.JobNotification;
import com.sos.inventory.model.jobtemplate.Parameters;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobTemplate
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "path"
})
public class JobTemplate
    extends com.sos.inventory.model.jobtemplate.JobTemplate
{

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
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;

    /**
     * No args constructor for use in serialization
     * 
     */
    public JobTemplate() {
    }

    /**
     * 
     * @param warnIfLonger
     * @param parallelism
     * @param jobResourceNames
     * @param criticality
     * @param failOnErrWritten
     * @param description
     * @param title
     * @param version
     * @param executable
     * @param timeout
     * @param warnIfShorter
     * @param admissionTimeScheme
     * @param path
     * @param notification
     * @param graceTimeout
     * @param defaultArguments
     * @param skipIfNoAdmissionForOrderDay
     * @param name
     * @param arguments
     * @param documentationName
     * @param hash
     */
    public JobTemplate(String name, String path, String version, Executable executable, AdmissionTimeScheme admissionTimeScheme, Boolean skipIfNoAdmissionForOrderDay, Integer parallelism, Integer timeout, Integer graceTimeout, Boolean failOnErrWritten, Parameters arguments, Environment defaultArguments, List<String> jobResourceNames, String title, String description, String documentationName, JobCriticality criticality, String warnIfShorter, String warnIfLonger, JobNotification notification, String hash) {
        super(version, executable, admissionTimeScheme, skipIfNoAdmissionForOrderDay, parallelism, timeout, graceTimeout, failOnErrWritten, arguments, defaultArguments, jobResourceNames, title, description, documentationName, criticality, warnIfShorter, warnIfLonger, notification, hash);
        this.name = name;
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
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("name", name).append("path", path).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(name).append(path).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobTemplate) == false) {
            return false;
        }
        JobTemplate rhs = ((JobTemplate) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(name, rhs.name).append(path, rhs.path).isEquals();
    }

}
