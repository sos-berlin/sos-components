package com.sos.auth.classes;

import java.util.Map;

public class SOSLockerContent {
   private Long created;
   private Map<String, String> content;

public Long getCreated() {
    return created;
}

public void setCreated(Long created) {
    this.created = created;
}

public Map<String, String> getContent() {
    return content;
}

public void setContent(Map<String, String> content) {
    this.content = content;
}

}
