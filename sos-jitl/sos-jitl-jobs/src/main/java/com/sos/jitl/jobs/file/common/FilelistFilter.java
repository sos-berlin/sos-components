package com.sos.jitl.jobs.file.common;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

public class FilelistFilter implements FilenameFilter {

    Pattern pattern;

    public FilelistFilter(final String regexp, final int flag) throws Exception {
        pattern = Pattern.compile(regexp, flag);
    }

    @Override
    public boolean accept(final File dir, final String filename) {
        return find(filename);
    }

    public boolean accept(final String filename) {
        return find(filename);
    }

    private boolean find(final String val) {
        return pattern.matcher(val).find();
    }
}
