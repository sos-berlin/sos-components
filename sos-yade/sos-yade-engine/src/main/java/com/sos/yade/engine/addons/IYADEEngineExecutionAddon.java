package com.sos.yade.engine.addons;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;

public interface IYADEEngineExecutionAddon {

    public void onBeforeDelegatorInitialized(ISOSLogger logger, AYADEArgumentsLoader argsLoader);

    public void onAfterSourceDelegatorConnected(YADESourceProviderDelegator sourceDelegator);

    public void onAfterTargetDelegatorConnected(YADETargetProviderDelegator targetDelegator);

    public void onBeforeDelegatorDisconnected(YADESourceProviderDelegator sourceDelegator, boolean isSourceDisconnectingEnabled,
            YADETargetProviderDelegator targetDelegator);
}
