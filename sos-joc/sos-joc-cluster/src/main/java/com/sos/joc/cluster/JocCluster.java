package com.sos.joc.cluster;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

import javassist.NotFoundException;

public class JocCluster {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocCluster.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private static final String CLUSTER_ID = "cluster";
    private static final int MAX_RERUNS_ON_ERROR = 10;
    private final SOSHibernateFactory dbFactory;
    private final HttpClient httpClient;
    private final String currentMemberId;

    private List<String> handlerUris;
    private String lastActiveMemberId;
    private String currentActiveMemberId;

    private String switchMemberId;
    private boolean currentIsActiv;
    private boolean switched;
    private boolean closed;

    public JocCluster(SOSHibernateFactory factory, JocConfiguration config) {
        dbFactory = factory;
        httpClient = new HttpClient();
        currentMemberId = config.getMemberId();

        initHandlers(config);
    }

    private void initHandlers(JocConfiguration config) {
        handlerUris = new ArrayList<String>();
        handlerUris.add("http://localhost:" + config.getPort() + "/history/history_event");// TODO
        // handlerUris.add("http://localhost:" + config.getPort() + "/order/orderInitiator");// TODO
    }

    public static ClusterAnswer switchHandler(int currentPort, String memberId) {
        HttpClient client = new HttpClient();
        try {
            return send(client, "http://localhost:" + currentPort + "/cluster/cluster?switch=1&memberId=" + memberId);
        } finally {
            client.close();
        }
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
            if (closed) {
                LOGGER.info(String.format("[end][skip due closed=true][current=%s][last=%s]%s", currentMemberId, lastActiveMemberId, SOSHibernate
                        .toString(item)));
                dbLayer.getSession().rollback();
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[end][current=%s][last=%s]%s", currentMemberId, lastActiveMemberId, SOSHibernate.toString(item)));
                }
                dbLayer.getSession().commit();
            }
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

    private ClusterAnswer notifyHandlers() {// TODO
        if (switched) {
            if (lastActiveMemberId == null || lastActiveMemberId.equals(currentMemberId)) {
                LOGGER.info(String.format("[active memberId]%s", currentMemberId));
            } else {
                LOGGER.info(String.format("[active memberId][%s][switched from %s]", currentMemberId, lastActiveMemberId));

            }
            currentActiveMemberId = currentMemberId;

            // List<Runnable> tasks = new ArrayList<Runnable>();
            List<Supplier<ClusterAnswer>> tasks = new ArrayList<Supplier<ClusterAnswer>>();
            for (String uri : handlerUris) {
                final String newUri = uri + "?" + (currentIsActiv ? "start=1" : "stop=1");
                Supplier<ClusterAnswer> task = new Supplier<ClusterAnswer>() {

                    @Override
                    public ClusterAnswer get() {
                        LOGGER.info(String.format("[notify][%s][run]...", newUri));
                        ClusterAnswer answer = sendNotify(newUri);
                        LOGGER.info(String.format("[notify][%s][end]", newUri));
                        return answer;
                    }

                };
                tasks.add(task);
            }
            return executeTasks(tasks);
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[switched=false]lastActiveMemberId=%s", lastActiveMemberId));
            }

            if (lastActiveMemberId != null) {
                if (currentActiveMemberId == null || !currentActiveMemberId.equals(lastActiveMemberId)) {
                    LOGGER.info(String.format("[active memberId]%s", lastActiveMemberId));
                    currentActiveMemberId = lastActiveMemberId;
                }
            }
        }
        return null;
    }

    private ClusterAnswer executeTasks(List<Supplier<ClusterAnswer>> tasks) {
        ExecutorService es = Executors.newFixedThreadPool(handlerUris.size());
        // CompletableFuture<ClusterAnswer>[] futures = tasks.stream().map(task -> CompletableFuture.supplyAsync(task, es)).toArray(
        // CompletableFuture[]::new);

        List<CompletableFuture<ClusterAnswer>> futuresList = tasks.stream().map(task -> CompletableFuture.supplyAsync(task, es)).collect(Collectors
                .toList());

        CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()])).join();
        shutdownThreadPool("[executeTasks]", es, 3); // es.shutdown();

        // for (CompletableFuture<ClusterAnswer> future : futuresList) {
        // try {
        // LOGGER.info(SOSString.toString(future.get()));
        // } catch (Exception e) {
        // LOGGER.error(e.toString(), e);
        // }
        // }
        ClusterAnswer answer = new ClusterAnswer();// TODO check future results
        answer.setType(ClusterAnswerType.SUCCESS);
        return answer;
    }

    private ClusterAnswer sendNotify(String handlerUri) {
        ClusterAnswer answer = null;
        if (!SOSString.isEmpty(handlerUri)) {

            boolean run = !closed;
            int counter = 0;
            while (run) {
                answer = send(httpClient, handlerUri);
                if (closed || answer.getType().equals(ClusterAnswerType.SUCCESS) || counter >= MAX_RERUNS_ON_ERROR) {
                    run = false;
                } else if (answer.getType().equals(ClusterAnswerType.ERROR)) {
                    if (answer.getError().getType().equals(NotFoundException.class.getSimpleName())) {
                        run = false;
                        LOGGER.error(String.format("[%s][skip]application not found/not deployed", handlerUri));
                    } else {
                        wait(5);
                    }
                }
                counter += 1;
            }
        }
        return answer;
    }

    private static synchronized ClusterAnswer send(HttpClient client, String uri) {
        ClusterAnswer answer = null;
        try {
            client.tryCreate(new HttpClientConfiguration());
            answer = readAnswer(client.executeGet(new URI(uri), null));
            LOGGER.info(String.format("[%s][%s]%s", client.getLastRestServiceDuration(), uri, SOSString.toString(answer)));
        } catch (Exception e) {
            client.close();
            LOGGER.error(String.format("[%s][exception]%s", uri, e.toString()));
            answer = new ClusterAnswer();
            answer.createError(e);
        } finally {
            // client.close();
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
        httpClient.close();
        synchronized (httpClient) {
            httpClient.notifyAll();
        }
    }

    public static void shutdownThreadPool(String callerMethod, ExecutorService threadPool, long awaitTerminationTimeout) {
        try {
            if (threadPool == null) {
                return;
            }
            threadPool.shutdown();
            // threadPool.shutdownNow();
            boolean shutdown = threadPool.awaitTermination(awaitTerminationTimeout, TimeUnit.SECONDS);
            if (shutdown) {
                LOGGER.info(String.format("%sthread has been shut down correctly", callerMethod));
            } else {
                LOGGER.info(String.format("%sthread has ended due to timeout of %ss on shutdown", callerMethod, awaitTerminationTimeout));
            }
        } catch (InterruptedException e) {
            LOGGER.error(String.format("%s[exception]%s", callerMethod, e.toString()), e);
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
