package com.iyte_yazilim.proje_pazari.application.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * Marks a class as a request handler for the Mediator pattern.
 *
 * <p>Classes annotated with @Handler are automatically discovered by the Mediator and registered as
 * Spring beans. This annotation is a specialization of @Component, allowing handlers to be
 * dependency-injected by Spring while being semantically identified as request handlers.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Handler {}
