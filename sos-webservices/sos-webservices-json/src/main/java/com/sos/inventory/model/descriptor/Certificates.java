
package com.sos.inventory.model.descriptor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "controller",
    "joc"
})
public class Certificates {

    @JsonProperty("controller")
    private Controller controller;
    @JsonProperty("joc")
    private Joc joc;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Certificates() {
    }

    /**
     * 
     * @param controller
     * @param joc
     */
    public Certificates(Controller controller, Joc joc) {
        super();
        this.controller = controller;
        this.joc = joc;
    }

    @JsonProperty("controller")
    public Controller getController() {
        return controller;
    }

    @JsonProperty("controller")
    public void setController(Controller controller) {
        this.controller = controller;
    }

    @JsonProperty("joc")
    public Joc getJoc() {
        return joc;
    }

    @JsonProperty("joc")
    public void setJoc(Joc joc) {
        this.joc = joc;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controller", controller).append("joc", joc).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controller).append(joc).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Certificates) == false) {
            return false;
        }
        Certificates rhs = ((Certificates) other);
        return new EqualsBuilder().append(controller, rhs.controller).append(joc, rhs.joc).isEquals();
    }

}
