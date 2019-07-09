package com.sos.commons.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSClassList {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSClassList.class);

    private final ClassLoader classLoader;
    private final Set<Class<?>> classes;

    public SOSClassList() {
        classes = new HashSet<Class<?>>();
        classLoader = ClassLoader.getSystemClassLoader();
    }

    public void addClassIfExist(String className) {
        try {
            Class<?> c = classLoader.loadClass(className);
            add(c);
        } catch (ClassNotFoundException e) {
            LOGGER.warn(String.format("Class %s not found in the classpath", className));
        }
    }

	public void add(Class<?> c) {
		classes.add(c);
	}

	public void merge(Collection<Class<?>> classesToMerge) {
		classes.addAll(classesToMerge);
	}

	public Set<Class<?>> getClasses() {
		return classes;
	}

}