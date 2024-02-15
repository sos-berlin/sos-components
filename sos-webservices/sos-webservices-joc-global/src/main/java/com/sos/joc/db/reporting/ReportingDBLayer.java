package com.sos.joc.db.reporting;

import java.util.Collections;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.db.DBLayer;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;

public class ReportingDBLayer extends DBLayer {

    private static final long serialVersionUID = 1L;
//    private Map<Long, List<DBItemInventoryTag>> map = Collections.emptyMap();
//    private List<Long> tagIdsWithObjects = Collections.emptyList();

    public ReportingDBLayer(SOSHibernateSession session) {
        super(session);
    }
    
    public List<DBItemReportHistory> getAll() throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_REPORT_HISTORY);
            Query<DBItemReportHistory> query = getSession().createQuery(sql.toString());
            List<DBItemReportHistory> result = getSession().getResultList(query);
            if (result == null) {
                return Collections.emptyList();
            }
            return result;
            
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }
}
