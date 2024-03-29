package com.sos.joc.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.JOCEvent;


public class EventBus {

    private static EventBus eventBus;
    private static final Logger LOGGER = LoggerFactory.getLogger(EventBus.class);
    private static final String threadNamePrefix = "Thread-EventBus-";
    private CopyOnWriteArraySet<Object> listeners = new CopyOnWriteArraySet<>();
    
    private EventBus() {
    }
    
    public static synchronized EventBus getInstance() {
        if (eventBus == null) {
            eventBus = new EventBus(); 
        }
        return eventBus;
    }

    public synchronized void register(final Object listener) {
        if (listener != null && hasSubcribedMethod(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void unRegister(final Object listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public void post(final JOCEvent evt) {
        if (evt != null) {
            new Thread(() -> {
                Set<Object> unsubcribedListeners = new HashSet<>();
                evt.setEventId(Instant.now().toEpochMilli());
                Collections.unmodifiableSet(listeners).stream().forEach(listener -> {
                    if (!invokeSubcribedMethods(listener, evt)) {
                        unsubcribedListeners.add(listener);
                    }
                });
                if (!unsubcribedListeners.isEmpty()) {
                    listeners.removeAll(unsubcribedListeners);
                } 
            }).start();
        }
    }

    private static boolean hasSubcribedMethod(final Object listener) {
        boolean subcribedMethodFound = false;
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Subscribe.class)) {
                Parameter[] params = method.getParameters();
                if (params.length > 0 && JOCEvent.class.isAssignableFrom(params[0].getType())) {
                    subcribedMethodFound = true;
                    break;
                }
            }
        }
        return subcribedMethodFound;
    }

    private static boolean invokeSubcribedMethods(final Object listener, final JOCEvent evt) {
        boolean subcribedMethodFound = false;
        AtomicInteger threadNameSuffix = new AtomicInteger(0);
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Subscribe.class)) {
                subcribedMethodFound = true;
                Subscribe annotation = method.getAnnotation(Subscribe.class);
                boolean eventIsDesired = false;
                Parameter[] params = method.getParameters();
                if (params.length > 0 && params[0].getType().isAssignableFrom(evt.getClass())) {
                    for (Class<? extends JOCEvent> evtClazz : annotation.value()) {
                        if (evtClazz.isAssignableFrom(evt.getClass())) {
                            eventIsDesired = true;
                            break;
                        }
                    } 
                }
                if (eventIsDesired) {
                    new Thread(() -> {
                        try {
                            method.invoke(listener, evt);
                        } catch (IllegalAccessException e) {
                            LOGGER.warn(String.format("EventBus tried to invoke method '%s' of listener '%s' but it is not 'public: %s'", method
                                    .getName(), listener.getClass().getName(), e.toString()));
                        } catch (InvocationTargetException e) {
                            if (e.getCause() != null) {
                                LOGGER.warn(String.format("EventBus tried to invoke method '%s' of listener '%s' caused by '%s': %s", method
                                        .getName(), listener.getClass().getName(), evt.getClass().getSimpleName(), e.getCause().toString()));
                            } else {
                                LOGGER.warn(String.format("EventBus tried to invoke method '%s' of listener '%s' caused by '%s': %s", method
                                        .getName(), listener.getClass().getName(), evt.getClass().getSimpleName(), e.toString()));
                            }
                        } catch (Exception e) {
                            LOGGER.warn(String.format("EventBus tried to invoke method '%s' of listener '%s' caused by '%s': %s", method.getName(),
                                    listener.getClass().getName(), evt.getClass().getSimpleName(), e.toString()));
                        }
                    }, threadNamePrefix + threadNameSuffix.incrementAndGet()).start();
                }
            }
        }
        return subcribedMethodFound;
    }

}
