package com.sos.commons.hibernate.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.hibernate.annotations.IdGeneratorType;

import com.sos.commons.hibernate.generator.SOSHibernateIdGeneratorImpl;

@Retention(RUNTIME)
@Target({ FIELD, METHOD })
@IdGeneratorType(SOSHibernateIdGeneratorImpl.class)
public @interface SOSIdGenerator {

    String sequenceName();
}
