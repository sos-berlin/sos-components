package com.sos.joc.db.inventory.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.note.Notification;
import com.sos.joc.model.note.common.Severity;

public class InventoryNoteItem extends Notification {
    
    @JsonIgnore
    private Integer type;
    @JsonIgnore
    private Integer color;
    @JsonIgnore
    private String folder;

    @JsonIgnore
    public Integer getType() {
        return type;
    }
    
    private ConfigurationType getTypeAsEnum() {
        if (type == null) {
           return null; 
        }
        try {
            return ConfigurationType.fromValue(type);
        } catch (Exception e) {
            return null;
        }
    }

    public void setType(Integer val) {
        type = val;
        setObjectType(getTypeAsEnum());
    }
    
    @JsonIgnore
    public Integer getColor() {
        return color;
    }
    
    private Severity getColorAsEnum() {
        if (color == null) {
            return null; 
         }
        try {
            return Severity.fromValue(color);
        } catch (Exception e) {
            return null;
        }
    }

    public void setColor(Integer val) {
        color = val;
        setSeverity(getColorAsEnum());
    }

    @JsonIgnore
    public String getFolder() {
        return folder;
    }

    public void setFolder(String val) {
        folder = val;
    }

}
