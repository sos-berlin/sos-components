package com.sos.yade.engine;

import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.yade.engine.common.TransferArguments;
import com.sos.yade.engine.common.YADEEngineHelper;
import com.sos.yade.engine.common.YADEEnginePolling;

public class YADEEngine {

    private final ISOSLogger logger;
    private final TransferArguments args;

    public YADEEngine(ISOSLogger logger, TransferArguments args) {
        this.logger = logger;
        this.args = args;
    }

    public void execute() throws Exception {
        IProvider source = null;
        IProvider target = null;

        try {
            // 1 - print transfer configuration
            YADEEngineHelper.printBanner(logger, args);

            // 2 - check/initialize configuration/connect
            YADEEngineHelper.checkArguments(args);
            YADEEngineHelper.setConfiguredSystemProperties(logger, args);
            source = YADEEngineHelper.getProvider(logger, args, true);
            target = YADEEngineHelper.getProvider(logger, args, false);
            connect(source, target);

            // 3 - transfer
            transfer(source, target);

            // 4 - disconnect
            disconnect(source, target);
            source = null;
            target = null;
        } catch (Throwable e) {
            throw e;
        } finally {
            disconnect(source, target);// if exception
            // 5 - print summary
            YADEEngineHelper.printSummary(logger, args);
        }
    }

    private void transfer(IProvider source, IProvider target) throws Exception {
        try {
            YADEEnginePolling polling = new YADEEnginePolling(logger, args, source);
            if (polling.enabled()) {
                String[] fileList = polling.doPolling();
                // ...
                if (target != null) {
                    target.ensureConnected();
                }
            }

        } catch (Throwable e) {
            throw e;
        } finally {

        }
    }

    private void connect(IProvider source, IProvider target) throws SOSProviderException {
        if (source != null) {
            source.connect();
        }
        if (target != null) {
            target.connect();
        }
    }

    /** Provider Disconnect does not throw exceptions - but logs with the type (source/destination) when occurred/disconnect executed
     * 
     * @param source
     * @param target */
    private void disconnect(IProvider source, IProvider target) {
        if (source != null) {
            source.disconnect();
        }
        if (target != null) {
            target.disconnect();
        }
    }

}
