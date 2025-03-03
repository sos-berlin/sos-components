package com.sos.yade.engine.common.arguments;

import java.util.List;

import com.sos.commons.util.common.ASOSArguments;
import com.sos.commons.util.common.SOSArgument;
import com.sos.yade.engine.common.helpers.YADEArgumentsHelper;

public class YADEProviderCommandArguments extends ASOSArguments {

    // -- Command delimiter for all command types ------------------------------
    private SOSArgument<String> commandDelimiter = new SOSArgument<>("command_delimiter", false, ";");

    // -- Operation related ------------------------------
    /** CommandBeforeOperation */
    // TODO new name: commands_before_operation
    private SOSArgument<List<String>> commandsBeforeOperation = new SOSArgument<>("pre_transfer_commands", false);
    /** CommandAfterOperationOnSuccess */
    // TODO new name: commands_after_operation
    private SOSArgument<List<String>> commandsAfterOperationOnSuccess = new SOSArgument<>("post_transfer_commands", false);
    /** CommandAfterOperationOnError YADE-406 */
    // TODO new name: commands_after_operation_on_error
    private SOSArgument<List<String>> commandsAfterOperationOnError = new SOSArgument<>("post_transfer_commands_on_error", false);
    /** CommandAfterOperationFinal YADE-406 */
    // TODO new name: commands_after_operation_final
    private SOSArgument<List<String>> commandsAfterOperationFinal = new SOSArgument<>("post_transfer_commands_final", false);

    // -- File related ------------------------------
    /** CommandBeforeFile include="PostPreProcessing-Variables" */
    // TODO new name: commands_before_file - YADE 1 support multiple commands but the name is pre_command instead of pre_commands
    private SOSArgument<List<String>> commandsBeforeFile = new SOSArgument<>("pre_command", false);
    /** CommandBeforeFile YADE-471 */
    // TODO new name: ??? command_before_file_enable_for_skipped or
    // - "command_before_file_for_skipped"
    private SOSArgument<Boolean> commandsBeforeFileEnableForSkipped = new SOSArgument<>("pre_command_enable_for_skipped_transfer", false, Boolean
            .valueOf(false));

    /** CommandAfterFile include="PostPreProcessing-Variables" */
    // TODO new name: command_after_file ??? support multiple commands
    private SOSArgument<List<String>> commandsAfterFile = new SOSArgument<>("post_command", false);
    /** CommandAfterFile YADE-471 */
    // TODO new name: command_after_file_disable_for_skipped (like YADE1) or
    // - "command_after_file_for_skipped" - !!! opposite meaning than the current implementation...
    private SOSArgument<Boolean> commandsAfterFileDisableForSkipped = new SOSArgument<>("post_command_disable_for_skipped_transfer", false, Boolean
            .valueOf(false));

    public SOSArgument<String> getCommandDelimiter() {
        return commandDelimiter;
    }

    public SOSArgument<List<String>> getCommandsBeforeOperation() {
        return commandsBeforeOperation;
    }

    public void setCommandsBeforeOperation(String val) {
        commandsBeforeOperation.setValue(YADEArgumentsHelper.stringListValue(val, commandDelimiter.getValue()));
    }

    public SOSArgument<List<String>> getCommandsAfterOperationOnSuccess() {
        return commandsAfterOperationOnSuccess;
    }

    public void setCommandsAfterOperationOnSuccess(String val) {
        commandsAfterOperationOnSuccess.setValue(YADEArgumentsHelper.stringListValue(val, commandDelimiter.getValue()));
    }

    public SOSArgument<List<String>> getCommandsAfterOperationOnError() {
        return commandsAfterOperationOnError;
    }

    public void setCommandsAfterOperationOnError(String val) {
        commandsAfterOperationOnError.setValue(YADEArgumentsHelper.stringListValue(val, commandDelimiter.getValue()));
    }

    public SOSArgument<List<String>> getCommandsAfterOperationFinal() {
        return commandsAfterOperationFinal;
    }

    public void setCommandsAfterOperationFinal(String val) {
        commandsAfterOperationFinal.setValue(YADEArgumentsHelper.stringListValue(val, commandDelimiter.getValue()));
    }

    public SOSArgument<List<String>> getCommandsBeforeFile() {
        return commandsBeforeFile;
    }

    public void setCommandsBeforeFile(String val) {
        commandsBeforeFile.setValue(YADEArgumentsHelper.stringListValue(val, commandDelimiter.getValue()));
    }

    public SOSArgument<Boolean> getCommandsBeforeFileEnableForSkipped() {
        return commandsBeforeFileEnableForSkipped;
    }

    public SOSArgument<List<String>> getCommandsAfterFile() {
        return commandsAfterFile;
    }

    public void setCommandsAfterFile(String val) {
        commandsAfterFile.setValue(YADEArgumentsHelper.stringListValue(val, commandDelimiter.getValue()));
    }

    public SOSArgument<Boolean> getCommandsAfterFileDisableForSkipped() {
        return commandsAfterFileDisableForSkipped;
    }

}
