package com.sos.js7.converter.autosys.common.v12.job;

import java.nio.file.Path;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.annotation.ArgumentSetter;

public class JobFTP extends ACommonMachineJob {

    // ftp_verify_cert: 1
    // ftp_keystore
    // ftp_truststore

    private static final String ATTR_TRANSFER_TYPE = "ftp_transfer_type";
    private static final String ATTR_USE_SSL = "ftp_use_ssl"; // or ftp_secure?
    private static final String ATTR_SSL_MODE = "ftp_ssl_mode";
    private static final String ATTR_USER_TYPE = "ftp_user_type";

    private static final String ATTR_TRANSFER_DIRECTION = "ftp_transfer_direction";
    private static final String ATTR_SERVER_NAME = "ftp_server_name";
    private static final String ATTR_SERVER_PORT = "ftp_server_port";

    private static final String ATTR_SERVER_USER = "ftp_user";

    private static final String ATTR_REMOTE_NAME = "ftp_remote_name";

    private static final String ATTR_LOCAL_NAME = "ftp_local_name";
    private static final String ATTR_LOCAL_USER = "ftp_local_user";

    // B
    private SOSArgument<String> ftpTransferType = new SOSArgument<>(ATTR_TRANSFER_TYPE, false, "B");
    // 0 , 1=FTPS
    private SOSArgument<String> ftpUseSsl = new SOSArgument<>(ATTR_USE_SSL, false);
    // explicit | implicit
    private SOSArgument<String> ftpSslMode = new SOSArgument<>(ATTR_SSL_MODE, false);
    // Simple, Windows
    private SOSArgument<String> ftpUserType = new SOSArgument<>(ATTR_USER_TYPE, false);

    // UPLOAD/DOWNLOAD
    private SOSArgument<String> ftpTransferDirection = new SOSArgument<>(ATTR_TRANSFER_DIRECTION, false);
    // server name
    private SOSArgument<String> ftpServerName = new SOSArgument<>(ATTR_SERVER_NAME, false);
    // server port
    private SOSArgument<String> ftpServerPort = new SOSArgument<>(ATTR_SERVER_PORT, false);
    // ftp server user
    private SOSArgument<String> ftpServerUser = new SOSArgument<>(ATTR_SERVER_USER, false);

    // /proj/app/output/reports
    private SOSArgument<String> ftpRemoteName = new SOSArgument<>(ATTR_REMOTE_NAME, false);
    // G:\\ANALYTICS\\Reports\\*
    private SOSArgument<String> ftpLocalName = new SOSArgument<>(ATTR_LOCAL_NAME, false);
    // user@server
    private SOSArgument<String> ftpLocalUser = new SOSArgument<>(ATTR_LOCAL_USER, false);

    // ---------------------------------------------------------------------------------------------------------------------
    public JobFTP(Path source, boolean reference) {
        super(source, ConverterJobType.FTP, reference);
    }

    public JobFTP(Path source, ConverterJobType type, boolean reference) {
        super(source, type, reference);
    }

    public SOSArgument<String> getFtpTransferType() {
        return ftpTransferType;
    }

    @ArgumentSetter(name = ATTR_TRANSFER_TYPE)
    public void setFtpTransferType(String val) {
        ftpTransferType.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getFtpUseSsl() {
        return ftpUseSsl;
    }

    @ArgumentSetter(name = ATTR_USE_SSL)
    public void setFtpUseSsl(String val) {
        ftpUseSsl.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getFtpSslMode() {
        return ftpSslMode;
    }

    @ArgumentSetter(name = ATTR_SSL_MODE)
    public void setFtpSslMode(String val) {
        ftpSslMode.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getFtpUserType() {
        return ftpUserType;
    }

    @ArgumentSetter(name = ATTR_USER_TYPE)
    public void setFtpUserType(String val) {
        ftpUserType.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getFtpTransferDirection() {
        return ftpTransferDirection;
    }

    @ArgumentSetter(name = ATTR_TRANSFER_DIRECTION)
    public void setFtpTransferDirection(String val) {
        ftpTransferDirection.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getFtpServerName() {
        return ftpServerName;
    }

    @ArgumentSetter(name = ATTR_SERVER_NAME)
    public void setFtpServerName(String val) {
        ftpServerName.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getFtpServerPort() {
        return ftpServerPort;
    }

    @ArgumentSetter(name = ATTR_SERVER_PORT)
    public void setFtpServerPort(String val) {
        ftpServerPort.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getFtpServerUser() {
        return ftpServerUser;
    }

    @ArgumentSetter(name = ATTR_SERVER_USER)
    public void setFtpServerUser(String val) {
        ftpServerUser.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getFtpRemoteName() {
        return ftpRemoteName;
    }

    @ArgumentSetter(name = ATTR_REMOTE_NAME)
    public void setFtpRemoteName(String val) {
        ftpRemoteName.setValue(JS7ConverterHelper.replaceDoubleSlashBackSlashes(JS7ConverterHelper.stringValue(val)));
    }

    public SOSArgument<String> getFtpLocalName() {
        return ftpLocalName;
    }

    @ArgumentSetter(name = ATTR_LOCAL_NAME)
    public void setFtpLocalName(String val) {
        ftpLocalName.setValue(JS7ConverterHelper.replaceDoubleSlashBackSlashes(JS7ConverterHelper.stringValue(val)));
    }

    public SOSArgument<String> getFtpLocalUser() {
        return ftpLocalUser;
    }

    @ArgumentSetter(name = ATTR_LOCAL_USER)
    public void setFtpLocalUser(String val) {
        ftpLocalUser.setValue(JS7ConverterHelper.stringValue(val));
    }

}
