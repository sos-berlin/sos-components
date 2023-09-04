package com.sos.joc.dailyplan.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.dailyplan.DBItemDailyPlanProjection;
import com.sos.joc.model.dailyplan.projections.items.meta.MetaItem;
import com.sos.joc.model.dailyplan.projections.items.year.YearsItem;

public class DBLayerDailyPlanProjections extends DBLayer {

    private static final long serialVersionUID = 1L;

    public DBLayerDailyPlanProjections(SOSHibernateSession session) {
        super(session);
    }

    public int cleanup() throws SOSHibernateException {
        return getSession().executeUpdate("delete from " + DBITEM_DPL_PROJECTIONS);
    }

    public void insert(int year, YearsItem o) throws Exception {
        if (o == null) {
            return;
        }

        DBItemDailyPlanProjection item = new DBItemDailyPlanProjection();
        item.setId(Long.valueOf(year));
        item.setContent(Globals.objectMapper.writeValueAsString(o));
        item.setCreated(new Date());

        getSession().save(item);
    }

    public void insertMeta(MetaItem o) throws Exception {
        if (o == null) {
            return;
        }

        DBItemDailyPlanProjection item = new DBItemDailyPlanProjection();
        item.setId(DBItemDailyPlanProjection.METADATEN_ID);
        item.setContent(Globals.objectMapper.writeValueAsString(o));
        item.setCreated(new Date());

        getSession().save(item);
    }

    public List<DBItemDailyPlanProjection> getProjections(List<Long> years) throws Exception {
        StringBuilder hql = new StringBuilder("from ").append(DBITEM_DPL_PROJECTIONS).append(" ");
        List<Long> ids = null;
        if (years != null && years.size() > 0) {
            ids = new ArrayList<>(years);
            ids.add(DBItemDailyPlanProjection.METADATEN_ID);
            ids = ids.stream().distinct().collect(Collectors.toList());

            hql.append("where id in (:ids)");
        }

        Query<DBItemDailyPlanProjection> query = getSession().createQuery(hql.toString());
        if (ids != null) {
            query.setParameterList("ids", ids);
        }

        return getSession().getResultList(query);
    }

}