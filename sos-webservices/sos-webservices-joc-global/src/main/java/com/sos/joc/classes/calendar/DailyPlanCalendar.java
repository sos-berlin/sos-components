package com.sos.joc.classes.calendar;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsDailyPlan;
import com.sos.joc.cluster.configuration.globals.common.ConfigurationEntry;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.dailyplan.DailyPlanCalendarEvent;
import com.sos.joc.exceptions.JocError;

import io.vavr.control.Either;
import js7.data.calendar.CalendarPath;
import js7.data_for_java.calendar.JCalendar;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;

public class DailyPlanCalendar {
    
    public static final String dailyPlanCalendarName = "dailyPlan";
    private static final CalendarPath dailyPlanCalendarPath = CalendarPath.of(dailyPlanCalendarName);
    private static DailyPlanCalendar instance;
    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanCalendar.class);
    private boolean initIsCalled = false;
    
    private DailyPlanCalendar() {
        EventBus.getInstance().register(this);
    }
    
    public static synchronized DailyPlanCalendar getInstance() {
        if (instance == null) {
            instance = new DailyPlanCalendar();
        }
        return instance;
    }
    
    @Subscribe({ DailyPlanCalendarEvent.class })
    public void initDailyPlanCalendar(DailyPlanCalendarEvent evt) {
        if (!initIsCalled) {
            initIsCalled = true;
            updateDailyPlanCalendar((String) null, (String) null, (JocError) null);
        }
    }
    
//    @Subscribe({ ProxyCoupled.class })
//    public void updateProxy(ProxyCoupled evt) {
//        if (evt.isCoupled() && initIsCalled) {
//            try {
//                JCalendar calendar = getDailyPlanCalendar(Globals.getConfigurationGlobalsDailyPlan());
//                JControllerProxy proxy = Proxy.of(evt.getControllerId());
//                if (!dailyPlanCalendarIsAlreadySubmitted(proxy, calendar)) {
//                    proxy.api().updateItems(Flux.just(JUpdateItemOperation.addOrChangeSimple(calendar))).thenAccept(e -> {
//                        ProblemHelper.postProblemEventIfExist(e, null, null, null);
//                        if (e.isRight()) {
//                            LOGGER.info("DailyPlanCalendar submitted to " + evt.getControllerId());
//                        }
//                    });
//                }
//            } catch (Exception e) {
//                //
//            }
//        }
//    }
    
    // this method is called directly in onProxyCoupled so that we don't need longer to listen ProxyCoupled event
    public void updateDailyPlanCalendar(JControllerApi controllerApi, JControllerState currentState, String controller) {
        if (initIsCalled) {
            try {
                JCalendar calendar = getDailyPlanCalendar(Globals.getConfigurationGlobalsDailyPlan());
                Map<CalendarPath, JCalendar> knownCalendars = currentState.pathToCalendar();
                if (!dailyPlanCalendarIsAlreadySubmitted(knownCalendars, calendar)) {
                    controllerApi.updateItems(Flux.just(JUpdateItemOperation.addOrChangeSimple(calendar))).thenAccept(e -> {
                        if (e.isRight()) {
                            LOGGER.info("DailyPlanCalendar submitted to " + controller);
                        } else {
                            LOGGER.error(ProblemHelper.getErrorMessage(e.getLeft()));
                        }
                    });
                }
            } catch (Exception e) {
                //
            }
        }
    }
    
    public synchronized void updateDailyPlanCalendar(String controllerId, String accessToken, JocError jocError) {
        try {
            JCalendar calendar = getDailyPlanCalendar(Globals.getConfigurationGlobalsDailyPlan());
            deployDailyPlanCalendar(calendar, controllerId, accessToken, jocError);
        } catch (Exception e) {
            LOGGER.warn(e.toString());
        }
    }
    
    public synchronized void deleteDailyPlanCalendar() {
        Flux<JUpdateItemOperation> itemOperation = Flux.just(JUpdateItemOperation.deleteSimple(dailyPlanCalendarPath));
        for (String controllerId : Proxies.getControllerDbInstances().keySet()) {
            try {
                JControllerProxy proxy = Proxy.of(controllerId);
                proxy.api().updateItems(itemOperation).thenAccept(e -> {
                    ProblemHelper.postProblemEventIfExist(e, null, null, null);
                    if (e.isRight()) {
                        LOGGER.info("DailyPlanCalendar deleted on " + controllerId);
                    }
                });
            } catch (Exception e) {
                LOGGER.error(e.toString());
            }
        }
    }

    private void deployDailyPlanCalendar(JCalendar calendar, String curControllerId, String accessToken, JocError jocError) {

        Flux<JUpdateItemOperation> itemOperation = Flux.just(JUpdateItemOperation.addOrChangeSimple(calendar));
        for (String controllerId : Proxies.getControllerDbInstances().keySet()) {
            try {
                JControllerProxy proxy = Proxy.of(controllerId);
                Map<CalendarPath, JCalendar> knownCalendars = proxy.currentState().pathToCalendar();
                if (!dailyPlanCalendarIsAlreadySubmitted(knownCalendars, calendar)) {
                    proxy.api().updateItems(itemOperation).thenAccept(e -> {
                        if (curControllerId != null && controllerId.equals(curControllerId)) {
                            ProblemHelper.postProblemEventIfExist(e, accessToken, jocError, controllerId);
                        } else {
                            ProblemHelper.postProblemEventIfExist(e, null, null, null);
                        }
                        if (e.isRight()) {
                            LOGGER.info("DailyPlanCalendar submitted to " + controllerId);
                        }
                    });
                } else {
                    LOGGER.info("DailyPlanCalendar already submitted to " + controllerId);
                }
            } catch (Exception e) {
                ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, curControllerId);
            }
        };
    }
    
    private static boolean dailyPlanCalendarIsAlreadySubmitted(Map<CalendarPath, JCalendar> knownCalendars, JCalendar newCalendar) {
        JCalendar oldCalendar = knownCalendars.get(dailyPlanCalendarPath);
        if (oldCalendar != null) {
            if (oldCalendar.timezone().equals(newCalendar.timezone()) && oldCalendar.dateOffset().equals(newCalendar.dateOffset())) {
                return true;
            }
        }
        return false;
    }
    
    private static JCalendar getDailyPlanCalendar(ConfigurationGlobalsDailyPlan conf) {
        return getCalendar(getValue(conf.getTimeZone()), convertPeriodBeginToLong(getValue(conf.getPeriodBegin())));
    }
    
    private static JCalendar getCalendar(String timezone, long dateOffset) {
        return JCalendar.of(dailyPlanCalendarPath, ZoneId.of(timezone), Duration.ofSeconds(dateOffset), "#([^#]+)#.*", "yyyy-MM-dd");
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
