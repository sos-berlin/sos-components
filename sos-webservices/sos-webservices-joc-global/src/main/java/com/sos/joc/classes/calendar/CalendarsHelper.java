package com.sos.joc.classes.calendar;

import java.time.Instant;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsDailyPlan;
import com.sos.joc.cluster.configuration.globals.common.ConfigurationEntry;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.dailyplan.DailyPlanCalendarEvent;
import com.sos.joc.event.bean.proxy.ProxyCoupled;
import com.sos.joc.event.bean.proxy.ProxyRemoved;
import com.sos.joc.event.bean.proxy.ProxyStarted;
import com.sos.joc.exceptions.JocError;

import js7.base.problem.Problem;
import js7.base.time.Timezone;
import js7.data.calendar.Calendar;
import js7.data.calendar.CalendarPath;
import js7.data.item.ItemOperation.AddOrChangeSimple;
import js7.data_for_java.item.JUpdateItemOperation;
import reactor.core.publisher.Flux;
import scala.concurrent.duration.FiniteDuration;

public class CalendarsHelper {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarsHelper.class);
    private static volatile CopyOnWriteArraySet<String> failedControllerIds = new CopyOnWriteArraySet<>();
    public static final String dailyPlanCalendarName = "dailyPlan";
    
    public CalendarsHelper() {
        EventBus.getInstance().register(this);
    }
    
    @Subscribe({ DailyPlanCalendarEvent.class })
    public static void initDailyPlanCalendar(DailyPlanCalendarEvent evt) {
        //TODO check if Calendar exists
        updateDailyPlanCalendar(null, null, null);
    }
    
    @Subscribe({ ProxyRemoved.class })
    public static void removeProxy(ProxyRemoved evt) {
        failedControllerIds.remove(evt.getControllerId());
    }
    
    @Subscribe({ ProxyCoupled.class })
    public static void updateProxy(ProxyCoupled evt) {
        if (failedControllerIds.contains(evt.getControllerId())) {
            ConfigurationGlobalsDailyPlan conf = Globals.getConfigurationGlobalsDailyPlan();
            Flux<JUpdateItemOperation> itemOperation = getItemOperation(getValue(conf.getTimeZone()), convertPeriodBeginToLong(getValue(conf
                    .getPeriodBegin())));
            try {
                ControllerApi.of(evt.getControllerId()).updateItems(itemOperation).thenAccept(e -> {
                    if (e.isRight()) {
                        LOGGER.info("DailyPlan-Calendar submitted to " + evt.getControllerId());
                        failedControllerIds.remove(evt.getControllerId());
                    }
                });
            } catch (Exception e) {
                //
            }
        }
    }
    
    @Subscribe({ ProxyStarted.class })
    public static void updateProxy(ProxyStarted evt) {
            ConfigurationGlobalsDailyPlan conf = Globals.getConfigurationGlobalsDailyPlan();
            Flux<JUpdateItemOperation> itemOperation = getItemOperation(getValue(conf.getTimeZone()), convertPeriodBeginToLong(getValue(conf
                    .getPeriodBegin())));
            try {
                ControllerApi.of(evt.getControllerId()).updateItems(itemOperation).thenAccept(e -> {
                    if (e.isRight()) {
                        LOGGER.info("DailyPlan-Calendar submitted to " + evt.getControllerId());
                    } else {
                        failedControllerIds.add(evt.getControllerId());
                    }
                });
            } catch (Exception e) {
                failedControllerIds.add(evt.getControllerId());
            }
    }
    
    public static synchronized void updateDailyPlanCalendar(String controllerId, String accessToken, JocError jocError) {
        ConfigurationGlobalsDailyPlan conf = Globals.getConfigurationGlobalsDailyPlan();

        try {
            deployDailyPlanCalendar(getValue(conf.getTimeZone()), convertPeriodBeginToLong(getValue(conf.getPeriodBegin())), controllerId,
                    accessToken, jocError);
        } catch (Exception e) {
            LOGGER.warn(e.toString());
        }
    }
    
    public static synchronized void deployDailyPlanCalendar(String timezone, long dateOffset, String curControllerId, String accessToken, JocError jocError) {

        Flux<JUpdateItemOperation> itemOperation = getItemOperation(timezone, dateOffset);
        
        failedControllerIds.clear();
        for (String controllerId : Proxies.getControllerDbInstances().keySet()) {
            try {
                ControllerApi.of(controllerId).updateItems(itemOperation).thenAccept(e -> {
                    if (curControllerId != null && controllerId.equals(curControllerId)) {
                        ProblemHelper.postProblemEventIfExist(e, accessToken, jocError, controllerId);
                    } else {
                        ProblemHelper.postProblemEventIfExist(e, null, null, null);
                    }
                    if (e.isRight()) {
                        LOGGER.info("DailyPlan-Calendar submitted to " + controllerId); 
                    } else {
                        failedControllerIds.add(controllerId);
                    }
                });
            } catch (Exception e) {
                failedControllerIds.add(controllerId);
                if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                    LOGGER.info(jocError.printMetaInfo());
                    jocError.clearMetaInfo();
                }
                LOGGER.error("", e);
            }
        };
        
//        if (!failedControllerIds.isEmpty()) {
//            LOGGER.warn("DailyPlan-Calendar is not submitted to: " + failedControllerIds.toString());
//            //throw new JocDeployException("DailyPlan-Calendar is not submitted to: " + failedControllerIds.toString());
//        }
    }
    
    public synchronized void deploy(com.sos.sign.model.calendar.Calendar cal) {
        // TODO for maybe other Calendars than dailyPlan
    }
    
    private static Flux<JUpdateItemOperation> getItemOperation(String timezone, long dateOffset) {
        scala.util.Either<Problem, Timezone> t = Timezone.checked(timezone);
        Timezone _timezone = null;
        if (t.isRight()) {
            _timezone = t.toOption().get();
        } else {
            throw new IllegalArgumentException("Time zone (" + timezone + ") is not available");
        }
        Calendar c = Calendar.apply(CalendarPath.of(dailyPlanCalendarName), _timezone, FiniteDuration.apply(dateOffset, TimeUnit.SECONDS), "#([^#]+)#.*",
                "yyyy-MM-dd", scala.Option.empty());
        LOGGER.info("Try to submit DailyPlan-Calendar: " + c.toString());
        return Flux.just(JUpdateItemOperation.apply(new AddOrChangeSimple(c)));
    }
    
    private static long convertPeriodBeginToLong(String periodBegin) {
        
        periodBegin = (periodBegin + ":00:00").substring(0, 8);
        if (!periodBegin.matches("\\d{2}:\\d{2}:\\d{2}")) {
            throw new IllegalArgumentException("periodBegin (" + periodBegin + ") must have the format hh:mm:ss");
        }
        
        return Instant.parse("1970-01-01T" + periodBegin + "Z").getEpochSecond();
    }
    
    private static String getValue(ConfigurationEntry c) {
        String s = c.getValue();
        if (s == null || s.isEmpty()) {
            return c.getDefault();
        }
        return s;
    }

}
