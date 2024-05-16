package com.sos.jitl.jobs.db.hibernateProxy;

import java.util.Date;
import java.util.List;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.js7.job.Job;
import com.sos.js7.job.JobHelper;
import com.sos.js7.job.OrderProcessStep;

public class HibernateProxyTest extends Job<HibernateProxyTestJobArguments> {

    @Override
    public void processOrder(OrderProcessStep<HibernateProxyTestJobArguments> step) throws Exception {

        for (int i = 1; i <= step.getDeclaredArguments().getFactoryInstances().getValue(); i++) {
            String prefix = i + ")";

            SOSHibernateFactory factory = null;
            SOSHibernateSession session = null;
            try {
                step.getLogger().info(prefix + "createJobHibernateFactory");
                factory = createJobHibernateFactory();

                if (step.getDeclaredArguments().getInserts().getValue() > 0L) {
                    session = factory.openStatelessSession(HibernateProxyTest.class.getSimpleName());
                    insert(step, session);

                    List<DBItemHibernateProxy> l = session.getResultList("from " + DBItemHibernateProxy.class.getName());
                    step.getLogger().info(prefix + "Total items: " + l.size());
                    for (DBItemHibernateProxy p : l) {
                        step.getLogger().info(SOSString.toString(p));
                    }
                }
            } catch (Throwable e) {
                if (session != null) {
                    session.rollback();
                }
                throw e;
            } finally {
                if (factory != null) {
                    step.getLogger().info(prefix + "closeJobHibernateFactory");
                    factory.close(session);
                }
            }
        }
    }

    private void insert(OrderProcessStep<HibernateProxyTestJobArguments> step, SOSHibernateSession session) throws Exception {
        session.beginTransaction();

        step.getLogger().info("Insert " + step.getDeclaredArguments().getInserts().getValue() + " items ...");
        for (int i = 0; i < step.getDeclaredArguments().getInserts().getValue(); i++) {
            DBItemHibernateProxy item = new DBItemHibernateProxy();
            item.setName(i + "=" + (new Date().getTime()));
            item.setValue(item.getName() + "-value");
            session.save(item);
        }

        session.commit();
    }

    private static SOSHibernateFactory createJobHibernateFactory() throws Exception {
        SOSHibernateFactory f = new SOSHibernateFactory(JobHelper.getAgentHibernateFile());
        f.addClassMapping(DBItemHibernateProxy.class);
        f.build();
        return f;
    }
}
