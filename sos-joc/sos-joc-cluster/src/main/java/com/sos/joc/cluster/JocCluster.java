package com.sos.joc.cluster;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationStaleStateException;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.db.cluster.DBItemJocCluster;
import com.sos.jobscheduler.event.http.HttpClient;
import com.sos.jobscheduler.event.http.HttpClientConfiguration;
import com.sos.joc.cluster.db.DBLayerCluster;

public class JocCluster {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocCluster.class);

    private final SOSHibernateFactory dbFactory;
    private final HttpClient httpClient;
    private final List<String> handlerUris;

    private final String currentMemberId;
    private String lastActiveMemberId;
    private String switchMemberId;
    private boolean currentIsActiv;
    private boolean switched;
    private boolean closed;
    private boolean firstRunExecuted;

    public JocCluster(SOSHibernateFactory factory, String memberId) {
        dbFactory = factory;
        currentMemberId = memberId;

        httpClient = new HttpClient();
        handlerUris = new ArrayList<String>();
        handlerUris.add("http://localhost:4446/history/history_event");// TODO
    }

    public void doProcessing() {
        currentIsActiv = false;
        firstRunExecuted = false;
        LOGGER.info(String.format("current memberId=%s", currentMemberId));
        while (!closed) {
            try {
                process();

                wait(30);
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);
                wait(10);
            }
            if (!firstRunExecuted) {
                firstRunExecuted = true;
            }
        }
    }

    private void process() throws Exception {
        DBLayerCluster dbLayer = null;
        boolean isNew = false;
        try {
            dbLayer = new DBLayerCluster(dbFactory.openStatelessSession());
            dbLayer.getSession().beginTransaction();
            dbLayer.updateInstanceHeartBeat(currentMemberId);
            dbLayer.getSession().commit();

            dbLayer.getSession().beginTransaction();
            DBItemJocCluster item = dbLayer.getCluster();
            if (item == null) {
                lastActiveMemberId = null;
                setCurrentIsActive(true);// vor db operation
                isNew = true;
                item = new DBItemJocCluster();
                item.setMemberId(currentMemberId);
                dbLayer.getSession().save(item);

            } else {
                lastActiveMemberId = item.getMemberId();
                if (item.getMemberId().equals(currentMemberId)) {
                    setCurrentIsActive(true);
                    item.setMemberId(currentMemberId);
                    dbLayer.getSession().update(item);
                } else {
                    Date now = new Date();
                    if (((now.getTime() / 1_000) - (item.getHeartBeat().getTime() / 1_000)) >= 60) {
                        setCurrentIsActive(true);
                        item.setMemberId(currentMemberId);
                        dbLayer.getSession().update(item);
                    } else {
                        setCurrentIsActive(false);
                    }
                }
            }
            dbLayer.getSession().commit();

        } catch (SOSHibernateObjectOperationStaleStateException e) {// @Version
            // locked
            if (dbLayer != null) {
                dbLayer.getSession().rollback();
            }
            if (isNew) {
                // ignore, locked by another instance
                resetCurrentIsActive();
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            if (dbLayer != null) {
                dbLayer.getSession().rollback();
            }
            throw e;
        } finally {
            if (dbLayer != null) {
                dbLayer.getSession().close();
            }
            notifyHandlers();// TODO
        }
    }

    private void notifyHandlers() {// TODO
        if (switched || !firstRunExecuted) {
            if (!firstRunExecuted) {
                LOGGER.info(String.format("[switched=true]lastActiveMemberId=%s", lastActiveMemberId));
            }
            for (String uri : handlerUris) {
                final String newUri = uri + "?" + (currentIsActiv ? "start=1" : "stop=1");
                Runnable task = new Runnable() {

                    @Override
                    public void run() {
                        LOGGER.info(String.format("[notify][%s][run]...", newUri));
                        sendNotify(newUri);
                        LOGGER.info(String.format("[notify][%s][end]", newUri));
                    }

                };
                Thread thread = new Thread(task);// TODO
                thread.start();
            }

        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[switched=false]lastActiveMemberId=%s", lastActiveMemberId));
            }
        }
    }

    private void sendNotify(String handlerUri) {
        if (!SOSString.isEmpty(handlerUri)) {
            try {
                httpClient.create(new HttpClientConfiguration());
                URI uri = new URI(handlerUri);
                // String response = httpClient.executePost(uri, params, null, true);
                String response = httpClient.executeGet(uri, null);
                LOGGER.info(String.format("[%s][%s]%s", httpClient.getLastRestServiceDuration(), handlerUri, response));

            } catch (Throwable t) {
                LOGGER.warn(String.format("[%s][exception]%s", t.toString()), t);
            } finally {
                httpClient.close();
            }
        }
    }

    private void setCurrentIsActive(boolean val) {
        switched = currentIsActiv != val;
        currentIsActiv = val;
    }

    private void resetCurrentIsActive() {
        switched = false;
        currentIsActiv = false;
    }

    public void close() {
        closed = true;
        synchronized (httpClient) {
            httpClient.notifyAll();
        }
    }

    public void wait(int interval) {
        if (!closed && interval > 0) {
            String method = "wait";
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s%ss ...", method, interval));
            }
            try {
                synchronized (httpClient) {
                    httpClient.wait(interval * 1_000);
                }
            } catch (InterruptedException e) {
                if (closed) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("%ssleep interrupted due handler close", method));
                    }
                } else {
                    LOGGER.warn(String.format("%s%s", method, e.toString()), e);
                }
            }
        }
    }

    public void setSwitchMemberId(String val) {
        switchMemberId = val;
    }
}
