package com.sos.joc.classes.tag;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.sos.commons.util.SOSCheckJavaVariableName;

public class GroupedTag {

    private static final String GROUP_DELIMITER = ":";
    private static final String GROUPS_DELIMITER = ";";
    private static final String TAGS_DELIMITER = ",";

    private final String tag;
    private Optional<String> group;

    public GroupedTag(String tagWithOptionalGroup) {
        String[] s = tagWithOptionalGroup.split(GROUP_DELIMITER, 2);
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

    public boolean hasTag() {
        return tag != null;
    }

    public boolean hasGroup() {
        return group.isPresent();
    }

    public void checkJavaNameRules() {
        SOSCheckJavaVariableName.test("tag name: ", tag);
        group.ifPresent(g -> SOSCheckJavaVariableName.test("group name: ", g));
    }
    
    public boolean testJavaNameRules() {
        boolean test = SOSCheckJavaVariableName.test(tag);
        if (test && group.isPresent()) {
            test = SOSCheckJavaVariableName.test(group.get()); 
        }
        return test;
    }

    @Override
    public String toString() {
        return group.map(g -> g + GROUP_DELIMITER + tag).orElse(tag);
    }

    public static String toString(List<GroupedTag> val) {
        if (val == null) {
            return "";
        }

        Map<String, List<String>> grouped = val.stream().filter(GroupedTag::hasTag).collect(Collectors.groupingBy(gt -> gt.getGroup().orElse(""),
                LinkedHashMap::new, Collectors.mapping(GroupedTag::getTag, Collectors.toList())));
        return grouped.entrySet().stream().map(entry -> {
            String groupName = entry.getKey();
            List<String> tags = entry.getValue();
            if (!groupName.isEmpty()) {
                return groupName + GROUP_DELIMITER + String.join(TAGS_DELIMITER, tags);
            } else {
                return String.join(TAGS_DELIMITER, tags);
            }
        }).collect(Collectors.joining(GROUPS_DELIMITER));
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
