
package com.sos.joc.model.xmleditor.validate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * xmleditor validate configuration error answer
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "code",
    "message",
    "line",
    "column",
    "elementName",
    "elementPosition"
})
public class ErrorMessage {

    @JsonProperty("code")
    private String code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("line")
    private Integer line;
    @JsonProperty("column")
    private Integer column;
    @JsonProperty("elementName")
    private String elementName;
    @JsonProperty("elementPosition")
    private String elementPosition;

    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    @JsonProperty("code")
    public void setCode(String code) {
        this.code = code;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("line")
    public Integer getLine() {
        return line;
    }

    @JsonProperty("line")
    public void setLine(Integer line) {
        this.line = line;
    }

    @JsonProperty("column")
    public Integer getColumn() {
        return column;
    }

    @JsonProperty("column")
    public void setColumn(Integer column) {
        this.column = column;
    }

    @JsonProperty("elementName")
    public String getElementName() {
        return elementName;
    }

    @JsonProperty("elementName")
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    @JsonProperty("elementPosition")
    public String getElementPosition() {
        return elementPosition;
    }

    @JsonProperty("elementPosition")
    public void setElementPosition(String elementPosition) {
        this.elementPosition = elementPosition;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("code", code).append("message", message).append("line", line).append("column", column).append("elementName", elementName).append("elementPosition", elementPosition).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(code).append(line).append(column).append(elementPosition).append(message).append(elementName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ErrorMessage) == false) {
            return false;
        }
        ErrorMessage rhs = ((ErrorMessage) other);
        return new EqualsBuilder().append(code, rhs.code).append(line, rhs.line).append(column, rhs.column).append(elementPosition, rhs.elementPosition).append(message, rhs.message).append(elementName, rhs.elementName).isEquals();
    }

}
