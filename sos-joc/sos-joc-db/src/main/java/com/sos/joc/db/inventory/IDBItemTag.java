package com.sos.joc.db.inventory;

import java.util.Date;

public interface IDBItemTag {
    
    public Long getId();
    public void setId(Long var);
    
    public String getName();
    public void setName(String var);
    
    public Long getGroupId();
    public void setGroupId(Long var);
    
    public Integer getOrdering();
    public void setOrdering(Integer val);
    
    public void setModified(Date val);
}
