package com.sos.yade.engine.handlers.commands;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sos.commons.vfs.common.file.ProviderDirectoryPath;
import com.sos.commons.vfs.common.file.ProviderFile;

/** The following special variables are available:<br/>
 * See:<br/>
 * YADE User Manual - Pre- & Post-Processing / Variables<br/>
 * YADE-448 Refactoring variables used in pre- and post-processing commands<br/>
 * 
 * Date/time variables: <br/>
 * $date - the current date in yyyy-MM-dd format.<br/>
 * $time - the current time in Format HH:mm:ss format.<br/>
 *
 * Directory variables: <br/>
 * ${TargetDirFullName} - the directory where files are stored on the target system.<br/>
 * ${SourceDirFullName} - the directory where files are stored on the source system.<br/>
 *
 * The name of a file on the target host: <br/>
 * ${TargetFileFullName}<br/>
 * ${TargetFileRelativeName}<br/>
 * ${TargetFileBaseName}<br/>
 * ${TargetFileParentFullName}<br/>
 * ${TargetFileParentBaseName}<br/>
 *
 * The name of a file on the target host during transfer (a file name can be prefixed or suffixed): <br/>
 * ${TargetTransferFileFullName}<br/>
 * ${TargetTransferFileRelativeName}<br/>
 * ${TargetTransferFileBaseName}<br/>
 * ${TargetTransferFileParentFullName}<br/>
 * ${TargetTransferFileParentBaseName}<br/>
 *
 * The name of a file on the source host: <br/>
 * ${SourceFileFullName}<br/>
 * ${SourceFileRelativeName}<br/>
 * ${SourceFileBaseName}<br/>
 * ${SourceFileParentFullName}<br/>
 * ${SourceFileParentBaseName}<br/>
 * 
 * The name of a file on the source host after Rename operation: <br/>
 * ${SourceFileRenamedFullName}<br/>
 * ${SourceFileRenamedRelativeName}<br/>
 * ${SourceFileRenamedBaseName}<br/>
 * ${SourceFileRenamedParentFullName}<br/>
 * ${SourceFileRenamedParentBaseName}<br/>
 */
public class YADEFileCommandsVariablesResolver {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\w+|\\$\\{\\w+\\}");

    public static String resolve(ProviderDirectoryPath sourceDirectory, ProviderDirectoryPath targetDirectory, ProviderFile file, String command) {
        Matcher matcher = VARIABLE_PATTERN.matcher(command);
        if (!matcher.find()) {
            return command;
        }

        StringBuilder result = new StringBuilder();
        matcher.reset();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(getVarValue(sourceDirectory, targetDirectory, file, matcher.group())));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String getVarValue(ProviderDirectoryPath sourceDirectory, ProviderDirectoryPath targetDirectory, ProviderFile file, String var) {
        switch (var) {
        case "$date":
            return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        case "$time":
            return new SimpleDateFormat("HH:mm:ss").format(new Date());
        case "${TargetDirFullName}":
            return getOrDefault(targetDirectory);
        case "${SourceDirFullName}":
            return getOrDefault(sourceDirectory);
        default:
            return var;
        }
    }

    private static String getOrDefault(ProviderDirectoryPath val) {
        return val == null ? "" : getOrDefault(val.getPath());
    }

    private static String getOrDefault(String val) {
        return val == null ? "" : val;
    }

}
