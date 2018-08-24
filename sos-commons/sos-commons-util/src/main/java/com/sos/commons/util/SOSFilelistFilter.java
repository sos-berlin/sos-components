package com.sos.commons.util;

import java.io.File;
import java.io.FilenameFilter;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SOSFilelistFilter implements FilenameFilter {

    Pattern pattern;

    public SOSFilelistFilter(String regexp, int flag)   {
        pattern = Pattern.compile(regexp, flag);
    }

    public boolean accept(File dir, String filename) {
        Matcher matcher = pattern.matcher(filename);
        return matcher.find();
    }

}
