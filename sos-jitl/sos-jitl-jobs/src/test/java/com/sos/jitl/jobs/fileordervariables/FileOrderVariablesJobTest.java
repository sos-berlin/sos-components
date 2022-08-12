package com.sos.jitl.jobs.fileordervariables;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.jitl.jobs.common.UnitTestJobHelper;
import com.sos.jitl.jobs.fileordervariablesjob.FileOrderVariablesJob;
import com.sos.jitl.jobs.fileordervariablesjob.FileOrderVariablesJobArguments;

public class FileOrderVariablesJobTest {

    @Ignore
    @Test
    public void testFileOrderVariablesJob() throws Exception {

        FileOrderVariablesJobArguments args = new FileOrderVariablesJobArguments();
        args.setJs7SourceFile("c:/temp/1.txt");
        FileOrderVariablesJob job = new FileOrderVariablesJob(null);

        UnitTestJobHelper<FileOrderVariablesJobArguments> h = new UnitTestJobHelper<>(job);

        job.process(h.newJobStep(args), args);
    }

}
