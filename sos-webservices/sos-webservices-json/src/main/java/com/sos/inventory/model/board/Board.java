
package com.sos.inventory.model.board;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.IInventoryObject;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.common.IDeployObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * board
 * <p>
 * deploy object with fixed property 'TYPE':'Board'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "TYPE",
    "boardType",
    "postOrderToNoticeId",
    "endOfLife",
    "expectOrderToNoticeId",
    "version",
    "title",
    "documentationName"
})
public class Board implements IInventoryObject, IConfigurationObject, IDeployObject
{

    /**
     * deployType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    private DeployType tYPE = DeployType.NOTICEBOARD;
    /**
     * boardType
     * <p>
     * 
     * 
     */
    @JsonProperty("boardType")
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
     * Expression that returns a NoticeId for the ExpectNotice statement.
     * 
     */
    @JsonProperty("expectOrderToNoticeId")
    @JsonPropertyDescription("Expression that returns a NoticeId for the ExpectNotice statement.")
    @JsonAlias({
        "readingOrderToNoticeId"
    })
    private String expectOrderToNoticeId;
    /**
     * inventory repository version
     * <p>
     * inventory repository version
     * 
     */
    @JsonProperty("version")
    @JsonPropertyDescription("inventory repository version")
    private String version = "1.7.2";
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    private String documentationName;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Board() {
    }

    /**
     * 
     * @param boardType
     * @param postOrderToNoticeId
     * @param expectOrderToNoticeId
     * @param documentationName
     * 
     * @param title
     * @param version
     * @param endOfLife
     */
    public Board(BoardType boardType, String postOrderToNoticeId, String endOfLife, String expectOrderToNoticeId, String version, String title, String documentationName) {
        super();
        this.boardType = boardType;
        this.postOrderToNoticeId = postOrderToNoticeId;
        this.endOfLife = endOfLife;
        this.expectOrderToNoticeId = expectOrderToNoticeId;
        this.version = version;
        this.title = title;
        this.documentationName = documentationName;
    }

    /**
     * deployType
     * <p>
     * 
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
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(DeployType tYPE) {
        this.tYPE = tYPE;
    }

    /**
     * boardType
     * <p>
     * 
     * 
     */
    @JsonProperty("boardType")
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
     * Expression that returns a NoticeId for the ExpectNotice statement.
     * 
     */
    @JsonProperty("expectOrderToNoticeId")
    public String getExpectOrderToNoticeId() {
        return expectOrderToNoticeId;
    }

    /**
     * Expression that returns a NoticeId for the ExpectNotice statement.
     * 
     */
    @JsonProperty("expectOrderToNoticeId")
    public void setExpectOrderToNoticeId(String expectOrderToNoticeId) {
        this.expectOrderToNoticeId = expectOrderToNoticeId;
    }

    /**
     * inventory repository version
     * <p>
     * inventory repository version
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * inventory repository version
     * <p>
     * inventory repository version
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    public String getDocumentationName() {
        return documentationName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    public void setDocumentationName(String documentationName) {
        this.documentationName = documentationName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("boardType", boardType).append("postOrderToNoticeId", postOrderToNoticeId).append("endOfLife", endOfLife).append("expectOrderToNoticeId", expectOrderToNoticeId).append("version", version).append("title", title).append("documentationName", documentationName).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(boardType).append(postOrderToNoticeId).append(expectOrderToNoticeId).append(documentationName).append(tYPE).append(title).append(version).append(endOfLife).toHashCode();
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
        return new EqualsBuilder().append(boardType, rhs.boardType).append(postOrderToNoticeId, rhs.postOrderToNoticeId).append(expectOrderToNoticeId, rhs.expectOrderToNoticeId).append(documentationName, rhs.documentationName).append(tYPE, rhs.tYPE).append(title, rhs.title).append(version, rhs.version).append(endOfLife, rhs.endOfLife).isEquals();
    }

}
