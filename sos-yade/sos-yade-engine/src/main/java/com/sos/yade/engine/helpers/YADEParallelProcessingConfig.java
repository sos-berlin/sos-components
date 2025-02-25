package com.sos.yade.engine.helpers;

import com.sos.yade.engine.arguments.YADEArguments;

public class YADEParallelProcessingConfig {

    private final boolean auto;
    private final int maxThreads;

    private YADEParallelProcessingConfig(int maxThreads) {
        this.auto = maxThreads <= 0;
        this.maxThreads = maxThreads;
    }

    public boolean isAuto() {
        return auto;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    @Override
    public String toString() {
        if (auto) {
            return "auto=true";
        }
        return "maxThreads=" + maxThreads;
    }

    public static YADEParallelProcessingConfig createInstance(YADEArguments args) {
        if (args == null || args.getParallelMaxThreads().isEmpty()) {
            return null;
        }

        if ("AUTO".equalsIgnoreCase(args.getParallelMaxThreads().getValue())) {
            return new YADEParallelProcessingConfig(-1);
        }
        try {
            int val = Integer.valueOf(args.getParallelMaxThreads().getValue()).intValue();
            return val <= 1 ? null : new YADEParallelProcessingConfig(val);
        } catch (Throwable e) {
            return null;
        }
    }
}
