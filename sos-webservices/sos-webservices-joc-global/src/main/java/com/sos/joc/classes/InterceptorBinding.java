package com.sos.joc.classes;

import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;

public class InterceptorBinding implements DynamicFeature {
 
    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        context.register(InterceptorWithRequestLogging.class);
    }
}
