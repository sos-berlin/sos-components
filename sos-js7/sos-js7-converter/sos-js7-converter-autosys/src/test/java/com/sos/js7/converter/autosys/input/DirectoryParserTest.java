package com.sos.js7.converter.autosys.input;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.config.AutosysConverterConfig;
import com.sos.js7.converter.autosys.input.DirectoryParser.DirectoryParserResult;

public class DirectoryParserTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryParserTest.class);

    @Ignore
    @Test
    public void testXMLParser() {
        Path inputDir = Paths.get("src/test/resources/input/xml");
        Path reportDir = Paths.get("src/test/resources/output/report");
        boolean reference = false;

        AutosysConverterConfig config = new AutosysConverterConfig();
        Instant start = Instant.now();
        DirectoryParserResult r = DirectoryParser.parse(config.getParserConfig(), new XMLJobParser(config, reportDir, reference), inputDir,
                reportDir);
        LOGGER.info("[parse][duration]" + SOSDate.getDuration(start, Instant.now()));

        log(r);
    }

    @Ignore
    @Test
    public void testJILParser() {
        Path inputDir = Paths.get("src/test/resources/input/jil");
        Path reportDir = Paths.get("src/test/resources/output/report");
        boolean reference = false;

        AutosysConverterConfig config = new AutosysConverterConfig();
        Instant start = Instant.now();
        DirectoryParserResult r = DirectoryParser.parse(config.getParserConfig(), new JILJobParser(config, reportDir, reference), inputDir,
                reportDir);
        LOGGER.info("[parse][duration]" + SOSDate.getDuration(start, Instant.now()));

        log(r);
    }

    @Ignore
    @Test
    public void testCopyJILFiles2SplitConfigurationFolders() throws Exception {
        Path splitConfigurationDir = Paths.get("autosys.input.original\\config");

        Path inputDirWithJILFiles = Paths.get("JS7-Rollout");

        List<Path> fl = SOSPath.getFolderList(splitConfigurationDir);
        LOGGER.info("Total: " + fl.size());
        for (Path folder : fl) {
            String application = folder.getFileName().toString();
            LOGGER.info(application);
            // search for <application>.jil files
            List<Path> jilFiles = SOSPath.getFileList(inputDirWithJILFiles, application + "\\.jil", Pattern.CASE_INSENSITIVE, true);
            for (Path jilFile : jilFiles) {
                Path jilFileCopy = folder.resolve(jilFile.getParent().getFileName() + "_" + jilFile.getFileName());
                LOGGER.info("    " + jilFileCopy.getFileName());
                SOSPath.deleteIfExists(jilFileCopy);
                SOSPath.copyFile(jilFile, jilFileCopy);
            }
        }

    }

    private void log(DirectoryParserResult r) {
        LOGGER.info(String.format("[JOBS]%s", r.getJobs().size()));
        for (ACommonJob job : r.getJobs()) {
            LOGGER.info(job.getName() + "=" + SOSString.toString(job));
        }
        LOGGER.info(String.format("[JOBS]%s", r.getJobs().size()));
    }

}
