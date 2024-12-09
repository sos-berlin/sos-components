
package com.sos.sign.model.board;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.board.BoardType;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.model.common.IDeployObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


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
    "boardType",
    "postOrderToNoticeId",
    "endOfLife",
    "expectOrderToNoticeId"
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
    private DeployType tYPE = DeployType.NOTICEBOARD;
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
     * boardType
     * <p>
     * 
     * 
     */
    @JsonIgnore
    private BoardType boardType = BoardType.fromValue("GLOBAL");
    /**
     * Expression that returns a NoticeId for the PostNotice statement.
     * 
     */
    @JsonProperty("postOrderToNoticeId")
    @JsonPropertyDescription("Expression that returns a NoticeId for the PostNotice statement.")
    @JsonAlias({
        "toNotice"
    })
    private String postOrderToNoticeId;
    /**
     * Expression that returns for the PostNotice statement the time until when the note should be valid, expressed as number of milliseconds since 1970-01-01, 0 o'clock, UTC. Then JS7 will delete the note.
     * 
     */
    @JsonProperty("endOfLife")
    @JsonPropertyDescription("Expression that returns for the PostNotice statement the time until when the note should be valid, expressed as number of milliseconds since 1970-01-01, 0 o'clock, UTC. Then JS7 will delete the note.")
    private String endOfLife;
    /**
     * Expression that returns a NoticeId for the ReadNotice statement.
     * 
     */
    @JsonProperty("expectOrderToNoticeId")
    @JsonPropertyDescription("Expression that returns a NoticeId for the ReadNotice statement.")
    @JsonAlias({
        "readingOrderToNoticeId"
    })
    private String expectOrderToNoticeId;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Board() {
    }

    /**
     * 
     * @param path
     * @param postOrderToNoticeId
     * @param expectOrderToNoticeId
     * @param tYPE
     * @param endOfLife
     */
    public Board(DeployType tYPE, String path, String postOrderToNoticeId, String endOfLife, String expectOrderToNoticeId) {
        super();
        setTYPE(tYPE);
        this.path = path;
        this.postOrderToNoticeId = postOrderToNoticeId;
        this.endOfLife = endOfLife;
        this.expectOrderToNoticeId = expectOrderToNoticeId;
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
        if (getBoardType() != null && getBoardType().equals(BoardType.PLANNABLE)) {
            return DeployType.PLANNABLEBOARD;
        } else {
            return tYPE;
        }
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
        if (tYPE != null && tYPE.equals(DeployType.PLANNABLEBOARD)) {
            setBoardType(BoardType.PLANNABLE);
        }
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
     * boardType
     * <p>
     * 
     * 
     */
    @JsonIgnore
    public BoardType getBoardType() {
        return boardType;
    }

    /**
     * boardType
     * <p>
     * 
     * 
     */
    @JsonProperty("boardType")
    public void setBoardType(BoardType boardType) {
        this.boardType = boardType;
    }

    /**
     * Expression that returns a NoticeId for the PostNotice statement.
     * 
     */
    @JsonProperty("postOrderToNoticeId")
    public String getPostOrderToNoticeId() {
        return postOrderToNoticeId;
    }

    /**
     * Expression that returns a NoticeId for the PostNotice statement.
     * 
     */
    @JsonProperty("postOrderToNoticeId")
    public void setPostOrderToNoticeId(String postOrderToNoticeId) {
        this.postOrderToNoticeId = postOrderToNoticeId;
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
     * 
     */
    @JsonProperty("expectOrderToNoticeId")
    public String getExpectOrderToNoticeId() {
        return expectOrderToNoticeId;
    }

    /**
     * Expression that returns a NoticeId for the ReadNotice statement.
     * 
     */
    @JsonProperty("expectOrderToNoticeId")
    public void setExpectOrderToNoticeId(String expectOrderToNoticeId) {
        this.expectOrderToNoticeId = expectOrderToNoticeId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("path", path).append("boardType", boardType).append("postOrderToNoticeId", postOrderToNoticeId).append("endOfLife", endOfLife).append("expectOrderToNoticeId", expectOrderToNoticeId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(boardType).append(postOrderToNoticeId).append(expectOrderToNoticeId).append(tYPE).append(endOfLife).toHashCode();
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
        return new EqualsBuilder().append(path, rhs.path).append(boardType, rhs.boardType).append(postOrderToNoticeId, rhs.postOrderToNoticeId).append(expectOrderToNoticeId, rhs.expectOrderToNoticeId).append(tYPE, rhs.tYPE).append(endOfLife, rhs.endOfLife).isEquals();
    }

}
