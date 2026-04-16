package com.sos.commons.hibernate.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.hibernate.annotations.ValueGenerationType;

import com.sos.commons.hibernate.generator.SOSHibernateCurrentTimestampUtcGeneratorImpl;

/** Generates the current database UTC timestamp only when the entity is inserted.
 * <p>
 * This is a specialized version of {@link SOSCurrentTimestampUtc} for insert-only timestamps.<br />
 * Equivalent to: {@code @SOSCurrentTimestampUtc(event = EventType.INSERT)}
 * <p>
 * Example:
 * 
 * <pre>
 * 
 * &#64;SOSCreationTimestampUtc
 * &#64;Column(name = "[CREATED]", nullable = false, updatable = true)
 * private Instant created;
 * </pre>
 * <p>
 * <b>Note:</b> {@code @Column(updatable = true)} is ignored because the generator never performs UPDATE operations.
 *
 * @see SOSCurrentTimestampUtc
 * @see org.hibernate.generator.EventType#INSERT */
@ValueGenerationType(generatedBy = SOSHibernateCurrentTimestampUtcGeneratorImpl.class)
@Target({ FIELD, METHOD })
@Retention(RUNTIME)
public @interface SOSCreationTimestampUtc {

}
