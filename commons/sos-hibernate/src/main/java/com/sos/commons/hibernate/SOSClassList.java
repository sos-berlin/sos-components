package com.sos.commons.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSClassList {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSClassList.class);

    private final ClassLoader classLoader;
    @SuppressWarnings("rawtypes")
    private final List<Class> classes;

    @SuppressWarnings("rawtypes")
    public SOSClassList() {
        this.classes = new ArrayList<Class>();
        this.classLoader = ClassLoader.getSystemClassLoader();
    }

    @SuppressWarnings("rawtypes")
    public void addClassIfExist(String className) {
        try {
            Class c = classLoader.loadClass(className);
            add(c);
        } catch (ClassNotFoundException e) {
            LOGGER.warn(String.format("Class %s not found in the classpath", className));
        }
    }

    @SuppressWarnings("rawtypes")
    public void add(Class c) {
        if (!classes.contains(c)) {
            classes.add(c);
        }
    }

    @SuppressWarnings("rawtypes")
    public void merge(List<Class> classesToMerge) {
        for (Class c : classesToMerge) {
            add(c);
        }
    }

    @SuppressWarnings("rawtypes")
    public List<Class> getClasses() {
        return classes;
    }

}