package com.sos.yade.engine.delegators;

import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.common.file.ProviderDirectoryPath;
import com.sos.yade.engine.arguments.YADESourceTargetArguments;

public interface IYADEProviderDelegator {

    public IProvider getProvider();

    public YADESourceTargetArguments getArgs();

    public ProviderDirectoryPath getDirectory();

    public String getIdentifier();

    public String getLogPrefix();
}
