package com.sos.jitl.jobs.checkhistory.classes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckHistoryHelper {


    public static String getParameter(String p) {
        p = p.trim();
        String s = "";
        Pattern pattern = Pattern.compile("^.*\\(([^\\)]*)\\)$", Pattern.DOTALL + Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(p);
        if (matcher.find()) {
            s = matcher.group(1).trim();
        }
        return s;
    }

    public static String getQueryName(String p) {
        p = p.trim();
        String s = p;
        Pattern pattern = Pattern.compile("^([^\\(]*)\\(.*\\)$", Pattern.DOTALL + Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(p);
        if (matcher.find()) {
            s = matcher.group(1).trim();
        }
        return s.trim();
    }

}