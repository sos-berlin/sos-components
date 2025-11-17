
package com.sos.joc.model.note.common;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * note
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "noteId",
    "path",
    "metadata",
    "posts",
    "participants"
})
public class Note
    extends NoteIdentifier
{

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("noteId")
    private Long noteId;
    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;
    /**
     * metadata
     * <p>
     * 
     * 
     */
    @JsonProperty("metadata")
    private Metadata metadata;
    @JsonProperty("posts")
    private List<Post> posts = new ArrayList<Post>();
    @JsonProperty("participants")
    private List<Participant> participants = new ArrayList<Participant>();

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("noteId")
    public Long getNoteId() {
        return noteId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("noteId")
    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * metadata
     * <p>
     * 
     * 
     */
    @JsonProperty("metadata")
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * metadata
     * <p>
     * 
     * 
     */
    @JsonProperty("metadata")
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @JsonProperty("posts")
    public List<Post> getPosts() {
        return posts;
    }

    @JsonProperty("posts")
    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    @JsonProperty("participants")
    public List<Participant> getParticipants() {
        return participants;
    }

    @JsonProperty("participants")
    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("noteId", noteId).append("path", path).append("metadata", metadata).append("posts", posts).append("participants", participants).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(path).append(noteId).append(metadata).append(posts).append(participants).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Note) == false) {
            return false;
        }
        Note rhs = ((Note) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(path, rhs.path).append(noteId, rhs.noteId).append(metadata, rhs.metadata).append(posts, rhs.posts).append(participants, rhs.participants).isEquals();
    }

}
