package com.sos.yade.engine.handlers.operations.copymove.file.helpers;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.logger.SOSSlf4jLogger;
import com.sos.commons.vfs.local.LocalProvider;
import com.sos.commons.vfs.local.commons.LocalProviderArguments;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.delegators.AYADEProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.handlers.operations.copymove.file.commons.YADEFileNameInfo;

/** @see YADEFileNameInfo
 * @apiNote COPY/MOVE operations.<br/>
 *          - A masked replacement supports masks for substitution in the filename with format strings that are enclosed with [ and ].<br/>
 *          -- The following format strings are supported:<br/>
 *          --- [date:<date format>] - date format must be a valid Java data format string, e.g. yyyyMMddHHmmss, yyyy-MM-dd.HHmmss etc.<br/>
 *          --- [filename:] - will be substituted by the original file name including the file extension<br/>
 *          --- [filename:lowercase] - will be substituted by the original file name including the file extension with all characters converted to lower
 *          case.<br/>
 *          --- [filename:uppercase] - will be substituted by the original file name including the file extension with all characters converted to upper
 *          case.<br/>
 *          - Use with capturing groups:<br/>
 *          -- For replacement "capturing groups" are used. Only the content of the capturing groups is replaced.<br/>
 *          -- Multiple replacements are separated by a semicolon ";".<br/>
 *          -- Example 1: regex:(1)abc(12)def(.*), replacement:A;BB;CCC<br/>
 *          --- Result for "1abc12def123.txt":<br/>
 *          ---- AabcBBdefCCC (the same parent path as before)<br/>
 *          -- Example 2: regex=(^.*$), replacement=X:/sub/$1<br/>
 *          --- Result for "1abc12def123.txt":<br/>
 *          ---- the same name but the parent path is "X:/sub"<br/>
 */
public class YADEFileReplacementHelper {

    private static final String VAR_DATE_PREFIX = "[date:";
    private static final String VAR_FILENAME_PREFIX = "[filename:";

    public static Optional<YADEFileNameInfo> getReplacementResultIfDifferent(AYADEProviderDelegator delegator, String fileName, String regex,
            String replacement) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileName);
        if (!matcher.find()) {
            return Optional.empty();
        }

        String newName;
        if (matcher.groupCount() == 0) {
            newName = replaceVariables(replacement, fileName);
        } else {
            String[] replacements = replacement.split(";");
            StringBuilder result = new StringBuilder();
            int lastEnd = 0;
            for (int i = 1; i <= matcher.groupCount(); i++) {
                result.append(fileName, lastEnd, matcher.start(i));
                String groupReplacement = (i - 1 < replacements.length) ? replacements[i - 1] : replacements[replacements.length - 1];
                groupReplacement = replaceVariables(groupReplacement, fileName);

                // dynamic replacement of all place holders ($1, $2, ...)
                // YADE JS7: replaceGroupReferences is new and not available with YADE1...
                groupReplacement = replaceGroupReferences(groupReplacement, matcher);

                result.append(groupReplacement);
                lastEnd = matcher.end(i);
            }
            result.append(fileName.substring(lastEnd));
            newName = result.toString();
        }
        if (newName.equals(fileName)) {
            return Optional.empty();
        } else {
            return Optional.of(new YADEFileNameInfo(delegator, newName));
        }
    }

    /** Replaces all $1, $2, ... with the actual group values from the Matcher<br/>
     * Java 8 solution because of execution on the Agent ... */
    private static String replaceGroupReferences(String replacement, Matcher matcher) {
        Pattern groupPattern = Pattern.compile("\\$(\\d+)");
        Matcher groupMatcher = groupPattern.matcher(replacement);

        // Java 9+ solution
        // return groupMatcher.replaceAll(match -> {
        // int groupIndex = Integer.parseInt(match.group(1)); // $1 -> 1
        // return matcher.groupCount() >= groupIndex ? matcher.group(groupIndex) : "";
        // });

        StringBuilder result = new StringBuilder();
        while (groupMatcher.find()) {
            int groupIndex = Integer.parseInt(groupMatcher.group(1)); // $1 -> 1
            String replacementValue = matcher.groupCount() >= groupIndex ? matcher.group(groupIndex) : "";
            groupMatcher.appendReplacement(result, replacementValue != null ? Matcher.quoteReplacement(replacementValue) : "");
        }
        groupMatcher.appendTail(result);
        return result.toString();
    }

    private static String replaceVariables(String replacement, String fileName) {
        while (replacement.contains(VAR_DATE_PREFIX)) {
            int start = replacement.indexOf(VAR_DATE_PREFIX) + 6;
            int end = replacement.indexOf("]", start);
            String dateFormat = replacement.substring(start, end);
            replacement = replacement.replace(VAR_DATE_PREFIX + dateFormat + "]", new SimpleDateFormat(dateFormat).format(new Date()));
        }
        while (replacement.contains(VAR_FILENAME_PREFIX)) {
            int start = replacement.indexOf(VAR_FILENAME_PREFIX) + 10;
            int end = replacement.indexOf("]", start);
            String option = replacement.substring(start, end);
            String modifiedFileName = fileName;
            if ("lowercase".equals(option)) {
                modifiedFileName = modifiedFileName.toLowerCase();
            } else if ("uppercase".equals(option)) {
                modifiedFileName = modifiedFileName.toUpperCase();
            }
            replacement = replacement.replace(VAR_FILENAME_PREFIX + option + "]", modifiedFileName);
        }
        return replacement;
    }

    public static void main(String[] args) {
        try {
            AYADEProviderDelegator delegator = new YADESourceProviderDelegator(new LocalProvider(new SOSSlf4jLogger(), new LocalProviderArguments()),
                    new YADESourceArguments());

            YADEProviderFile file = new YADEProviderFile(delegator, "/tmp/1abc12def123.TXT", 0, 0, null, false);
            /** 1) Change File Name */
            // YADE1 functionality
            String regex = "(1)abc(12)def(.*)";
            String replacement = "A;BB;CCC";

            regex = ".*";
            replacement = "[date:yyyy-MM-dd-HH-mm-ss];BB;1.txt";
            replacement = "[filename:uppercase]";

            // YADE JS7 New: dynamic replacement of all place holders ($1, $2, ...)
            // regex = "(\\.[a-zA-Z0-9]+)$";
            // replacement = "X$1";

            /** 2) Change File Path based on file name */
            regex = "(^.*$)";
            replacement = "/sub/$1";
            // replacement = "../$1";

            Optional<YADEFileNameInfo> result = getReplacementResultIfDifferent(delegator, file.getName(), regex, replacement);
            System.out.println("[RESULT]" + (result.isPresent() ? SOSString.toString(result.get()) : "false"));

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find()) {
                System.out.println("[RESULT][replaceAll]" + file.getName().replaceAll(regex, replacement));
            }

            System.out.println(Path.of("/sub").isAbsolute());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
