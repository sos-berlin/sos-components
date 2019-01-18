
package com.sos.joc.model.docu;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.JobSchedulerObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "documentation",
    "objects"
})
public class DeployDocumentation {

    @JsonProperty("documentation")
    private String documentation;
    @JsonProperty("objects")
    private List<JobSchedulerObject> objects = new ArrayList<JobSchedulerObject>();

    /**
     * 
     * @return
     *     The documentation
     */
    @JsonProperty("documentation")
    public String getDocumentation() {
        return documentation;
    }

    /**
     * 
     * @param documentation
     *     The documentation
     */
    @JsonProperty("documentation")
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    /**
     * 
     * @return
     *     The objects
     */
    @JsonProperty("objects")
    public List<JobSchedulerObject> getObjects() {
        return objects;
    }

    /**
     * 
     * @param objects
     *     The objects
     */
    @JsonProperty("objects")
    public void setObjects(List<JobSchedulerObject> objects) {
        this.objects = objects;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(documentation).append(objects).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployDocumentation) == false) {
            return false;
        }
        DeployDocumentation rhs = ((DeployDocumentation) other);
        return new EqualsBuilder().append(documentation, rhs.documentation).append(objects, rhs.objects).isEquals();
    }

}
