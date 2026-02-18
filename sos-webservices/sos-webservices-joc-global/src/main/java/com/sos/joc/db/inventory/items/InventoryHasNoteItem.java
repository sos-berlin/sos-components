package com.sos.joc.db.inventory.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.model.note.common.HasNote;
import com.sos.joc.model.note.common.Severity;

public class InventoryHasNoteItem extends HasNote {
    
    @JsonIgnore
    private Integer color;
    @JsonIgnore
    private String objectName;
//    @JsonIgnore
//    private String accountName;

    @JsonIgnore
    public Integer getColor() {
        return color;
    }
    
    @JsonIgnore
    public Severity getColorAsEnum() {
        return Severity.fromValueOrNull(color);
    }

    public void setColor(Integer val) {
        color = val;
        setSeverity(getColorAsEnum());
    }
    
    @JsonIgnore
    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String val) {
        objectName = val;
    }
    
//    @JsonIgnore
//    public String getAccountName() {
//        return accountName;
//    }
//
//    public void setAccountName(String val) {
//        accountName = val;
//    }
}
