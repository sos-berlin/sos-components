package com.sos.js7.converter.autosys.common.v12.job;

import java.nio.file.Path;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.annotation.ArgumentSetter;

public class JobSCP extends ACommonMachineJob {

    private static final String ATTR_TARGET_OS = "scp_target_os";
    private static final String ATTR_PROTOCOL = "scp_protocol";

    private static final String ATTR_TRANSFER_DIRECTION = "scp_transfer_direction";
    private static final String ATTR_SERVER_NAME = "scp_server_name";
    private static final String ATTR_SERVER_PORT = "scp_server_port";

    private static final String ATTR_REMOTE_DIR = "scp_remote_dir";
    private static final String ATTR_REMOTE_NAME = "scp_remote_name";

    private static final String ATTR_LOCAL_NAME = "scp_local_name";
    private static final String ATTR_LOCAL_USER = "scp_local_user";

    private static final String ATTR_DELETE_SOURCEDIR = "scp_delete_sourcedir";

    // UNIX
    private SOSArgument<String> scpTargetOs = new SOSArgument<>(ATTR_TARGET_OS, false);
    // SFTP
    private SOSArgument<String> scpProtocol = new SOSArgument<>(ATTR_PROTOCOL, false);
    // UPLOAD/DOWNLOAD
    private SOSArgument<String> scpTransferDirection = new SOSArgument<>(ATTR_TRANSFER_DIRECTION, false);
    // server name
    private SOSArgument<String> scpServerName = new SOSArgument<>(ATTR_SERVER_NAME, false);
    // 22
    private SOSArgument<String> scpServerPort = new SOSArgument<>(ATTR_SERVER_PORT, false);

    // /ftproot/home/workday_ftp
    private SOSArgument<String> scpRemoteDir = new SOSArgument<>(ATTR_REMOTE_DIR, false);
    // employee_<yyyymmddhhmiss>.csv , * , ...
    private SOSArgument<String> scpRemoteName = new SOSArgument<>(ATTR_REMOTE_NAME, false);
    // \\proj\\app\\ebs\\WFS\\
    private SOSArgument<String> scpLocalName = new SOSArgument<>(ATTR_LOCAL_NAME, false);
    // user@server
    private SOSArgument<String> scpLocalUser = new SOSArgument<>(ATTR_LOCAL_USER, false);
    // 0
    private SOSArgument<String> scpDeleteSourcedir = new SOSArgument<>(ATTR_DELETE_SOURCEDIR, false);

    // ---------------------------------------------------------------------------------------------------------------------
    public JobSCP(Path source, boolean reference) {
        super(source, ConverterJobType.SCP, reference);
    }

    public SOSArgument<String> getScpTargetOs() {
        return scpTargetOs;
    }

    @ArgumentSetter(name = ATTR_TARGET_OS)
    public void setScpTargetOs(String val) {
        scpTargetOs.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getScpProtocol() {
        return scpProtocol;
    }

    @ArgumentSetter(name = ATTR_PROTOCOL)
    public void setScpProtocol(String val) {
        scpProtocol.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getScpTransferDirection() {
        return scpTransferDirection;
    }

    @ArgumentSetter(name = ATTR_TRANSFER_DIRECTION)
    public void setScpTransferDirection(String val) {
        scpTransferDirection.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getScpServerName() {
        return scpServerName;
    }

    @ArgumentSetter(name = ATTR_SERVER_NAME)
    public void setScpServerName(String val) {
        scpServerName.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getScpServerPort() {
        return scpServerPort;
    }

    @ArgumentSetter(name = ATTR_SERVER_PORT)
    public void setScpServerPort(String val) {
        scpServerPort.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getScpRemoteDir() {
        return scpRemoteDir;
    }

    @ArgumentSetter(name = ATTR_REMOTE_DIR)
    public void setScpRemoteDir(String val) {
        scpRemoteDir.setValue(JS7ConverterHelper.replaceDoubleSlashBackSlashes(JS7ConverterHelper.stringValue(val)));
    }

    public SOSArgument<String> getScpRemoteName() {
        return scpRemoteName;
    }

    @ArgumentSetter(name = ATTR_REMOTE_NAME)
    public void setScpRemoteName(String val) {
        scpRemoteName.setValue(JS7ConverterHelper.replaceDoubleSlashBackSlashes(JS7ConverterHelper.stringValue(val)));
    }

    public SOSArgument<String> getScpLocalName() {
        return scpLocalName;
    }

    @ArgumentSetter(name = ATTR_LOCAL_NAME)
    public void setScpLocalName(String val) {
        scpLocalName.setValue(JS7ConverterHelper.replaceDoubleSlashBackSlashes(JS7ConverterHelper.stringValue(val)));
    }

    public SOSArgument<String> getScpLocalUser() {
        return scpLocalUser;
    }

    @ArgumentSetter(name = ATTR_LOCAL_USER)
    public void setScpLocalUser(String val) {
        scpLocalUser.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getScpDeleteSourcedir() {
        return scpDeleteSourcedir;
    }

    @ArgumentSetter(name = ATTR_DELETE_SOURCEDIR)
    public void setScpDeleteSourcedir(String val) {
        scpDeleteSourcedir.setValue(JS7ConverterHelper.stringValue(val));
    }

}
