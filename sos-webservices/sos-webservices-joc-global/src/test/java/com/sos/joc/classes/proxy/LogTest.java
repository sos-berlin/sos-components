package com.sos.joc.classes.proxy;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.ForkJoinPool;

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
import js7.data.node.Js7ServerId;
import js7.proxy.javaapi.JControllerProxy;
import js7.proxy.javaapi.log.JLogSelection;
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

    @Test
    public void testLog6() {
        try {
            //Globals.sosCockpitProperties = new JocCockpitProperties("/joc/joc.oh.properties");
            JControllerProxy proxy = Proxy.of(credential); //Proxies.getInstance().of(credential, 5000l); //Proxy.of(credential);
            
            ZoneId zoneId = ZoneId.of(proxy.currentState().asScala().controllerMetaState().timezone().string());
            LOGGER.info("Controller ZoneId: " + zoneId.getId());
            
            JLogSelection selection = JLogSelection.empty().withLineLimit(OptionalLong.of(10l));
            List<String> loglines = new ArrayList<>();
            
            LogLineKey llk = proxy.engineLog(Js7ServerId.primaryController, LogLevel.debug()).flatMap(eLog -> eLog.keyedLogLineFlux(Instant.parse(
                    "2026-03-03T17:35:00Z"), selection).publishOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())).doOnNext(keyedLogLines -> {
                        //System.out.println(keyedLogLines);
                        //keyedLogLines.stream().map(KeyedLogLine::line).forEach(LOGGER::info);
                        loglines.addAll(keyedLogLines.stream().map(KeyedLogLine::line).toList());
                    })).last().map(lines -> lines.get(lines.size() - 1).key()).toFuture().get();
            LOGGER.info("loglineKey fileinstant: " + llk.fileInstant().toString());
            LOGGER.info("loglineKey position: " + llk.position());
            Map<String, Object> response = new HashMap<>();
            response.put("loglines", loglines);
            
            LOGGER.info(Globals.prettyPrintObjectMapper.writeValueAsString(response));
            
        } catch (ControllerConnectionRefusedException e) {
            LOGGER.warn(e.toString());
            Assert.assertTrue("Controller is unfortunately not available at the time of testing", true);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }
    
    

}
