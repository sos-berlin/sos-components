package com.sos.joc.classes.proxy;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.OptionalLong;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;

import js7.base.log.LogLevel;
import js7.base.log.reader.KeyedLogLine;
import js7.base.log.reader.LogLineKey;
import js7.data.node.EngineServerId;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;

@Ignore
public class LogTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogTest.class);
    private static ProxyCredentials credential = null;
    private static final int connectionTimeOut = Globals.httpConnectionTimeout;

    @BeforeClass
    public static void setUp() {
        //Proxies.closeAll();
        Globals.httpConnectionTimeout = Math.max(20000, Globals.httpConnectionTimeout);
        credential = ProxyCredentialsBuilder.withControllerIdAndUrl("standalone", "http://centosdev_secondary.sos:5344")
                .withAccount(ProxyUser.JOC).build();
    }

    @AfterClass
    public static void tearDown() {
        Globals.httpConnectionTimeout = connectionTimeOut;
        Proxies.closeAll();
    }
    
    public void fluxDoOnComplete() {
        LOGGER.info("OnComplete");
    }
    
    public void fluxDoFinally(SignalType type) {
        LOGGER.info("OnFinally: " + type.toString());
    }
    
    public void fluxDoOnError(Throwable t) {
        LOGGER.error("OnError", t);
    }

    @Test
    public void testLog6() {
        try {
            //Globals.sosCockpitProperties = new JocCockpitProperties("/joc/joc.oh.properties");
            JControllerProxy proxy = Proxy.of(credential); //Proxies.getInstance().of(credential, 5000l); //Proxy.of(credential);
            
            ZoneId zoneId = ZoneId.of(proxy.currentState().asScala().controllerMetaState().timezone().string());
            LOGGER.info("Controller ZoneId: " + zoneId.getId());
            //Instant instant = Instant.now().minusSeconds(3600);
            //Instant instant = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
            //Flux<List<KeyedLogLine>> flux = proxy.keyedLogLineFlux(LogLevel.debug(), instant, 100l);
            //LogLineKey.
            Flux<List<KeyedLogLine>> flux = proxy.keyedLogLineFlux(EngineServerId.primaryController, LogLevel.debug(), Instant.parse(
                    "2026-03-03T17:35:00Z"), OptionalLong.of(10l));
            // Error handling and completion
            flux = flux.doOnError(this::fluxDoOnError);
            flux = flux.doOnComplete(this::fluxDoOnComplete);
            //flux = flux.doOnCancel(this::fluxDoOnCancel);
            flux = flux.doFinally(this::fluxDoFinally);
            Map<String, Object> response = new HashMap<>();
            List<String> loglines = new ArrayList<>();
//            LogLineKey llk = flux.publishOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())).doOnNext(keyedLogLines -> {
//                //System.out.println(keyedLogLines);
//                keyedLogLines.stream().map(KeyedLogLine::line).forEach(LOGGER::info);
//                //loglines.addAll(keyedLogLines.stream().map(KeyedLogLine::line).toList());
//            }).reduce(LogLineKey.parse("0/0").toOption().get(), (a, lines) -> a = lines.get(lines.size() - 1).key()).toFuture().get();
            
            flux = proxy.keyedLogLineFlux(EngineServerId.primaryController, LogLevel.debug(), Instant.parse("2026-03-03T17:35:00Z"), OptionalLong.of(10l));
            LogLineKey llk2 = flux.publishOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())).doOnNext(keyedLogLines -> {
                //System.out.println(keyedLogLines);
                //keyedLogLines.stream().map(KeyedLogLine::line).forEach(LOGGER::info);
                loglines.addAll(keyedLogLines.stream().map(KeyedLogLine::line).toList());
            }).last().map(lines -> lines.get(lines.size() - 1).key()).toFuture().get();
            
//            LOGGER.info("loglineKey instant: " + llk.instant().toString());
//            LOGGER.info("loglineKey position: " + llk.position());
            LOGGER.info("loglineKey fileinstant: " + llk2.fileInstant().toString());
            LOGGER.info("loglineKey position: " + llk2.position());
            
            LogLineKey llkTest = llk2; //LogLineKey.parse("0/150041").toOption().get();
            flux = proxy.keyedLogLineFlux(EngineServerId.primaryController, LogLevel.debug(), llkTest, OptionalLong.of(10l));
            LogLineKey llk3;
            try {
                llk3 = flux.publishOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())).doOnNext(keyedLogLines -> {
                    //System.out.println(keyedLogLines);
                    //keyedLogLines.stream().map(KeyedLogLine::line).forEach(LOGGER::info);
                    loglines.addAll(keyedLogLines.stream().map(KeyedLogLine::line).toList());
                }).last().map(lines -> lines.get(lines.size() - 1).key()).toFuture().get(57, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                if (e.getCause() != null && e.getCause() instanceof NoSuchElementException) {
                    LOGGER.info("Empty flux");
                }
                llk3 = llkTest;
            } catch (TimeoutException e) {
                LOGGER.info("Empty flux");
                llk3 = llkTest;
            }
            //.reduce(llkTest, (a, lines) -> a = lines.get(lines.size() - 1).key()).toFuture().get();
            response.put("loglines", loglines);
            response.put("loglineKey", llk3.toString());
            LOGGER.info("loglineKey fileinstant: " + llk3.fileInstant().toString());
            LOGGER.info("loglineKey position: " + llk3.position());
            
            LOGGER.info(Globals.prettyPrintObjectMapper.writeValueAsString(response));
            
        } catch (ControllerConnectionRefusedException e) {
            LOGGER.warn(e.toString());
            Assert.assertTrue("Controller is unfortunately not available at the time of testing", true);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }
    
    

}
