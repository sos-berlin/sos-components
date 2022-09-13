package com.sos.js7.converter.js1.common.jobstreams;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import com.sos.commons.util.SOSPath;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.js1.common.EConfigFileExtensions;
import com.sos.js7.converter.js1.common.json.jobstreams.JobStream;
import com.sos.js7.converter.js1.common.json.jobstreams.JobStreamJob;

public class JobStreamsHelper {

    private final Path file;
    private final List<JobStream> jobStreams;

    private JobStreamsHelper(Path file, List<JobStream> jobStreams) {
        this.file = file;
        this.jobStreams = jobStreams;
    }

    public static JobStreamsHelper convert(Path file) throws Exception {
        return new JobStreamsHelper(file, JS7ConverterHelper.JSON_OM.readerForListOf(JobStream.class).readValue(SOSPath.readFile(file,
                StandardCharsets.UTF_8)));
    }

    public Path getFile() {
        return file;
    }

    public List<JobStream> getJobStreams() {
        return jobStreams;
    }

    public static Path getJobPath(Path live, JobStreamJob job) {
        String pathRel = job.getJob();
        if (pathRel.startsWith("/") || pathRel.startsWith("\\")) {
            pathRel = pathRel.substring(1);
        }
        return live.resolve(pathRel + EConfigFileExtensions.JOB.extension()).normalize();
    }
}
