package com.sos.js7.history.controller.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.One;

public class EventFluxStopper {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventFluxStopper.class);

    private final One<Boolean> sinks;
    private final Mono<Boolean> mono;

    public EventFluxStopper() {
        // sinks = Sinks.many().multicast().directBestEffort();
        sinks = Sinks.one();
        mono = sinks.asMono();
    }

    public void stop() {
        LOGGER.debug("stop...");
        this.sinks.emitValue(true, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    public Mono<Boolean> stopped() {
        return mono.filter(v -> v.equals(Boolean.TRUE)).doOnNext(s -> {
            LOGGER.debug("stopped");
        });
    }
}
