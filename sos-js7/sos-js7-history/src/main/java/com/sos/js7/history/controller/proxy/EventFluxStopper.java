package com.sos.js7.history.controller.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

public class EventFluxStopper {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventFluxStopper.class);
    private final FluxProcessor<Boolean, Boolean> processor;
    private final FluxSink<Boolean> sink;

    public EventFluxStopper() {
        processor = DirectProcessor.create();
        sink = processor.sink();
    }

    /** Signals that all existing streams should be stopped */
    public void stop() {
        LOGGER.debug("stop...");
        this.sink.next(true);
    }

    /** Returns a Mono that emits when should be stopped */
    // public Mono<Boolean> onStop(Boolean close) {
    // return processor.filter(close::equals).doOnNext(s -> {
    // LOGGER.debug("onStop:" + s);
    // }).next();
    // }

    public Mono<Boolean> stopped() {
        return processor.filter(v -> v.equals(Boolean.TRUE)).doOnNext(s -> {
            LOGGER.debug("stopped");
        }).next();
    }
}
