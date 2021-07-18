
package com.sos.sign.model.board;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.model.common.IDeployObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Board
 * <p>
 * deploy object with fixed property 'TYPE':'Board'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "TYPE",
    "path",
    "toNotice",
    "endOfLife",
    "readingOrderToNoticeId"
})
public class Board implements IDeployObject
{

    /**
     * deployType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    private DeployType tYPE = DeployType.BOARD;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    private String path;
    /**
     * Expression that returns a NoticeId for the PostNotice statement.
     * (Required)
     * 
     */
    @JsonProperty("toNotice")
    @JsonPropertyDescription("Expression that returns a NoticeId for the PostNotice statement.")
    private String toNotice;
    /**
     * Expression that returns for the PostNotice statement the time until when the note should be valid, expressed as number of milliseconds since 1970-01-01, 0 o'clock, UTC. Then JS7 will delete the note.
     * 
     */
    @JsonProperty("endOfLife")
    @JsonPropertyDescription("Expression that returns for the PostNotice statement the time until when the note should be valid, expressed as number of milliseconds since 1970-01-01, 0 o'clock, UTC. Then JS7 will delete the note.")
    private String endOfLife;
    /**
     * Expression that returns a NoticeId for the ReadNotice statement.
     * (Required)
     * 
     */
    @JsonProperty("readingOrderToNoticeId")
    @JsonPropertyDescription("Expression that returns a NoticeId for the ReadNotice statement.")
    private String readingOrderToNoticeId;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Board() {
    }

    /**
     * 
     * @param path
     * @param toNotice
     * @param tYPE
     * @param readingOrderToNoticeId
     * @param endOfLife
     */
    public Board(DeployType tYPE, String path, String toNotice, String endOfLife, String readingOrderToNoticeId) {
        super();
        this.tYPE = tYPE;
        this.path = path;
        this.toNotice = toNotice;
        this.endOfLife = endOfLife;
        this.readingOrderToNoticeId = readingOrderToNoticeId;
    }

    /**
     * deployType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public DeployType getTYPE() {
        return tYPE;
    }

    /**
     * deployType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(DeployType tYPE) {
        this.tYPE = tYPE;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Expression that returns a NoticeId for the PostNotice statement.
     * (Required)
     * 
     */
    @JsonProperty("toNotice")
    public String getToNotice() {
        return toNotice;
    }

    /**
     * Expression that returns a NoticeId for the PostNotice statement.
     * (Required)
     * 
     */
    @JsonProperty("toNotice")
    public void setToNotice(String toNotice) {
        this.toNotice = toNotice;
    }

    /**
     * Expression that returns for the PostNotice statement the time until when the note should be valid, expressed as number of milliseconds since 1970-01-01, 0 o'clock, UTC. Then JS7 will delete the note.
     * 
     */
    @JsonProperty("endOfLife")
    public String getEndOfLife() {
        return endOfLife;
    }

    /**
     * Expression that returns for the PostNotice statement the time until when the note should be valid, expressed as number of milliseconds since 1970-01-01, 0 o'clock, UTC. Then JS7 will delete the note.
     * 
     */
    @JsonProperty("endOfLife")
    public void setEndOfLife(String endOfLife) {
        this.endOfLife = endOfLife;
    }

    /**
     * Expression that returns a NoticeId for the ReadNotice statement.
     * (Required)
     * 
     */
    @JsonProperty("readingOrderToNoticeId")
    public String getReadingOrderToNoticeId() {
        return readingOrderToNoticeId;
    }

    /**
     * Expression that returns a NoticeId for the ReadNotice statement.
     * (Required)
     * 
     */
    @JsonProperty("readingOrderToNoticeId")
    public void setReadingOrderToNoticeId(String readingOrderToNoticeId) {
        this.readingOrderToNoticeId = readingOrderToNoticeId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("path", path).append("toNotice", toNotice).append("endOfLife", endOfLife).append("readingOrderToNoticeId", readingOrderToNoticeId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(toNotice).append(tYPE).append(readingOrderToNoticeId).append(endOfLife).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Board) == false) {
            return false;
        }
        Board rhs = ((Board) other);
        return new EqualsBuilder().append(path, rhs.path).append(toNotice, rhs.toNotice).append(tYPE, rhs.tYPE).append(readingOrderToNoticeId, rhs.readingOrderToNoticeId).append(endOfLife, rhs.endOfLife).isEquals();
    }

}
