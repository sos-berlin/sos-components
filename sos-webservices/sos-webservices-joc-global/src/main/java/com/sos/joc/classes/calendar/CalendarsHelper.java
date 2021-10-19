package com.sos.joc.classes.calendar;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
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
import com.sos.joc.exceptions.JocDeployException;
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
    public static final String dailyPlanCalendarName = "dailyPlan";
    
    public CalendarsHelper() {
        EventBus.getInstance().register(this);
    }
    
    @Subscribe({ DailyPlanCalendarEvent.class })
    public void initDailyPlanCalendar(DailyPlanCalendarEvent evt) {
        //TODO check if Calendar exists
        updateDailyPlanCalendar(null, null, null);
    }
    
    public static void updateDailyPlanCalendar(String controllerId, String accessToken, JocError jocError) {
        ConfigurationGlobalsDailyPlan conf = Globals.getConfigurationGlobalsDailyPlan();

        try {
            deployDailyPlanCalendar(getValue(conf.getTimeZone()), convertPeriodBeginToLong(getValue(conf.getPeriodBegin())), controllerId,
                    accessToken, jocError);
        } catch (Exception e) {
            LOGGER.warn(e.toString());
        }
    }
    
    public static void deployDailyPlanCalendar(String timezone, long dateOffset, String curControllerId, String accessToken, JocError jocError) {

        scala.util.Either<Problem, Timezone> t = Timezone.checked(timezone);
        Timezone _timezone = null;
        if (t.isRight()) {
            _timezone = t.toOption().get();
        } else {
            throw new IllegalArgumentException("Time zone (" + timezone + ") is not available");
        }
        Calendar c = Calendar.apply(CalendarPath.of(dailyPlanCalendarName), _timezone, FiniteDuration.apply(dateOffset, TimeUnit.SECONDS), "#([^#]+)#.*",
                "yyyy-MM-dd", scala.Option.empty());
        Flux<JUpdateItemOperation> itemOperation = Flux.just(JUpdateItemOperation.apply(new AddOrChangeSimple(c)));
        
        LOGGER.info("Try to submit DailyPlan-Calendar: " + c.toString());
        
        Set<String> failedControllerIds = new HashSet<>();
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
        
        if (!failedControllerIds.isEmpty()) {
            throw new JocDeployException("DailyPlan-Calendar is not submitted to: " + failedControllerIds.toString());
        }
    }
    
    public static void deploy(com.sos.sign.model.calendar.Calendar cal) {
        // TODO for maybe other Calendars than dailyPlan
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
