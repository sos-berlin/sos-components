
package com.sos.joc.model.docu;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * documentation
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "jobschedulerId",
    "documentations"
})
public class DeployDocumentations {

    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("documentations")
    private List<DeployDocumentation> documentations = new ArrayList<DeployDocumentation>();

    /**
     * 
     * @return
     *     The jobschedulerId
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * @param jobschedulerId
     *     The jobschedulerId
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * 
     * @return
     *     The documentations
     */
    @JsonProperty("documentations")
    public List<DeployDocumentation> getDocumentations() {
        return documentations;
    }

    /**
     * 
     * @param documentations
     *     The documentations
     */
    @JsonProperty("documentations")
    public void setDocumentations(List<DeployDocumentation> documentations) {
        this.documentations = documentations;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobschedulerId).append(documentations).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployDocumentations) == false) {
            return false;
        }
        DeployDocumentations rhs = ((DeployDocumentations) other);
        return new EqualsBuilder().append(jobschedulerId, rhs.jobschedulerId).append(documentations, rhs.documentations).isEquals();
    }

}
