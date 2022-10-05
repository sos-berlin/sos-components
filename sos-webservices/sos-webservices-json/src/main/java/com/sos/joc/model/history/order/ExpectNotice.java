
package com.sos.joc.model.history.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "boardName",
    "id"
})
public class ExpectNotice {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("boardName")
    private String boardName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    private String id;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("boardName")
    public String getBoardName() {
        return boardName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("boardName")
    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("boardName", boardName).append("id", id).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(boardName).append(id).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExpectNotice) == false) {
            return false;
        }
        ExpectNotice rhs = ((ExpectNotice) other);
        return new EqualsBuilder().append(boardName, rhs.boardName).append(id, rhs.id).isEquals();
    }

}
