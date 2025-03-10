package com.sos.yade.engine.handlers.command;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.delegators.AYADEProviderDelegator;
import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;

/** @apiNote The following special variables are available:<br/>
 *          See:<br/>
 *          YADE User Manual - Pre- & Post-Processing / Variables<br/>
 *          YADE-448 Refactoring variables used in pre- and post-processing commands<br/>
 */
public class YADEFileCommandVariablesResolver {

    private static final Set<String> VAR_NAMES = new HashSet<>();

    static {
        /** Date/time variables */
        /** $date - the current date in yyyy-MM-dd format */
        /** $time - the current time in Format HH:mm:ss format. */

        /** Only ${...} variables - without $date & $time */
        /** Directory variables */
        VAR_NAMES.add("TargetDirFullName"); // the directory where files are stored on the target system
        VAR_NAMES.add("SourceDirFullName"); // the directory where files are stored on the source system

        /** The name of a file on the target host */
        VAR_NAMES.add("TargetFileFullName");
        VAR_NAMES.add("TargetFileRelativeName");
        VAR_NAMES.add("TargetFileBaseName");
        VAR_NAMES.add("TargetFileParentFullName");
        VAR_NAMES.add("TargetFileParentBaseName");

        /** The name of a file on the target host during transfer (a file name can be prefixed or suffixed) */
        VAR_NAMES.add("TargetTransferFileFullName");
        VAR_NAMES.add("TargetTransferFileRelativeName");
        VAR_NAMES.add("TargetTransferFileBaseName");
        VAR_NAMES.add("TargetTransferFileParentFullName");
        VAR_NAMES.add("TargetTransferFileParentBaseName");

        /** The name of a file on the source host */
        VAR_NAMES.add("SourceFileFullName");
        VAR_NAMES.add("SourceFileRelativeName");
        VAR_NAMES.add("SourceFileBaseName");
        VAR_NAMES.add("SourceFileParentFullName");
        VAR_NAMES.add("SourceFileParentBaseName");

        /** The name of a file on the source host after Rename operation */
        VAR_NAMES.add("SourceFileRenamedFullName");
        VAR_NAMES.add("SourceFileRenamedRelativeName");
        VAR_NAMES.add("SourceFileRenamedBaseName");
        VAR_NAMES.add("SourceFileRenamedParentFullName");
        VAR_NAMES.add("SourceFileRenamedParentBaseName");
    }

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$(date|time)|\\$\\{(" + String.join("|", VAR_NAMES) + ")\\}");

    public static String resolve(YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator, ProviderFile providerFile,
            String command) {
        Matcher matcher = VARIABLE_PATTERN.matcher(command);
        if (!matcher.find()) {
            return command;
        }

        YADEProviderFile file = (YADEProviderFile) providerFile;
        StringBuilder result = new StringBuilder();
        matcher.reset();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(getVarValue(sourceDelegator, targetDelegator, file, matcher.group())));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String getVarValue(YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator, YADEProviderFile file,
            String var) {

        boolean sourceFileNotNeedsRename = !file.needsRename();

        switch (var) {
        /** Date/time variables */
        case "$date":
            return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        case "$time":
            return new SimpleDateFormat("HH:mm:ss").format(new Date());

        /** Directory variables */
        case "${TargetDirFullName}":
            return getDirectoryPath(targetDelegator);
        case "${SourceDirFullName}":
            return getDirectoryPath(sourceDelegator);

        /** The name of a file on the target host */
        case "${TargetFileFullName}":
            return file.getTarget() == null ? "" : getFileFullPath(file.getTarget().getFinalFullPath());
        case "${TargetFileRelativeName}":
            return file.getTarget() == null ? "" : getFileRelativePath(targetDelegator, file.getTarget().getFinalFullPath());
        case "${TargetFileBaseName}":
            return file.getTarget() == null ? "" : getFileBaseName(file.getTarget().getFinalFullPath());
        case "${TargetFileParentFullName}":
            return file.getTarget() == null ? "" : getFileParentFullPath(targetDelegator, file.getTarget().getFinalFullPath());
        case "${TargetFileParentBaseName}":
            return file.getTarget() == null ? "" : getFileParentBaseName(targetDelegator, file.getTarget().getFinalFullPath());

        /** The name of a file on the target host during transfer (a file name can be prefixed or suffixed) */
        case "${TargetTransferFileFullName}":
            return file.getTarget() == null ? "" : getFileFullPath(file.getTarget().getFullPath());
        case "${TargetTransferFileRelativeName}":
            return file.getTarget() == null ? "" : getFileRelativePath(targetDelegator, file.getTarget().getFullPath());
        case "${TargetTransferFileBaseName}":
            return file.getTarget() == null ? "" : getFileBaseName(file.getTarget().getFullPath());
        case "${TargetTransferFileParentFullName}":
            return file.getTarget() == null ? "" : getFileParentFullPath(targetDelegator, file.getTarget().getFullPath());
        case "${TargetTransferFileParentBaseName}":
            return file.getTarget() == null ? "" : getFileParentBaseName(targetDelegator, file.getTarget().getFullPath());

        /** The name of a file on the source host */
        case "${SourceFileFullName}":
            return getFileFullPath(file.getFullPath());
        case "${SourceFileRelativeName}":
            return getFileRelativePath(sourceDelegator, file.getFullPath());
        case "${SourceFileBaseName}":
            return getFileBaseName(file.getFullPath());
        case "${SourceFileParentFullName}":
            return getFileParentFullPath(sourceDelegator, file.getFullPath());
        case "${SourceFileParentBaseName}":
            return getFileParentBaseName(sourceDelegator, file.getFullPath());

        /** The name of a file on the source host after Rename operation */
        case "${SourceFileRenamedFullName}":
            return sourceFileNotNeedsRename ? "" : getFileFullPath(file.getFinalFullPath());
        case "${SourceFileRenamedRelativeName}":
            return sourceFileNotNeedsRename ? "" : getFileRelativePath(sourceDelegator, file.getFinalFullPath());
        case "${SourceFileRenamedBaseName}":
            return sourceFileNotNeedsRename ? "" : getFileBaseName(file.getFinalFullPath());
        case "${SourceFileRenamedParentFullName}":
            return sourceFileNotNeedsRename ? "" : getFileParentFullPath(sourceDelegator, file.getFinalFullPath());
        case "${SourceFileRenamedParentBaseName}":
            return sourceFileNotNeedsRename ? "" : getFileParentBaseName(sourceDelegator, file.getFinalFullPath());
        default:
            return var;
        }
    }

    private static String getDirectoryPath(IYADEProviderDelegator delegator) {
        if (delegator == null || delegator.getDirectory() == null) {
            return getOrDefault(null);
        }
        return delegator.getDirectory();
    }

    private static String getFileFullPath(String fullPath) {
        return getOrDefault(fullPath);
    }

    // TODO check...
    private static String getFileRelativePath(IYADEProviderDelegator delegator, String fullPath) {
        if (delegator == null) {
            return getOrDefault(null);
        }
        if (delegator.getDirectory() == null) {
            return getFileBaseName(fullPath);
        }
        if (fullPath == null) {
            return getOrDefault(fullPath);
        }
        if (fullPath.startsWith(delegator.getDirectoryWithTrailingPathSeparator())) {
            return fullPath.substring(delegator.getDirectoryWithTrailingPathSeparator().length());
        }
        return getOrDefault(fullPath);
    }

    private static String getFileBaseName(String fullPath) {
        return getOrDefault(SOSPathUtil.getName(fullPath));
    }

    private static String getFileParentFullPath(AYADEProviderDelegator delegator, String fullPath) {
        return getOrDefault(delegator.getParentPath(fullPath));
    }

    private static String getFileParentBaseName(AYADEProviderDelegator delegator, String fullPath) {
        return getOrDefault(SOSPathUtil.getName(delegator.getParentPath(fullPath)));
    }

    private static String getOrDefault(String val) {
        return val == null ? "" : val;
    }

}
