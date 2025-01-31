package com.sos.yade.engine.handlers.commands;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.vfs.common.file.ProviderDirectoryPath;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.engine.delegators.YADEProviderFile;

/** The following special variables are available:<br/>
 * See:<br/>
 * YADE User Manual - Pre- & Post-Processing / Variables<br/>
 * YADE-448 Refactoring variables used in pre- and post-processing commands<br/>
 */
public class YADEFileCommandsVariablesResolver {

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

    public static String resolve(ProviderDirectoryPath sourceDirectory, ProviderDirectoryPath targetDirectory, ProviderFile file, String command) {
        Matcher matcher = VARIABLE_PATTERN.matcher(command);
        if (!matcher.find()) {
            return command;
        }

        YADEProviderFile yadeFile = (YADEProviderFile) file;
        StringBuilder result = new StringBuilder();
        matcher.reset();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(getVarValue(sourceDirectory, targetDirectory, yadeFile, matcher.group())));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String getVarValue(ProviderDirectoryPath sourceDirectory, ProviderDirectoryPath targetDirectory, YADEProviderFile file,
            String var) {
        switch (var) {
        /** Date/time variables */
        case "$date":
            return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        case "$time":
            return new SimpleDateFormat("HH:mm:ss").format(new Date());

        /** Directory variables */
        case "${TargetDirFullName}":
            return getOrDefault(targetDirectory);
        case "${SourceDirFullName}":
            return getOrDefault(sourceDirectory);

        /** The name of a file on the target host */
        case "${TargetFileFullName}":
            return file.getTarget() == null ? "" : getFullName(file.getTarget().getFullPath());
        case "${TargetFileRelativeName}":// TODO
            return file.getTarget() == null ? "" : getFullName(file.getTarget().getFullPath());
        case "${TargetFileBaseName}":
            return file.getTarget() == null ? "" : getBaseName(file.getTarget().getFullPath());
        case "${TargetFileParentFullName}":
            return file.getTarget() == null ? "" : getParentFullName(file.getTarget().getFullPath());
        case "${TargetFileParentBaseName}":
            return file.getTarget() == null ? "" : getParentBaseName(file.getTarget().getFullPath());

        /** The name of a file on the target host during transfer (a file name can be prefixed or suffixed) */
        case "${TargetTransferFileFullName}":
            return file.getTarget() == null ? "" : getFullName(file.getTarget().getNewFullPath());
        case "${TargetTransferFileRelativeName}":// TODO
            return file.getTarget() == null ? "" : getFullName(file.getTarget().getNewFullPath());
        case "${TargetTransferFileBaseName}":
            return file.getTarget() == null ? "" : getBaseName(file.getTarget().getNewFullPath());
        case "${TargetTransferFileParentFullName}":
            return file.getTarget() == null ? "" : getParentFullName(file.getTarget().getNewFullPath());
        case "${TargetTransferFileParentBaseName}":
            return file.getTarget() == null ? "" : getParentBaseName(file.getTarget().getNewFullPath());

        /** The name of a file on the source host */
        case "${SourceFileFullName}":
            return getFullName(file.getFullPath());
        case "${SourceFileRelativeName}":
            return var;// TODO
        case "${SourceFileBaseName}":
            return getBaseName(file.getFullPath());
        case "${SourceFileParentFullName}":
            return getParentFullName(file.getFullPath());
        case "${SourceFileParentBaseName}":
            return getParentBaseName(file.getFullPath());

        /** The name of a file on the source host after Rename operation */
        case "${SourceFileRenamedFullName}":
            return getFullName(file.getNewFullPath());
        case "${SourceFileRenamedRelativeName}":
            return var;// TODO
        case "${SourceFileRenamedBaseName}":
            return getBaseName(file.getNewFullPath());
        case "${SourceFileRenamedParentFullName}":
            return getParentFullName(file.getNewFullPath());
        case "${SourceFileRenamedParentBaseName}":
            return getParentBaseName(file.getNewFullPath());
        default:
            return var;
        }
    }

    private static String getFullName(String fullPath) {
        return getOrDefault(fullPath);
    }

    private static String getBaseName(String fullPath) {
        return getOrDefault(SOSPathUtil.getName(fullPath));
    }

    private static String getParentFullName(String fullPath) {
        return getOrDefault(SOSPathUtil.getParentPath(fullPath));
    }

    private static String getParentBaseName(String fullPath) {
        return getOrDefault(SOSPathUtil.getName(SOSPathUtil.getParentPath(fullPath)));
    }

    private static String getOrDefault(ProviderDirectoryPath val) {
        return val == null ? "" : getOrDefault(val.getPath());
    }

    private static String getOrDefault(String val) {
        return val == null ? "" : val;
    }

}
