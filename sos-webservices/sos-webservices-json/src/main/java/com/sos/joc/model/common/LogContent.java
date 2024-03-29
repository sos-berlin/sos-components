
package com.sos.joc.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * log content
 * <p>
 * The parameter 'mime' can specify if the content is plain or html. Either 'plain' or 'html' is required. 'plain' is default.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "plain",
    "html"
})
public class LogContent {

    @JsonProperty("plain")
    private String plain;
    @JsonProperty("html")
    private String html;

    @JsonProperty("plain")
    public String getPlain() {
        return plain;
    }

    @JsonProperty("plain")
    public void setPlain(String plain) {
        this.plain = plain;
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
        return new ToStringBuilder(this).append("plain", plain).append("html", html).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(plain).append(html).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LogContent) == false) {
            return false;
        }
        LogContent rhs = ((LogContent) other);
        return new EqualsBuilder().append(plain, rhs.plain).append(html, rhs.html).isEquals();
    }

}
