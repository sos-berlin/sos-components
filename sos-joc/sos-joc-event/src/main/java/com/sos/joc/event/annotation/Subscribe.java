package com.sos.joc.event.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.sos.joc.event.bean.JOCEvent;


@Retention(RUNTIME)
@Target(METHOD)
public @interface Subscribe {
    Class<? extends JOCEvent>[] value() default JOCEvent.class;
}
