package com.sos.joc.bean;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.db.history.HistoryFilter;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.history.HistoryOrderTaskTerminated;
import com.sos.joc.event.bean.history.HistoryOrderTerminated;
import com.sos.joc.model.common.HistoryStateText;

public class HistorySummary implements HistorySummaryMBean, IJocMBean {

    private final String controllerId;
    private HistoryFilter orderHistoryFilter;
    private HistoryFilter orderStepsHistoryFilter;
    private AtomicLong successfulOrders = new AtomicLong(0);
    private AtomicLong failedOrders = new AtomicLong(0);
    private AtomicLong successfulJobs = new AtomicLong(0);
    private AtomicLong failedJobs = new AtomicLong(0);
    private static final boolean onlylast24hours = true;
    private AtomicBoolean hasOrderEvent = new AtomicBoolean(false);
    private AtomicBoolean hasOrderStepEvent = new AtomicBoolean(false);
    private static final Logger LOGGER = LoggerFactory.getLogger(HistorySummaryMBean.class);
    private Predicate<String> isMainOrder = oId -> oId == null || !oId.contains("|");
    
    public HistorySummary(String controllerId) {
        this.controllerId = controllerId;
        orderHistoryFilter = initOrderHistoryFilter();
        orderStepsHistoryFilter = initOrderStepsHistoryFilter();
        EventBus.getInstance().register(this);
        setOrderSummary();
    }

    @Subscribe({ HistoryOrderTerminated.class })
    public void update(HistoryOrderTerminated evt) {
        if (controllerId.equals(evt.getControllerId()) && isMainOrder.test(evt.getOrderId())) {
            if (!hasOrderEvent.getAndSet(true)) {
                Executors.newScheduledThreadPool(1).schedule(() -> {
                    setOrderSummary();
                    hasOrderEvent.set(false);
                }, 5, TimeUnit.SECONDS);
            }
        }
    }
    
    @Subscribe({ HistoryOrderTaskTerminated.class })
    public void update(HistoryOrderTaskTerminated evt) {
        if (controllerId.equals(evt.getControllerId())) {
            if (!hasOrderStepEvent.getAndSet(true)) {
                Executors.newScheduledThreadPool(1).schedule(() -> {
                    setOrderStepsSummary();
                    hasOrderStepEvent.set(false);
                }, 5, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public String objectName() {
        //return getClass().getSimpleName();
        return "history";
    }
    
    private HistoryFilter initOrderHistoryFilter() {
        HistoryFilter historyFilter = new HistoryFilter();
        historyFilter.setControllerIds(Collections.singleton(controllerId));
        historyFilter.setMainOrder(true);
        return historyFilter;
    }
    
    private HistoryFilter initOrderStepsHistoryFilter() {
        HistoryFilter historyFilter = new HistoryFilter();
        historyFilter.setControllerIds(Collections.singleton(controllerId));
        return historyFilter;
    }

    private void setOrderSummary() {
        SOSHibernateSession connection = null;
        try {
            if (onlylast24hours) {
                orderHistoryFilter.setExecutedFrom(Date.from(Instant.now().minusSeconds(60 * 60 * 24)));
            }
            connection = Globals.createSosHibernateStatelessConnection(HistorySummaryMBean.class.getSimpleName());
            JobHistoryDBLayer jobHistoryDBLayer = new JobHistoryDBLayer(connection, orderHistoryFilter);
            failedOrders.set(jobHistoryDBLayer.getCountOrders(HistoryStateText.FAILED, null));
            successfulOrders.set(jobHistoryDBLayer.getCountOrders(HistoryStateText.SUCCESSFUL, null));
        } catch (Exception e) {
            LOGGER.warn("Error at updating " + objectName() + " metrics: ", e);
        } finally {
            Globals.disconnect(connection);
        }
    }

    private void setOrderStepsSummary() {
        SOSHibernateSession connection = null;
        try {
            if (onlylast24hours) {
                orderStepsHistoryFilter.setExecutedFrom(Date.from(Instant.now().minusSeconds(60 * 60 * 24)));
            }
            connection = Globals.createSosHibernateStatelessConnection(HistorySummaryMBean.class.getSimpleName());
            JobHistoryDBLayer jobHistoryDBLayer = new JobHistoryDBLayer(connection, orderStepsHistoryFilter);
            failedJobs.set(jobHistoryDBLayer.getCountJobs(HistoryStateText.FAILED, null));
            successfulJobs.set(jobHistoryDBLayer.getCountJobs(HistoryStateText.SUCCESSFUL, null));
        } catch (Exception e) {
            LOGGER.warn("Error at updating " + objectName() + " metrics: ", e);
        } finally {
            Globals.disconnect(connection);
        }
    }

    @Override
    public long getsuccessful_orders_of_last_24_hours() {
        return successfulOrders.get();
    }

    @Override
    public long getfailed_orders_of_last_24_hours() {
        return failedOrders.get();
    }

    @Override
    public long getsuccessful_jobs_of_last_24_hours() {
        return successfulJobs.get();
    }

    @Override
    public long getfailed_jobs_of_last_24_hours() {
        return failedJobs.get();
    }

}
