package com.sos.yade.engine.handlers.operations.copymove.file.common;

import com.sos.commons.util.SOSPathUtil;
import com.sos.yade.engine.common.delegators.AYADEProviderDelegator;
import com.sos.yade.engine.handlers.operations.copymove.file.helpers.YADEFileReplacementHelper;

/** @see YADEFileReplacementHelper
 * @apiNote Usage: COPY/MOVE operations.<br/>
 *          The main reason for implementing this class is the YADE Replacement functionality:<br/>
 *          - The Replacement is based on a file name (not the entire path), but the Replacement can define a different parent path than the original<br/>
 *          -- Example: replacing=(^.*$), replacement=/sub/$1, replacement=sub/$1, replacement=../sub/$1, replacement=X://sub/$1 ...<br/>
 *          - Note LocalProvider Windows:<br/>
 *          -- Replacement paths that begin with \ or / are not absolute paths (such as nio Path.of("/")).<br/>
 *          --- This means:<br/>
 *          ---- /sub/$1 or sub/$1 is the same and the sub folder will be placed in the target directory (if defined) or the working directory.<br/>
 *          --- To "create" the sub directory absolutely, the Replacement should define an absolute path, e.g.:<br/>
 *          ---- X://sub/$1, X:\sub\$1, \\server\sub\$1, ...<br/>
 */
public class YADEFileNameInfo {

    private String name;
    private String parent;
    private String path;
    private boolean absolutePath;

    public YADEFileNameInfo(final AYADEProviderDelegator delegator, final String fileNameOrPath) {
        String formatted = delegator.getProvider().toPathStyle(fileNameOrPath);
        if (delegator.containsParentPath(formatted)) {
            name = SOSPathUtil.getName(formatted);
            parent = delegator.getParentPath(formatted);
            // parent is not null - due to normalized.contains(...)
            path = delegator.appendPath(parent, name);
            // absolutePath = path.startsWith(delegator.getProvider().getPathSeparator()); <- see class description
            absolutePath = delegator.getProvider().isAbsolutePath(parent);
        } else {
            name = formatted;
            parent = null;
            path = null;
            absolutePath = false;
        }
    }

    public String getName() {
        return name;
    }

    public String getParent() {
        return parent;
    }

    public boolean needsParent() {
        return parent != null;
    }

    public String getPath() {
        return path;
    }

    public boolean isAbsolutePath() {
        return absolutePath;
    }

}
