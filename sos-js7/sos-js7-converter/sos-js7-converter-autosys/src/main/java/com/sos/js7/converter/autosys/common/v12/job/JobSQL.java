package com.sos.js7.converter.autosys.common.v12.job;

import java.nio.file.Path;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.annotation.ArgumentSetter;

/** The JIL job definition does not contain a database user or password. */
public class JobSQL extends ACommonMachineJob {

    private static final String ATTR_CONNECT_STRING = "connect_string";
    private static final String ATTR_SQL_COMMAND = "sql_command";
    private static final String ATTR_DESTINATION_FILE = "destination_file";

    // "jdbc:oracle:thin:@//server:1521/xyz"
    private SOSArgument<String> connectString = new SOSArgument<>(ATTR_CONNECT_STRING, false);
    // SELECT/DELETE etc
    private SOSArgument<String> sqlCommand = new SOSArgument<>(ATTR_SQL_COMMAND, false);
    // /proj/app/file.txt, /proj/app/delete/logs/LOGGING.err
    private SOSArgument<String> destinationFile = new SOSArgument<>(ATTR_DESTINATION_FILE, false);

    public JobSQL(Path source, boolean reference) {
        super(source, ConverterJobType.SQL, reference);
    }

    public SOSArgument<String> getConnectString() {
        return connectString;
    }

    @ArgumentSetter(name = ATTR_CONNECT_STRING)
    public void setConnectString(String val) {
        connectString.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getSqlCommand() {
        return sqlCommand;
    }

    @ArgumentSetter(name = ATTR_SQL_COMMAND)
    public void setSqlCommand(String val) {
        sqlCommand.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getDestinationFile() {
        return destinationFile;
    }

    @ArgumentSetter(name = ATTR_DESTINATION_FILE)
    public void setDestinationFile(String val) {
        destinationFile.setValue(JS7ConverterHelper.stringValue(val));
    }

}
