package com.sos.auth.classes;

import java.util.Map;

public class SOSLockerContent {
   private Long created;
   private Map<String, Object> content;

public Long getCreated() {
    return created;
}

public void setCreated(Long created) {
    this.created = created;
}

public Map<String, Object> getContent() {
    return content;
}

public void setContent(Map<String, Object> content) {
    this.content = content;
}

}
