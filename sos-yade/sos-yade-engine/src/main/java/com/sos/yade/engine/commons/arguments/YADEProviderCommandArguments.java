package com.sos.yade.engine.commons.arguments;

import java.util.ArrayList;
import java.util.List;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.yade.engine.commons.helpers.YADEArgumentsHelper;

public class YADEProviderCommandArguments extends ASOSArguments {

    // -- Command delimiter for all command types ------------------------------
    private SOSArgument<String> commandDelimiter = new SOSArgument<>("command_delimiter", false, ";");

    // -- Operation related ------------------------------
    /** CommandBeforeOperation */
    // YADE1 name: pre_transfer_commands
    private SOSArgument<List<String>> commandsBeforeOperation = new SOSArgument<>("CommandBeforeOperation", false);
    /** CommandAfterOperationOnSuccess */
    // YADE1 name: post_transfer_commands
    private SOSArgument<List<String>> commandsAfterOperationOnSuccess = new SOSArgument<>("CommandAfterOperationOnSuccess", false);
    /** CommandAfterOperationOnError YADE-406 */
    // YADE1 name: post_transfer_commands_on_error
    private SOSArgument<List<String>> commandsAfterOperationOnError = new SOSArgument<>("CommandAfterOperationOnError", false);
    /** CommandAfterOperationFinal YADE-406 */
    // YADE1 name: post_transfer_commands_final
    private SOSArgument<List<String>> commandsAfterOperationFinal = new SOSArgument<>("CommandAfterOperationFinal", false);

    // -- File related ------------------------------
    /** CommandBeforeFile include="PostPreProcessing-Variables" */
    // YADE1 name: pre_command - YADE 1 support multiple commands but the name is pre_command instead of pre_commands
    private SOSArgument<List<String>> commandsBeforeFile = new SOSArgument<>("CommandBeforeFile", false);

    /** CommandBeforeFile YADE-471 */
    // YADE1 name: pre_command_enable_for_skipped_transfer
    private SOSArgument<Boolean> commandsBeforeFileEnableForSkipped = new SOSArgument<>("CommandBeforeFile@enable_for_skipped_transfer", false,
            Boolean.valueOf(false));

    /** CommandAfterFile include="PostPreProcessing-Variables" */
    // YADE1 name: post_command
    private SOSArgument<List<String>> commandsAfterFile = new SOSArgument<>("CommandAfterFile", false);
    /** CommandAfterFile YADE-471 */
    // YADE1 name: post_command_disable_for_skipped_transfer
    private SOSArgument<Boolean> commandsAfterFileDisableForSkipped = new SOSArgument<>("CommandAfterFile@disable_for_skipped_transfer", false,
            Boolean.valueOf(false));
    // YADE1 name: tfn_post_command
    // Specifies the commands to be executed for each file on the server after the transfer but before a Rename.
    private SOSArgument<List<String>> commandsBeforeRename = new SOSArgument<>("CommandsBeforeRename", false);

    public SOSArgument<String> getCommandDelimiter() {
        return commandDelimiter;
    }

    public SOSArgument<List<String>> getCommandsBeforeOperation() {
        return commandsBeforeOperation;
    }

    public String getCommandsBeforeOperationAsString() {
        return getCommandsAsString(commandsBeforeOperation);
    }

    public void setCommandsBeforeOperation(String commands) {
        commandsBeforeOperation.setValue(YADEArgumentsHelper.stringListValue(commands, commandDelimiter.getValue()));
    }

    public void addCommandBeforeOperation(String command) {
        if (commandsBeforeOperation.getValue() == null) {
            commandsBeforeOperation.setValue(new ArrayList<>());
        }
        commandsBeforeOperation.getValue().add(command);
    }

    public SOSArgument<List<String>> getCommandsAfterOperationOnSuccess() {
        return commandsAfterOperationOnSuccess;
    }

    public String getCommandsAfterOperationOnSuccessAsString() {
        return getCommandsAsString(commandsAfterOperationOnSuccess);
    }

    public void setCommandsAfterOperationOnSuccess(String commands) {
        commandsAfterOperationOnSuccess.setValue(YADEArgumentsHelper.stringListValue(commands, commandDelimiter.getValue()));
    }

    public void addCommandAfterOperationOnSuccess(String command) {
        if (commandsAfterOperationOnSuccess.getValue() == null) {
            commandsAfterOperationOnSuccess.setValue(new ArrayList<>());
        }
        commandsAfterOperationOnSuccess.getValue().add(command);
    }

    public SOSArgument<List<String>> getCommandsAfterOperationOnError() {
        return commandsAfterOperationOnError;
    }

    public String getCommandsAfterOperationOnErrorAsString() {
        return getCommandsAsString(commandsAfterOperationOnError);
    }

    public void setCommandsAfterOperationOnError(String commands) {
        commandsAfterOperationOnError.setValue(YADEArgumentsHelper.stringListValue(commands, commandDelimiter.getValue()));
    }

    public SOSArgument<List<String>> getCommandsAfterOperationFinal() {
        return commandsAfterOperationFinal;
    }

    public String getCommandsAfterOperationFinalAsString() {
        return getCommandsAsString(commandsAfterOperationFinal);
    }

    public void setCommandsAfterOperationFinal(String commands) {
        commandsAfterOperationFinal.setValue(YADEArgumentsHelper.stringListValue(commands, commandDelimiter.getValue()));
    }

    public SOSArgument<List<String>> getCommandsBeforeFile() {
        return commandsBeforeFile;
    }

    public String getCommandsBeforeFileAsString() {
        return getCommandsAsString(commandsBeforeFile);
    }

    public void setCommandsBeforeFile(String commands) {
        commandsBeforeFile.setValue(YADEArgumentsHelper.stringListValue(commands, commandDelimiter.getValue()));
    }

    public SOSArgument<Boolean> getCommandsBeforeFileEnableForSkipped() {
        return commandsBeforeFileEnableForSkipped;
    }

    public SOSArgument<List<String>> getCommandsAfterFile() {
        return commandsAfterFile;
    }

    public String getCommandsAfterFileAsString() {
        return getCommandsAsString(commandsAfterFile);
    }

    public void setCommandsAfterFile(String val) {
        commandsAfterFile.setValue(YADEArgumentsHelper.stringListValue(val, commandDelimiter.getValue()));
    }

    public SOSArgument<Boolean> getCommandsAfterFileDisableForSkipped() {
        return commandsAfterFileDisableForSkipped;
    }

    public SOSArgument<List<String>> getCommandsBeforeRename() {
        return commandsBeforeRename;
    }

    public String getCommandsBeforeRenameAsString() {
        return getCommandsAsString(commandsBeforeRename);
    }

    public void setCommandsBeforeRename(String val) {
        commandsBeforeRename.setValue(YADEArgumentsHelper.stringListValue(val, commandDelimiter.getValue()));
    }

    public boolean isPreProcessingEnabled() {
        return commandsBeforeFile.isDirty() || commandsBeforeOperation.isDirty();
    }

    public boolean isPostProcessingEnabled() {
        return commandsAfterFile.isDirty() || commandsAfterOperationOnSuccess.isDirty() || commandsAfterOperationOnError.isDirty()
                || commandsAfterOperationFinal.isDirty() || commandsBeforeRename.isDirty();
    }

    private String getCommandsAsString(SOSArgument<List<String>> arg) {
        if (arg.getValue() == null) {
            return null;
        }
        return String.join(commandDelimiter.getValue(), arg.getValue());
    }

}
