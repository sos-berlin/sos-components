
package com.sos.joc.model.xmleditor.xml2json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * xmleditor xml2json configuration answer
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "configurationJson"
})
public class Xml2JsonConfigurationAnswer {

    @JsonProperty("configurationJson")
    private String configurationJson;

    @JsonProperty("configurationJson")
    public String getConfigurationJson() {
        return configurationJson;
    }

    @JsonProperty("configurationJson")
    public void setConfigurationJson(String configurationJson) {
        this.configurationJson = configurationJson;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("configurationJson", configurationJson).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(configurationJson).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Xml2JsonConfigurationAnswer) == false) {
            return false;
        }
        Xml2JsonConfigurationAnswer rhs = ((Xml2JsonConfigurationAnswer) other);
        return new EqualsBuilder().append(configurationJson, rhs.configurationJson).isEquals();
    }

}
