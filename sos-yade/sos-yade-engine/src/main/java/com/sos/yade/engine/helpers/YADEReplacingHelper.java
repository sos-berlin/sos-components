package com.sos.yade.engine.helpers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sos.yade.engine.delegators.YADEProviderFile;

public class YADEReplacingHelper {

    private static final String VAR_DATE_PREFIX = "[date:";
    private static final String VAR_FILENAME_PREFIX = "[filename:";

    public static Optional<String> getNewFileNameIfDifferent(String fileName, String regex, String replacement) {
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
                result.append(groupReplacement);
                lastEnd = matcher.end(i);
            }
            result.append(fileName.substring(lastEnd));
            newName = result.toString();
        }
        if (newName.equals(fileName)) {
            return Optional.empty();
        } else {
            return Optional.of(newName);
        }
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
            YADEProviderFile file = new YADEProviderFile("/tmp/1abc12def123.TXT", 0, 0, false);
            String regex = "(1)abc(12)def(.*)";
            String replacement = "A;BB;CCC";

            // regex = ".*";
            // replacement = "[date:yyyy-MM-dd-HH-mm-ss];BB;1.txt";
            // replacement = "[filename:uppercase]";

            Optional<String> newName = getNewFileNameIfDifferent(file.getName(), regex, replacement);
            System.out.println("RESULT:" + newName.isPresent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
