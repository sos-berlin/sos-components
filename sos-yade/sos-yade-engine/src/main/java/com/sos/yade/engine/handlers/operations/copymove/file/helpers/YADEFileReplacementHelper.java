package com.sos.yade.engine.handlers.operations.copymove.file.helpers;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sos.yade.engine.commons.delegators.AYADEProviderDelegator;
import com.sos.yade.engine.commons.helpers.YADEExpressionResolver;
import com.sos.yade.engine.exceptions.YADEEngineInvalidExpressionException;
import com.sos.yade.engine.handlers.operations.copymove.file.commons.YADEFileNameInfo;

/** @see YADEFileNameInfo
 * @apiNote COPY/MOVE operations.<br/>
 *          - A masked replacement supports masks for substitution in the filename with format strings that are enclosed with [ and ].<br/>
 *          -- The following format strings are supported:<br/>
 *          --- [date:&lt;date format&gt; [timezone:&lt;zone&gt;]] - see {@link YADEExpressionResolver}.<br/>
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

    private static final String VAR_FILENAME_PREFIX = "[filename:";
    private static final int VAR_FILENAME_PREFIX_LENGTH = VAR_FILENAME_PREFIX.length();

    public static Optional<YADEFileNameInfo> getReplacementResultIfDifferent(AYADEProviderDelegator delegator, String fileName, String regex,
            String replacement) throws YADEEngineInvalidExpressionException {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileName);
        if (!matcher.find()) {
            return Optional.empty();
        }

        String newName;
        if (matcher.groupCount() == 0) {
            String newReplacement = replaceVariables(replacement, fileName);
            if (matcher.matches()) {
                newName = newReplacement;
            } else {
                newName = matcher.replaceAll(newReplacement);
            }
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
            return Optional.of(new YADEFileNameInfo(delegator, newName, true));
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

    private static String replaceVariables(String replacement, String fileName) throws YADEEngineInvalidExpressionException {
        // [date: ...
        replacement = YADEExpressionResolver.replaceDateExpressions(replacement);

        // [filename: ...
        while (replacement.contains(VAR_FILENAME_PREFIX)) {
            int start = replacement.indexOf(VAR_FILENAME_PREFIX) + VAR_FILENAME_PREFIX_LENGTH;
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

}
