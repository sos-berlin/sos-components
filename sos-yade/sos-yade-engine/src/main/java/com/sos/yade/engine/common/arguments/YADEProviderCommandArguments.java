package com.sos.yade.engine.common.arguments;

import java.util.List;

import com.sos.commons.util.common.ASOSArguments;
import com.sos.commons.util.common.SOSArgument;

public class YADEProviderCommandArguments extends ASOSArguments {

    // -- Operation related ------------------------------
    /** CommandBeforeOperation */
    private SOSArgument<List<String>> preTransferCommands = new SOSArgument<>("pre_transfer_commands", false);
    /** CommandAfterOperationOnSuccess */
    private SOSArgument<List<String>> postTransferCommands = new SOSArgument<>("post_transfer_commands", false);
    /** CommandAfterOperationOnError YADE-406 */
    private SOSArgument<List<String>> postTransferCommandsOnError = new SOSArgument<>("post_transfer_commands_on_error", false);
    /** CommandAfterOperationFinal YADE-406 */
    private SOSArgument<List<String>> postTransferCommandsFinal = new SOSArgument<>("post_transfer_commands_final", false);

    // -- File related ------------------------------
    /** CommandBeforeFile include="PostPreProcessing-Variables" */
    private SOSArgument<String> preCommand = new SOSArgument<>("pre_command", false);
    /** CommandBeforeFile YADE-471 */
    private SOSArgument<String> preCommandEnableForSkippedTransfer = new SOSArgument<>("pre_command_enable_for_skipped_transfer", false);

    /** CommandAfterFile include="PostPreProcessing-Variables" */
    private SOSArgument<String> postCommand = new SOSArgument<>("post_command", false);
    /** CommandAfterFile YADE-471 */
    private SOSArgument<String> postCommandDisableForSkippedTransfer = new SOSArgument<>("post_command_disable_for_skipped_transfer", false);

}
