package com.sos.joc.bean;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.classes.proxy.Proxies;

public class JOCMBeanServer {

    private static JOCMBeanServer instance;
    private static final String namespace = "joc:type=";
    private static final List<Class<? extends IJocMBean>> mBeansControllerSpecific = Arrays.asList(OrdersSnapshot.class, HistorySummary.class,
            DailyPlanSummary.class);
    private static final List<Class<? extends IJocMBean>> mBeans = Collections.emptyList();
    private static final MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
    private Set<ObjectName> registered = new HashSet<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(JOCMBeanServer.class);

    private JOCMBeanServer() {
    }

    public static JOCMBeanServer getInstance() {
        if (instance == null) {
            instance = new JOCMBeanServer();
        }
        return instance;
    }

    public static void register() {
        JOCMBeanServer.getInstance()._register();
    }
    
    public static void update(String controllerId) {
        JOCMBeanServer.getInstance()._update(controllerId);
    }

    public static void unregister() { //called during JOC shutdown
        try {
            JOCMBeanServer.getInstance()._unregister();
        } catch (Exception e) {
            LOGGER.warn("Error at unregister JOC metrics: " + e.toString());
        }
    }
    
    private void _update(String controllerId) {
        for (Class<? extends IJocMBean> clazz : mBeansControllerSpecific) {
//            try {
//                Method m = clazz.getDeclaredMethod("getInstance", String.class);
//                registerPerController(controllerId, m);
//            } catch (Exception e) {
//                LOGGER.error("Error at register Mbean " + clazz.getSimpleName() + "(" + controllerId + "): ", e);
//            }
            registerPerController(controllerId, clazz);
        }
    }

    private void _register() {
        
        for (Class<? extends IJocMBean> clazz : mBeans) {
            try {
                // Method m = clazz.getDeclaredMethod("getInstance");
                // IJocMBean obj = (IJocMBean) m.invoke(null);
                IJocMBean obj = clazz.getDeclaredConstructor().newInstance();
                ObjectName oName = new ObjectName(namespace + obj.objectName());
                if (registered.contains(oName)) {
                    continue;
                }
                LOGGER.debug("try register Mbean " + obj.objectName());
                registerMBean(obj, oName);
            } catch (Exception e) {
                LOGGER.error("Error at register Mbean " + clazz.getSimpleName(), e);
            }
        }
        for (Class<? extends IJocMBean> clazz : mBeansControllerSpecific) {
            // Method m = clazz.getDeclaredMethod("getInstance", String.class);
            for (String controllerId : Proxies.getControllerDbInstances().keySet()) {
                // registerPerController(controllerId, m);
                registerPerController(controllerId, clazz);
            }
        }
    }

    private void registerPerController(String controllerId, Class<? extends IJocMBean> clazz) {
        try {
            IJocMBean obj = clazz.getDeclaredConstructor(String.class).newInstance(controllerId);
            ObjectName oName = new ObjectName(namespace + obj.objectName() + ",controllerId=" + controllerId);
            if (registered.contains(oName)) {
                return;
            }
            LOGGER.debug("try register Mbean " + obj.objectName() + "(" + controllerId + ")");
            registerMBean(obj, oName);
        } catch (Exception e) {
            LOGGER.error("Error at register Mbean " + clazz.getSimpleName() + "(" + controllerId + "): ", e);
        }
    }

//    private void registerPerController(String controllerId, Method m) throws IllegalAccessException, IllegalArgumentException,
//            InvocationTargetException, MalformedObjectNameException, MBeanRegistrationException, NotCompliantMBeanException {
//        IJocMBean obj = (IJocMBean) m.invoke(null, controllerId);
//        ObjectName oName = new ObjectName(namespace + obj.objectName() + ",controllerId=" + controllerId);
//        if (registered.contains(oName)) {
//            return;
//        }
//        LOGGER.debug("try register Mbean " + obj.objectName() + "(" + controllerId + ")");
//        registerMBean(obj, oName);
//    }
    
    private boolean registerMBean(IJocMBean obj, ObjectName oName) throws MBeanRegistrationException, NotCompliantMBeanException {
        try {
            beanServer.registerMBean(obj, oName);
            registered.add(oName);
        } catch (InstanceAlreadyExistsException e) {
            //
        }
        return true;
    }

    private void _unregister() throws MBeanRegistrationException {
        for (ObjectName objName : registered) {
            try {
                beanServer.unregisterMBean(objName);
            } catch (InstanceNotFoundException e) {
                //
            }
        }
        registered.clear();
    }
}
