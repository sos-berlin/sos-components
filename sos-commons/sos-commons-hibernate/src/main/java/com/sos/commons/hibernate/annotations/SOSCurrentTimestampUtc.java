package com.sos.commons.hibernate.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.hibernate.annotations.ValueGenerationType;
import org.hibernate.generator.EventType;

import com.sos.commons.hibernate.generator.SOSHibernateCurrentTimestampUtcGeneratorImpl;

/** Generates the current database UTC timestamp for the annotated field.
 * <p>
 * The timestamp is generated directly by the database, ensuring UTC timezone consistency regardless of the application server's timezone or database session
 * settings.
 * <p>
 * <b>Event types (default: INSERT and UPDATE):</b>
 * <ul>
 * <li>{@link EventType#INSERT} - generate timestamp on insert</li>
 * <li>{@link EventType#UPDATE} - generate timestamp on update</li>
 * </ul>
 * <p>
 * <b>Interaction with {@code @Column} settings:</b>
 * <p>
 * When this annotation declares an {@code event()} type (e.g., {@code EventType.INSERT}), the corresponding {@code @Column(insertable=false)} or
 * {@code @Column(updatable=false)} settings are <b>ignored</b>.<br />
 * The generator takes precedence for the declared event types.
 * <p>
 * Example:
 * 
 * <pre>
 * 
 * &#64;Column(insertable = false)  // ignored for INSERT due to annotation below
 * &#64;SOSCurrentTimestampUtc(event = EventType.INSERT)
 * private Instant created;
 * </pre>
 * 
 * @see SOSCreationTimestampUtc
 * @see org.hibernate.generator.EventType
 * @see jakarta.persistence.Column */
@ValueGenerationType(generatedBy = SOSHibernateCurrentTimestampUtcGeneratorImpl.class)
@Target({ FIELD, METHOD })
@Retention(RUNTIME)
public @interface SOSCurrentTimestampUtc {

    EventType[] event() default { EventType.INSERT, EventType.UPDATE };
}
