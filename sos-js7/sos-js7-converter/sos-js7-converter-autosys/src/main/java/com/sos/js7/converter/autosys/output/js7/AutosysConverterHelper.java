package com.sos.js7.converter.autosys.output.js7;

import java.nio.file.Path;
import java.text.Collator;
import java.util.Locale;

import com.sos.commons.util.SOSString;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;

public class AutosysConverterHelper {

    public static final String DIAGRAM_FOLDER_NAME = "diagram";

    private static Collator COLLATOR = Collator.getInstance(Locale.ENGLISH);

    public static Path getMainOutputPath(Path outputDirectory, ACommonJob j, boolean withFolderName) {
        return getMainOutputPath(outputDirectory, j, withFolderName, null);
    }

    public static Path getMainOutputPath(Path outputDirectory, ACommonJob j, boolean withFolderName, String prefix) {
        if (j == null) {
            return outputDirectory;
        }
        Path p = outputDirectory.resolve(j.getJobFullPathFromJILDefinition().getParent());
        String pr = SOSString.isEmpty(prefix) ? "" : prefix;

        // j.getJobBaseName returns name after lastIndex of .
        // return withFolderName ? p.resolve(pr + j.getJobBaseName()) : p;
        return withFolderName ? p.resolve(pr + j.getName()) : p;
    }

    public static String getApplication(ACommonJob j) {
        if (j != null && j.getFolder() != null && !SOSString.isEmpty(j.getFolder().getApplication().getValue())) {
            return j.getFolder().getApplication().getValue();
        }
        return "";
    }

    public static String getGroup(ACommonJob j) {
        if (j != null && j.getFolder() != null && !SOSString.isEmpty(j.getFolder().getGroup().getValue())) {
            return j.getFolder().getGroup().getValue();
        }
        return "";
    }
}
