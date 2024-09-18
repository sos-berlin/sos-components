package com.sos.joc.db.inventory.common;

import java.util.List;
import java.util.Set;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.inventory.IDBItemTag;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;

public interface ITagDBLayer<T extends IDBItemTag> {
    
    public List<T> getTags(Set<String> tagNames) throws DBConnectionRefusedException, DBInvalidDataException;
    
    public List<T> getAllTags() throws DBConnectionRefusedException, DBInvalidDataException;
    
    public Integer deleteTaggings(Set<String> tagNames) throws SOSHibernateException;
    
    public Integer deleteTags(Set<String> tagNames) throws SOSHibernateException;
    
    public Integer getMaxOrdering() throws DBConnectionRefusedException, DBInvalidDataException;
    
    public SOSHibernateSession getSession();

}
