package com.sos.js7.converter.autosys.output.js7.helper.jobs.jitl.yade;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.job.Job;
import com.sos.js7.converter.autosys.common.v12.job.JobFTP;
import com.sos.js7.converter.autosys.common.v12.job.JobFTPS;
import com.sos.js7.converter.autosys.common.v12.job.JobSCP;
import com.sos.js7.converter.autosys.output.js7.helper.jobs.jitl.JITLJobConverter;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class YADEJobConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(YADEJobConverter.class);

    private static final String JOB_RESOURCE_FTP = "YADE-FileTransfer-FTP";
    private static final String JOB_RESOURCE_SFTP = "YADE-FileTransfer-SFTP";
    private static final String JOB_RESOURCE_VARIABLE = "settings";

    private static final String YADE_SETTINGS_FTP = JOB_RESOURCE_FTP + ".xml";
    private static final String YADE_SETTINGS_SFTP = JOB_RESOURCE_SFTP + ".xml";

    private static final String JITL_JOB_CLASSNAME = "com.sos.jitl.jobs.yade.YADEJob";

    private static final Map<String, FTPServer> ftpServerFragments = new TreeMap<>();
    private static final Map<String, SSHServer> sshServerFragments = new TreeMap<>();

    public static void clear() {
        ftpServerFragments.clear();
        sshServerFragments.clear();
    }

    public static void convert(Path dir) {
        convertFTP(dir);
        convertSFTP(dir);
    }

    private static void convertFTP(Path dir) {
        if (ftpServerFragments.size() == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(JS7ConverterHelper.JS7_NEW_LINE);
        sb.append("<Configurations>").append(JS7ConverterHelper.JS7_NEW_LINE);
        sb.append("<JobResource name=\"" + JOB_RESOURCE_FTP + "\" environment_variable=\"" + JOB_RESOURCE_VARIABLE.toUpperCase() + "\" variable=\""
                + JOB_RESOURCE_VARIABLE + "\" />").append(JS7ConverterHelper.JS7_NEW_LINE);
        sb.append("<Fragments>").append(JS7ConverterHelper.JS7_NEW_LINE).append(getFTPFragments()).append("</Fragments>").append(
                JS7ConverterHelper.JS7_NEW_LINE);
        sb.append("<Profiles>").append(JS7ConverterHelper.JS7_NEW_LINE).append(getFTPProfiles()).append("</Profiles>").append(
                JS7ConverterHelper.JS7_NEW_LINE);

        sb.append("</Configurations>");

        Path file = dir.resolve(YADE_SETTINGS_FTP);
        try {
            SOSPath.overwrite(file, sb.toString());
            LOGGER.info("[YADE][created]" + file);
        } catch (Exception e) {
            LOGGER.error("[YADE][" + file + "]" + e.toString(), e);
        }
    }

    private static void convertSFTP(Path dir) {
        if (sshServerFragments.size() == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(JS7ConverterHelper.JS7_NEW_LINE);
        sb.append("<Configurations>").append(JS7ConverterHelper.JS7_NEW_LINE);
        sb.append("<JobResource name=\"" + JOB_RESOURCE_SFTP + "\" environment_variable=\"" + JOB_RESOURCE_VARIABLE.toUpperCase() + "\" variable=\""
                + JOB_RESOURCE_VARIABLE + "\" />").append(JS7ConverterHelper.JS7_NEW_LINE);

        sb.append("<Fragments>").append(JS7ConverterHelper.JS7_NEW_LINE).append(getSFTPFragments()).append("</Fragments>").append(
                JS7ConverterHelper.JS7_NEW_LINE);
        sb.append("<Profiles>").append(JS7ConverterHelper.JS7_NEW_LINE).append(getSFTPProfiles()).append("</Profiles>").append(
                JS7ConverterHelper.JS7_NEW_LINE);

        sb.append("</Configurations>");

        Path file = dir.resolve(YADE_SETTINGS_SFTP);
        try {
            SOSPath.overwrite(file, sb.toString());
            LOGGER.info("[YADE][created]" + file);
        } catch (Exception e) {
            LOGGER.error("[YADE][" + file + "]" + e.toString(), e);
        }
    }

    private static StringBuilder getFTPFragments() {
        StringBuilder sb = new StringBuilder();
        sb.append("  <ProtocolFragments>").append(JS7ConverterHelper.JS7_NEW_LINE);
        for (Map.Entry<String, FTPServer> entry : ftpServerFragments.entrySet()) {
            FTPServer s = entry.getValue();
            sb.append("    <FTPFragment name=\"" + s.getId() + "\">").append(JS7ConverterHelper.JS7_NEW_LINE);
            sb.append("      <BasicConnection>").append(JS7ConverterHelper.JS7_NEW_LINE);
            sb.append("        <Hostname>").append(cdata(s.getServerName())).append("</Hostname>").append(JS7ConverterHelper.JS7_NEW_LINE);
            if (!SOSString.isEmpty(s.getServerPort())) {
                sb.append("        <Port>").append(s.getServerPort()).append("</Port>").append(JS7ConverterHelper.JS7_NEW_LINE);
            }

            String user = SOSString.isEmpty(s.getServerUser()) ? JITLJobConverter.DEFAULT_USER : s.getServerUser();

            sb.append("      </BasicConnection>").append(JS7ConverterHelper.JS7_NEW_LINE);
            sb.append("      <BasicAuthentication>").append(JS7ConverterHelper.JS7_NEW_LINE);
            sb.append("        <Account>").append(cdata(user)).append("</Account>").append(JS7ConverterHelper.JS7_NEW_LINE);
            sb.append("        <Password>").append(JITLJobConverter.DEFAULT_PASSWORD).append("</Password>").append(JS7ConverterHelper.JS7_NEW_LINE);
            sb.append("      </BasicAuthentication>").append(JS7ConverterHelper.JS7_NEW_LINE);
            if (!SOSString.isEmpty(s.getTransferType())) {
                String tm = s.getTransferType().toLowerCase().equals("b") ? "binary" : "ascii";
                sb.append("      <TransferMode>").append(cdata(tm)).append("</TransferMode>").append(JS7ConverterHelper.JS7_NEW_LINE);
            }
            sb.append("    </FTPFragment>").append(JS7ConverterHelper.JS7_NEW_LINE);

        }
        sb.append("  </ProtocolFragments>").append(JS7ConverterHelper.JS7_NEW_LINE);
        return sb;
    }

    private static StringBuilder getSFTPFragments() {
        StringBuilder sb = new StringBuilder();
        sb.append("  <ProtocolFragments>").append(JS7ConverterHelper.JS7_NEW_LINE);
        for (Map.Entry<String, SSHServer> entry : sshServerFragments.entrySet()) {
            SSHServer s = entry.getValue();
            sb.append("    <SFTPFragment name=\"" + s.getId() + "\">").append(JS7ConverterHelper.JS7_NEW_LINE);
            sb.append("      <BasicConnection>").append(JS7ConverterHelper.JS7_NEW_LINE);
            sb.append("        <Hostname>").append(cdata(s.getServerName())).append("</Hostname>").append(JS7ConverterHelper.JS7_NEW_LINE);
            if (!SOSString.isEmpty(s.getServerPort())) {
                sb.append("        <Port>").append(s.getServerPort()).append("</Port>").append(JS7ConverterHelper.JS7_NEW_LINE);
            }
            sb.append("      </BasicConnection>").append(JS7ConverterHelper.JS7_NEW_LINE);
            sb.append("      <SSHAuthentication>").append(JS7ConverterHelper.JS7_NEW_LINE);
            sb.append("        <Account>").append(cdata(JITLJobConverter.DEFAULT_USER)).append("</Account>").append(JS7ConverterHelper.JS7_NEW_LINE);
            sb.append("      </SSHAuthentication>").append(JS7ConverterHelper.JS7_NEW_LINE);
            sb.append("    </SFTPFragment>").append(JS7ConverterHelper.JS7_NEW_LINE);

        }
        sb.append("  </ProtocolFragments>").append(JS7ConverterHelper.JS7_NEW_LINE);
        return sb;
    }

    private static String cdata(String val) {
        return "<![CDATA[" + val + "]]>";
    }

    private static StringBuilder getFTPProfiles() {
        StringBuilder sb = new StringBuilder();
        Set<String> generated = new HashSet<>();
        for (Map.Entry<String, FTPServer> entry : ftpServerFragments.entrySet()) {
            FTPServer s = entry.getValue();

            s.getFragmentJobs().sort(Comparator.comparing(JobFTP::getName));

            for (JobFTP j : s.getFragmentJobs()) {
                if (generated.contains(j.getName())) {
                    continue;
                }
                generated.add(j.getName());

                boolean isUpload = j.getFtpTransferDirection().getValue().equalsIgnoreCase("upload");
                String source = j.getFtpLocalName().getValue();
                String target = j.getFtpRemoteName().getValue();
                if (!isUpload) { // download
                    source = j.getFtpRemoteName().getValue();
                    target = j.getFtpLocalName().getValue();
                }

                sb.append("  <Profile provile_id=\"" + j.getName() + "\">").append(JS7ConverterHelper.JS7_NEW_LINE);
                sb.append("  <Operation>").append(JS7ConverterHelper.JS7_NEW_LINE);
                sb.append("  <Copy>").append(JS7ConverterHelper.JS7_NEW_LINE);

                sb.append("  <CopySource>").append(JS7ConverterHelper.JS7_NEW_LINE);
                sb.append("  <CopySourceFragmentRef>").append(JS7ConverterHelper.JS7_NEW_LINE);
                if (isUpload) {
                    sb.append("    <LocalSource />").append(JS7ConverterHelper.JS7_NEW_LINE);
                } else {
                    sb.append("    <FTPFragmentRef ref=\"" + s.getId() + "\" />").append(JS7ConverterHelper.JS7_NEW_LINE);
                }
                sb.append("  </CopySourceFragmentRef>").append(JS7ConverterHelper.JS7_NEW_LINE);

                sb.append("  <SourceFileOptions>").append(JS7ConverterHelper.JS7_NEW_LINE);
                sb.append("  <Selection>").append(JS7ConverterHelper.JS7_NEW_LINE);

                boolean isFileSpec = source.endsWith("*");
                if (isFileSpec) {
                    sb.append("  <FileSpecSelection>").append(JS7ConverterHelper.JS7_NEW_LINE);
                    sb.append("  <FileSpec>").append(cdata(".*")).append("</FileSpec>").append(JS7ConverterHelper.JS7_NEW_LINE);
                    sb.append("  <Directory>").append(cdata(SOSPathUtils.getParentPath(source))).append("</Directory>").append(
                            JS7ConverterHelper.JS7_NEW_LINE);
                    sb.append("  </FileSpecSelection>").append(JS7ConverterHelper.JS7_NEW_LINE);
                } else {
                    sb.append("  <FilePathSelection>").append(JS7ConverterHelper.JS7_NEW_LINE);
                    sb.append("  <FilePath>").append(cdata(source)).append("</FilePath>").append(JS7ConverterHelper.JS7_NEW_LINE);
                    sb.append("  </FilePathSelection>").append(JS7ConverterHelper.JS7_NEW_LINE);
                }

                sb.append("  </Selection>").append(JS7ConverterHelper.JS7_NEW_LINE);
                sb.append("  </SourceFileOptions>").append(JS7ConverterHelper.JS7_NEW_LINE);

                sb.append("  </CopySource>").append(JS7ConverterHelper.JS7_NEW_LINE);

                sb.append("  <CopyTarget>").append(JS7ConverterHelper.JS7_NEW_LINE);
                sb.append("  <CopyTargetFragmentRef>").append(JS7ConverterHelper.JS7_NEW_LINE);
                if (isUpload) {
                    sb.append("    <FTPFragmentRef ref=\"" + s.getId() + "\" />").append(JS7ConverterHelper.JS7_NEW_LINE);
                } else {
                    sb.append("    <LocalTarget />").append(JS7ConverterHelper.JS7_NEW_LINE);
                }
                sb.append("  </CopyTargetFragmentRef>").append(JS7ConverterHelper.JS7_NEW_LINE);
                sb.append("  <Directory>");

                if (isFile(target)) {
                    sb.append(cdata(SOSPathUtils.getParentPath(target)));
                } else {
                    sb.append(cdata(target));
                }

                sb.append("  </Directory>").append(JS7ConverterHelper.JS7_NEW_LINE);
                sb.append("  </CopyTarget>").append(JS7ConverterHelper.JS7_NEW_LINE);

                sb.append("  </Copy>").append(JS7ConverterHelper.JS7_NEW_LINE);
                sb.append("  </Operation>").append(JS7ConverterHelper.JS7_NEW_LINE);
                sb.append("  </Profile>").append(JS7ConverterHelper.JS7_NEW_LINE);
            }
        }
        return sb;
    }

    private static StringBuilder getSFTPProfiles() {
        StringBuilder sb = new StringBuilder();
        Set<String> generated = new HashSet<>();
        for (Map.Entry<String, SSHServer> entry : sshServerFragments.entrySet()) {
            SSHServer s = entry.getValue();

            s.getFragmentJobs().sort(Comparator.comparing(JobSCP::getName));

            for (JobSCP j : s.getFragmentJobs()) {
                if (generated.contains(j.getName())) {
                    continue;
                }
                generated.add(j.getName());

                boolean isUpload = j.getScpTransferDirection().getValue().equalsIgnoreCase("upload");
                String source = j.getScpLocalName().getValue();
                String sourceDir = source;
                String target = j.getScpRemoteName().getValue();
                String targetDir = j.getScpRemoteDir().getValue();
                if (targetDir == null) {
                    targetDir = target;
                }
                if (!isUpload) { // download
                    source = j.getScpRemoteName().getValue();
                    sourceDir = j.getScpRemoteDir().getValue();
                    if (sourceDir == null) {
                        sourceDir = source;
                    }
                    target = j.getScpLocalName().getValue();
                    targetDir = target;
                }

                sb.append("  <Profile provile_id=\"" + j.getName() + "\">").append(JS7ConverterHelper.JS7_NEW_LINE);
                sb.append("  <Operation>").append(JS7ConverterHelper.JS7_NEW_LINE);
                sb.append("  <Copy>").append(JS7ConverterHelper.JS7_NEW_LINE);

                sb.append("  <CopySource>").append(JS7ConverterHelper.JS7_NEW_LINE);
                sb.append("  <CopySourceFragmentRef>").append(JS7ConverterHelper.JS7_NEW_LINE);

                boolean deleteSourceDir = j.getScpDeleteSourcedir().getValue() != null && j.getScpDeleteSourcedir().getValue().equals("1");
                if (isUpload) {
                    sb.append("    <LocalSource>").append(JS7ConverterHelper.JS7_NEW_LINE);
                    if (deleteSourceDir) {
                        sb.append("    <LocalPostProcessing>").append(JS7ConverterHelper.JS7_NEW_LINE);
                        sb.append("    <CommandAfterOperationOnSuccess>").append(cdata("REMOVE_DIRECTORY()")).append(
                                "</CommandAfterOperationOnSuccess>").append(JS7ConverterHelper.JS7_NEW_LINE);
                        sb.append("    </LocalPostProcessing>").append(JS7ConverterHelper.JS7_NEW_LINE);
                    }
                    sb.append("    </LocalSource>").append(JS7ConverterHelper.JS7_NEW_LINE);
                } else {
                    sb.append("    <SFTPFragmentRef ref=\"" + s.getId() + "\">").append(JS7ConverterHelper.JS7_NEW_LINE);
                    if (deleteSourceDir) {
                        sb.append("    <SFTPPostProcessing>").append(JS7ConverterHelper.JS7_NEW_LINE);
                        sb.append("    <CommandAfterOperationOnSuccess>").append(cdata("REMOVE_DIRECTORY()")).append(
                                "</CommandAfterOperationOnSuccess>").append(JS7ConverterHelper.JS7_NEW_LINE);
                        sb.append("    </SFTPPostProcessing>").append(JS7ConverterHelper.JS7_NEW_LINE);
                    }
                    sb.append("    </SFTPFragmentRef>").append(JS7ConverterHelper.JS7_NEW_LINE);
                }
                sb.append("  </CopySourceFragmentRef>").append(JS7ConverterHelper.JS7_NEW_LINE);

                sb.append("  <SourceFileOptions>").append(JS7ConverterHelper.JS7_NEW_LINE);
                sb.append("  <Selection>").append(JS7ConverterHelper.JS7_NEW_LINE);

                boolean isFileSpec = source.endsWith("*") || !source.equals(sourceDir);
                if (isFileSpec) {
                    sb.append("  <FileSpecSelection>").append(JS7ConverterHelper.JS7_NEW_LINE);
                    if (source.equals(sourceDir)) {
                        sourceDir = SOSPathUtils.getParentPath(source);
                    }
                    if (source.endsWith("*")) {
                        sb.append("  <FileSpec>").append(cdata(".*")).append("</FileSpec>").append(JS7ConverterHelper.JS7_NEW_LINE);
                        sb.append("  <Directory>").append(cdata(sourceDir)).append("</Directory>").append(JS7ConverterHelper.JS7_NEW_LINE);
                    } else {
                        // employee_<yyyymmddhhmiss>.csv
                        if (source.contains("<yyyymmddhhmiss>")) {
                            source = source.replace("<yyyymmddhhmiss>", "[date:yyyyMMddHHmmss]");
                        }

                        sb.append("  <FileSpec>").append(cdata(source)).append("</FileSpec>").append(JS7ConverterHelper.JS7_NEW_LINE);
                        sb.append("  <Directory>").append(cdata(sourceDir)).append("</Directory>").append(JS7ConverterHelper.JS7_NEW_LINE);
                    }
                    sb.append("  </FileSpecSelection>").append(JS7ConverterHelper.JS7_NEW_LINE);
                } else {
                    sb.append("  <FilePathSelection>").append(JS7ConverterHelper.JS7_NEW_LINE);

                    // employee_<yyyymmddhhmiss>.csv
                    if (source.contains("<yyyymmddhhmiss>")) {
                        source = source.replace("<yyyymmddhhmiss>", "[date:yyyyMMddHHmmss]");
                    }

                    sb.append("  <FilePath>").append(cdata(source)).append("</FilePath>").append(JS7ConverterHelper.JS7_NEW_LINE);
                    sb.append("  </FilePathSelection>").append(JS7ConverterHelper.JS7_NEW_LINE);
                }

                sb.append("  </Selection>").append(JS7ConverterHelper.JS7_NEW_LINE);
                sb.append("  </SourceFileOptions>").append(JS7ConverterHelper.JS7_NEW_LINE);

                sb.append("  </CopySource>").append(JS7ConverterHelper.JS7_NEW_LINE);

                sb.append("  <CopyTarget>").append(JS7ConverterHelper.JS7_NEW_LINE);
                sb.append("  <CopyTargetFragmentRef>").append(JS7ConverterHelper.JS7_NEW_LINE);
                if (isUpload) {
                    sb.append("    <SFTPFragmentRef ref=\"" + s.getId() + "\" />").append(JS7ConverterHelper.JS7_NEW_LINE);
                } else {
                    sb.append("    <LocalTarget />").append(JS7ConverterHelper.JS7_NEW_LINE);
                }
                sb.append("  </CopyTargetFragmentRef>").append(JS7ConverterHelper.JS7_NEW_LINE);
                sb.append("  <Directory>");

                if (isFile(targetDir)) {
                    sb.append(cdata(SOSPathUtils.getParentPath(targetDir)));
                } else {
                    sb.append(cdata(targetDir));
                }

                sb.append("  </Directory>").append(JS7ConverterHelper.JS7_NEW_LINE);
                sb.append("  </CopyTarget>").append(JS7ConverterHelper.JS7_NEW_LINE);

                sb.append("  </Copy>").append(JS7ConverterHelper.JS7_NEW_LINE);
                sb.append("  </Operation>").append(JS7ConverterHelper.JS7_NEW_LINE);
                sb.append("  </Profile>").append(JS7ConverterHelper.JS7_NEW_LINE);
            }
        }
        return sb;
    }

    private static boolean isFile(String val) {
        String n = SOSPathUtils.getName(val);
        return n != null && n.contains(".");
    }

    public static Job setExecutable(Job j, JobFTP jilJob, String platform) {
        j = JITLJobConverter.createExecutable(j, JITL_JOB_CLASSNAME);

        JITLJobConverter.addArgument(j, "profile", jilJob.getName());
        j.setJobResourceNames(Collections.singletonList(JOB_RESOURCE_FTP));

        String id = FTPServer.getId(jilJob);
        FTPServer s = ftpServerFragments.get(id);
        if (s == null) {
            s = new FTPServer(id, jilJob);
            ftpServerFragments.put(id, s);
        }
        s.addFragmentJob(jilJob);

        return j;
    }

    public static Job setExecutable(Job j, JobFTPS jilJob, String platform) {
        j = JITLJobConverter.createExecutable(j, JITL_JOB_CLASSNAME);

        JITLJobConverter.addArgument(j, "profile", jilJob.getName());
        j.setJobResourceNames(Collections.singletonList(JOB_RESOURCE_FTP));

        return j;
    }

    public static Job setExecutable(Job j, JobSCP jilJob, String platform) {
        j = JITLJobConverter.createExecutable(j, JITL_JOB_CLASSNAME);

        JITLJobConverter.addArgument(j, "profile", jilJob.getName());
        j.setJobResourceNames(Collections.singletonList(JOB_RESOURCE_SFTP));

        String id = SSHServer.getId(jilJob);
        SSHServer s = sshServerFragments.get(id);
        if (s == null) {
            s = new SSHServer(id, jilJob);
            sshServerFragments.put(id, s);
        }
        s.addFragmentJob(jilJob);

        return j;
    }

}
