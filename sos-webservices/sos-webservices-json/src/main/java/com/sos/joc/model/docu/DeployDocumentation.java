
package com.sos.joc.model.docu;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.InventoryObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "documentation",
    "objects"
})
public class DeployDocumentation {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentation")
    private String documentation;
    @JsonProperty("objects")
    private List<InventoryObject> objects = new ArrayList<InventoryObject>();

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentation")
    public String getDocumentation() {
        return documentation;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentation")
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    @JsonProperty("objects")
    public List<InventoryObject> getObjects() {
        return objects;
    }

    @JsonProperty("objects")
    public void setObjects(List<InventoryObject> objects) {
        this.objects = objects;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("documentation", documentation).append("objects", objects).toString();
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
