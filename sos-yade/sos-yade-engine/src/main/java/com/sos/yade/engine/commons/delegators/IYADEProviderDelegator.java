package com.sos.yade.engine.commons.delegators;

import com.sos.commons.vfs.commons.AProvider;
import com.sos.yade.engine.commons.arguments.YADESourceTargetArguments;

public interface IYADEProviderDelegator {

    public AProvider<?, ?> getProvider();

    public YADESourceTargetArguments getArgs();

    public String getLabel();

    /** Directory path without trailing path separator */
    public String getDirectory();

    /** Directory path with trailing path separator */
    public String getDirectoryWithTrailingPathSeparator();
}
