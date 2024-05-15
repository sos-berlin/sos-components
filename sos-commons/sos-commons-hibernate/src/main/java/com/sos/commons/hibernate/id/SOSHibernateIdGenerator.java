package com.sos.commons.hibernate.id;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.hibernate.annotations.IdGeneratorType;

@Retention(RUNTIME)
@Target({ FIELD, METHOD })
@IdGeneratorType(SOSHibernateIdGeneratorImpl.class)
public @interface SOSHibernateIdGenerator {

    String sequenceName();
}
