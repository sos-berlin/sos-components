package com.sos.joc.classes.tag;

import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.sos.commons.util.SOSCheckJavaVariableName;

public class GroupedTag {
    
    private final String tag;
    private Optional<String> group;
    
    public GroupedTag(String tagWithOptionalGroup) {
        String[] s = tagWithOptionalGroup.split(":", 2);
        if (s.length == 1) {
            group = Optional.empty();
            tag = s[0];
        } else {
            group = Optional.of(s[0]);
            tag = s[1];
        }
    }
    
    public GroupedTag(String group, String tag) {
        this.group = Optional.ofNullable(group);
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
    
    public Optional<String> getGroup() {
        return group;
    }
    
    public void setGroup(String group) {
        this.group = Optional.ofNullable(group);
    }
    
    public String getNonEmptyGroup() {
        return group.get();
    }

    public boolean hasGroup() {
        return group.isPresent();
    }
    
    public void checkJavaNameRules() {
        SOSCheckJavaVariableName.test("tag name: ", tag);
        group.ifPresent(g -> SOSCheckJavaVariableName.test("group name: ", g)); 
    }
    
    @Override
    public String toString() {
        return group.map(g -> g + ":" + tag).orElse(tag);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tag).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GroupedTag) == false) {
            return false;
        }
        GroupedTag rhs = ((GroupedTag) other);
        return new EqualsBuilder().append(tag, rhs.tag).isEquals();
    }
}
