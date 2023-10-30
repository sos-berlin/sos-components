
package com.sos.joc.model.docu;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * documentation
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "documentations"
})
public class DeployDocumentations {

    @JsonProperty("documentations")
    private List<DeployDocumentation> documentations = new ArrayList<DeployDocumentation>();

    @JsonProperty("documentations")
    public List<DeployDocumentation> getDocumentations() {
        return documentations;
    }

    @JsonProperty("documentations")
    public void setDocumentations(List<DeployDocumentation> documentations) {
        this.documentations = documentations;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("documentations", documentations).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(documentations).toHashCode();
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
        return new EqualsBuilder().append(documentations, rhs.documentations).isEquals();
    }

}
