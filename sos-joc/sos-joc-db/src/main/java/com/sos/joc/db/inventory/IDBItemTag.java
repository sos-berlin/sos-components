package com.sos.joc.db.inventory;

import java.time.LocalDateTime;

public interface IDBItemTag {

    public Long getId();

    public void setId(Long var);

    public String getName();

    public void setName(String var);

    public Long getGroupId();

    public void setGroupId(Long var);

    public Integer getOrdering();

    public void setOrdering(Integer val);

    public LocalDateTime getModified();
}
