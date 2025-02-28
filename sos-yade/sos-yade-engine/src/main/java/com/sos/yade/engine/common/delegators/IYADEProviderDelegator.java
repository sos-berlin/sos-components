package com.sos.yade.engine.common.delegators;

import com.sos.commons.vfs.common.IProvider;
import com.sos.yade.engine.common.arguments.YADESourceTargetArguments;

public interface IYADEProviderDelegator {

    public IProvider getProvider();

    public YADESourceTargetArguments getArgs();

    /** Directory path without trailing path separator */
    public String getDirectory();

    /** Directory path with trailing path separator */
    public String getDirectoryWithTrailingPathSeparator();

    public String getIdentifier();

    public String getLogPrefix();
}
