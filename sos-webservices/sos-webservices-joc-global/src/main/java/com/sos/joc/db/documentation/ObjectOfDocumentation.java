package com.sos.joc.db.documentation;

import com.sos.joc.model.inventory.common.ConfigurationType;

public class ObjectOfDocumentation {
    
    private ConfigurationType type;
    private String path;
    
    public ObjectOfDocumentation(String path, String type) {
        try {
            this.type = ConfigurationType.fromValue(type);
        } catch (IllegalArgumentException e) {
            this.type = null;
        }
        this.path = path;
    }
    
    public String getPath() {
        return path;
    }
    
    public ConfigurationType getType() {
        return type;
    }

}
