
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
    "id",
    "endOfLife"
})
public class PostNotice {

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
    @JsonProperty("endOfLife")
    private String endOfLife;

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

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("endOfLife")
    public String getEndOfLife() {
        return endOfLife;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("endOfLife")
    public void setEndOfLife(String endOfLife) {
        this.endOfLife = endOfLife;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("boardName", boardName).append("id", id).append("endOfLife", endOfLife).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(boardName).append(endOfLife).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PostNotice) == false) {
            return false;
        }
        PostNotice rhs = ((PostNotice) other);
        return new EqualsBuilder().append(id, rhs.id).append(boardName, rhs.boardName).append(endOfLife, rhs.endOfLife).isEquals();
    }

}
