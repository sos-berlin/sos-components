package com.sos.joc.bean;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JOCMBeanServer {

    private static JOCMBeanServer instance;
    private static final String namespace = "joc:type=";
    private static final List<IJocMBean> mBeans = Arrays.asList(OrdersSnapshot.getInstance());
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
        try {
            JOCMBeanServer.getInstance()._register();
        } catch (Exception e) {
            LOGGER.error("Error at register JOC metrics: ", e);
        }
    }

    public static void unregister() {
        try {
            JOCMBeanServer.getInstance()._unregister();
        } catch (Exception e) {
            LOGGER.error("Error at unregister JOC metrics: ", e);
        }
    }

    public static void registerOrThrow() throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException {
        JOCMBeanServer.getInstance()._register();
    }

    public static void unregisterOrThrow() throws MBeanRegistrationException, MalformedObjectNameException {
        JOCMBeanServer.getInstance()._unregister();
    }

    private void _register() throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException {
        for (IJocMBean obj : mBeans) {
            ObjectName oName = new ObjectName(namespace + obj.objectName());
            if (registered.contains(oName)) {
                continue;
            }
            beanServer.registerMBean(obj, oName);
            registered.add(oName);
        }
    }

    private void _unregister() throws MBeanRegistrationException {
        for (ObjectName objName : registered) {
            try {
                beanServer.unregisterMBean(objName);
                registered.remove(objName);
            } catch (InstanceNotFoundException e) {
                //
            }
        }
    }
}
