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
        Path p = outputDirectory;
        String app = getApplication(j);
        if (!SOSString.isEmpty(app)) {
            p = p.resolve(app);
        }
        String pr = SOSString.isEmpty(prefix) ? "" : prefix;
        return withFolderName ? p.resolve(pr + j.getInsertJob().getValue()) : p;
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
