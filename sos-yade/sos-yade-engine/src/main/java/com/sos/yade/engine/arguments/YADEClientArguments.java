package com.sos.yade.engine.arguments;

import java.nio.file.Path;
import java.util.List;

import com.sos.commons.util.SOSComparisonOperator;
import com.sos.commons.util.common.SOSArgument;

public class YADEClientArguments {

    /** - System Properties ------- */
    private SOSArgument<List<Path>> systemPropertyFiles = new SOSArgument<>("system_property_files", false);

    /** - Result ------- */
    // YADE 1 - alias - create_result_list - declared but not used
    // private SOSArgument<Boolean> createResultSet = new SOSArgument<>("create_result_set", false, Boolean.valueOf(false));
    // TODO not set default?
    private SOSArgument<SOSComparisonOperator> raiseErrorIfResultSetIs = new SOSArgument<>("raise_error_if_result_set_is", false);
    private SOSArgument<Integer> expectedSizeOfResultSet = new SOSArgument<>("expected_size_of_result_set", false, Integer.valueOf(0));
    // TODO 2 result files ....
    // TODO check if Path is OK (were the file created? on YADE client system - OK)
    private SOSArgument<Path> resultListFile = new SOSArgument<>("result_list_file", false);
    private SOSArgument<Path> resultSetFileName = new SOSArgument<>("result_set_file_name", false);

    /** - Banner ------- */
    private SOSArgument<Path> bannerHeader = new SOSArgument<>("banner_header", false);
    private SOSArgument<Path> bannerFooter = new SOSArgument<>("banner_footer", false);

    /** - E-Mail Arguments ------- */
    // TODO - only if standalone .... ?
    private SOSArgument<Boolean> mailOnSuccess = new SOSArgument<>("mail_on_success", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> mailOnError = new SOSArgument<>("mail_on_error", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> mailOnEmptyFiles = new SOSArgument<>("mail_on_empty_files", false, Boolean.valueOf(false));

    public SOSArgument<List<Path>> getSystemPropertyFiles() {
        return systemPropertyFiles;
    }

    public SOSArgument<Boolean> getMailOnSuccess() {
        return mailOnSuccess;
    }

    public SOSArgument<Boolean> getMailOnError() {
        return mailOnError;
    }

    public SOSArgument<Boolean> getMailOnEmptyFiles() {
        return mailOnEmptyFiles;
    }

    public SOSArgument<Integer> getExpectedSizeOfResultSet() {
        return expectedSizeOfResultSet;
    }

    public SOSArgument<SOSComparisonOperator> getRaiseErrorIfResultSetIs() {
        return raiseErrorIfResultSetIs;
    }

    public SOSArgument<Path> getResultListFile() {
        return resultListFile;
    }

    public SOSArgument<Path> getResultSetFileName() {
        return resultSetFileName;
    }

    public SOSArgument<Path> getBannerHeader() {
        return bannerHeader;
    }

    public SOSArgument<Path> getBannerFooter() {
        return bannerFooter;
    }

}
