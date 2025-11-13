
package com.sos.joc.model.note.common;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * post
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "postId",
    "content",
    "severity",
    "author",
    "posted"
})
public class Post {

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("postId")
    private Integer postId;
    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("content")
    private String content;
    /**
     * note/post severity
     * <p>
     * 
     * 
     */
    @JsonProperty("severity")
    private Severity severity = Severity.fromValue("NORMAL");
    /**
     * author/user
     * <p>
     * 
     * 
     */
    @JsonProperty("author")
    private Author author;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("posted")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date posted;

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("postId")
    public Integer getPostId() {
        return postId;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("postId")
    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("content")
    public String getContent() {
        return content;
    }

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("content")
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * note/post severity
     * <p>
     * 
     * 
     */
    @JsonProperty("severity")
    public Severity getSeverity() {
        return severity;
    }

    /**
     * note/post severity
     * <p>
     * 
     * 
     */
    @JsonProperty("severity")
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    /**
     * author/user
     * <p>
     * 
     * 
     */
    @JsonProperty("author")
    public Author getAuthor() {
        return author;
    }

    /**
     * author/user
     * <p>
     * 
     * 
     */
    @JsonProperty("author")
    public void setAuthor(Author author) {
        this.author = author;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("posted")
    public Date getPosted() {
        return posted;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("posted")
    public void setPosted(Date posted) {
        this.posted = posted;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("postId", postId).append("content", content).append("severity", severity).append("author", author).append("posted", posted).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(severity).append(postId).append(content).append(author).append(posted).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Post) == false) {
            return false;
        }
        Post rhs = ((Post) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(postId, rhs.postId).append(content, rhs.content).append(author, rhs.author).append(posted, rhs.posted).isEquals();
    }

}
