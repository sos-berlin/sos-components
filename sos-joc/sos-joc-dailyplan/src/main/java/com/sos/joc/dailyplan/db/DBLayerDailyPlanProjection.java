package com.sos.joc.dailyplan.db;

import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.dailyplan.DBItemDailyPlanProjection;
import com.sos.joc.model.dailyplan.projection.items.YearItem;

public class DBLayerDailyPlanProjection extends DBLayer {

    private static final long serialVersionUID = 1L;

    public DBLayerDailyPlanProjection(SOSHibernateSession session) {
        super(session);
    }

    public int cleanup() throws SOSHibernateException {
        return getSession().executeUpdate("delete from " + DBITEM_DPL_PROJECTIONS);
    }

    public void insert(int year, YearItem yearItem) throws Exception {
        if (yearItem == null) {
            return;
        }

        DBItemDailyPlanProjection item = new DBItemDailyPlanProjection();
        item.setId(Long.valueOf(year));
        item.setContent(Globals.objectMapper.writeValueAsString(yearItem));
        item.setCreated(new Date());

        getSession().save(item);
    }

}