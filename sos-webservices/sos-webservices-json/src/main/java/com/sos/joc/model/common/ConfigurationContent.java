
package com.sos.joc.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * configuration content
 * <p>
 * A parameter can specify if the content is xml or html. Either 'xml' or 'html' is required
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "xml",
    "html"
})
public class ConfigurationContent {

    @JsonProperty("xml")
    private String xml;
    @JsonProperty("html")
    private String html;

    @JsonProperty("xml")
    public String getXml() {
        return xml;
    }

    @JsonProperty("xml")
    public void setXml(String xml) {
        this.xml = xml;
    }

    @JsonProperty("html")
    public String getHtml() {
        return html;
    }

    @JsonProperty("html")
    public void setHtml(String html) {
        this.html = html;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("xml", xml).append("html", html).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(xml).append(html).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConfigurationContent) == false) {
            return false;
        }
        ConfigurationContent rhs = ((ConfigurationContent) other);
        return new EqualsBuilder().append(xml, rhs.xml).append(html, rhs.html).isEquals();
    }

}
