package com.sos.joc.cluster;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationStaleStateException;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.db.cluster.DBItemJocCluster;
import com.sos.jobscheduler.event.http.HttpClient;
import com.sos.jobscheduler.event.http.HttpClientConfiguration;
import com.sos.joc.cluster.api.bean.ClusterAnswer;
import com.sos.joc.cluster.api.bean.ClusterAnswer.ClusterAnswerType;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.db.DBLayerCluster;

public class JocCluster {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocCluster.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private static final String CLUSTER_ID = "cluster";
    private final SOSHibernateFactory dbFactory;
    private final HttpClient httpClient;
    private final List<String> handlerUris;

    private final String currentMemberId;
    private String lastActiveMemberId;
    private String switchMemberId;
    private boolean currentIsActiv;
    private boolean switched;
    private boolean closed;

    public JocCluster(SOSHibernateFactory factory, JocConfiguration config) {
        dbFactory = factory;
        httpClient = new HttpClient();
        currentMemberId = config.getMemberId();

        handlerUris = new ArrayList<String>();
        handlerUris.add("http://localhost:" + config.getPort() + "/history/history_event");// TODO
    }

    public static ClusterAnswer switchHandler(int currentPort, String memberId) {
        return send(new HttpClient(), "http://localhost:" + currentPort + "/cluster/cluster?switch=1&memberId=" + memberId);
    }

    public void doProcessing() {
        currentIsActiv = false;
        LOGGER.info(String.format("[current memberId]%s", currentMemberId));

        while (!closed) {
            try {
                process();

                wait(30);
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);
                wait(10);
            }
        }
    }

    public void switchMember(String memberId) {
        switchMemberId = memberId;
    }

    private void process() throws Exception {
        DBLayerCluster dbLayer = null;
        DBItemJocCluster item = null;
        boolean isNew = false;
        try {
            dbLayer = new DBLayerCluster(dbFactory.openStatelessSession());
            dbLayer.getSession().beginTransaction();
            dbLayer.updateInstanceHeartBeat(currentMemberId);
            dbLayer.getSession().commit();

            dbLayer.getSession().beginTransaction();
            item = dbLayer.getCluster();
            lastActiveMemberId = item == null ? null : item.getMemberId();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[start][current=%s][last=%s]%s", currentMemberId, lastActiveMemberId, SOSHibernate.toString(item)));
            }

            if (item == null) {
                setCurrentIsActive(true);// before db operation
                isNew = true;
                item = new DBItemJocCluster();
                item.setId(CLUSTER_ID);
                item.setMemberId(currentMemberId);
                dbLayer.getSession().save(item);

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[save]%s", item));
                }

            } else {
                if (item.getMemberId().equals(currentMemberId)) {
                    setCurrentIsActive(true);
                    item.setMemberId(currentMemberId);
                    dbLayer.getSession().update(item);

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[currentMember][update]%s", SOSHibernate.toString(item)));
                    }
                } else {
                    Date now = new Date();
                    if (((now.getTime() / 1_000) - (item.getHeartBeat().getTime() / 1_000)) >= 60) {
                        setCurrentIsActive(true);
                        item.setMemberId(currentMemberId);
                        dbLayer.getSession().update(item);

                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[heartBeat exceeded][update]%s", SOSHibernate.toString(item)));
                        }
                    } else {
                        setCurrentIsActive(false);

                        if (isDebugEnabled) {
                            LOGGER.debug("not active");
                        }
                    }
                }
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[end][current=%s][last=%s]%s", currentMemberId, lastActiveMemberId, SOSHibernate.toString(item)));
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
            LOGGER.error(e.toString(), e);
            LOGGER.error(String.format("[exception][current=%s][last=%s]%s", currentMemberId, lastActiveMemberId, SOSHibernate.toString(item)));

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
        if (switched) {
            if (lastActiveMemberId == null || lastActiveMemberId.contentEquals(currentMemberId)) {
                LOGGER.info(String.format("[%s]start processing", currentMemberId));
            } else {
                LOGGER.info(String.format("[%s][switched=true]from lastActiveMemberId=%s", currentMemberId, lastActiveMemberId));
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
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[switched=false]lastActiveMemberId=%s", lastActiveMemberId));
            }
        }
    }

    private void sendNotify(String handlerUri) {
        if (!SOSString.isEmpty(handlerUri)) {
            send(httpClient, handlerUri);
        }
    }

    private static ClusterAnswer send(HttpClient client, String uri) {
        ClusterAnswer answer = null;
        try {
            client.create(new HttpClientConfiguration());
            answer = readAnswer(client.executeGet(new URI(uri), null));
            LOGGER.info(String.format("[%s][%s]%s", client.getLastRestServiceDuration(), uri, SOSString.toString(answer)));

        } catch (Throwable t) {
            LOGGER.error(String.format("[%s][exception]%s", uri, t.toString()), t);
            answer = new ClusterAnswer();
            answer.setType(ClusterAnswerType.ERROR);
            answer.setMessage(t.toString());
        } finally {
            client.close();
        }
        return answer;
    }

    private static ClusterAnswer readAnswer(String response) throws Exception {
        ObjectMapper om = new ObjectMapper();
        om.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        return om.readValue(response, ClusterAnswer.class);
    }

    private void setCurrentIsActive(boolean val) {
        if (isDebugEnabled) {
            LOGGER.debug("currentIsActiv=" + currentIsActiv + ", val=" + val);
        }
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
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s%ss ...", method, interval));
            }
            try {
                synchronized (httpClient) {
                    httpClient.wait(interval * 1_000);
                }
            } catch (InterruptedException e) {
                if (closed) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%ssleep interrupted due handler close", method));
                    }
                } else {
                    LOGGER.warn(String.format("%s%s", method, e.toString()), e);
                }
            }
        }
    }

}
