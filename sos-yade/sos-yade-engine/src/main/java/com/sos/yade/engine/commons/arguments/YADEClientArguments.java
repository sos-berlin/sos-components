package com.sos.yade.engine.commons.arguments;

import java.nio.file.Path;
import java.util.List;

import com.sos.commons.util.SOSComparisonOperator;
import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;

public class YADEClientArguments extends ASOSArguments {

    public final static String LABEL = "Client";
    /** - Result ------- */
    // YADE 1 - alias - create_result_list - declared but not used
    // private SOSArgument<Boolean> createResultSet = new SOSArgument<>("create_result_set", false, Boolean.valueOf(false));
    // TODO not set default?
    private SOSArgument<SOSComparisonOperator> raiseErrorIfResultSetIs = new SOSArgument<>("raise_error_if_result_set_is", false);
    private SOSArgument<Integer> expectedSizeOfResultSet = new SOSArgument<>("expected_size_of_result_set", false);
    // YADE1 - resultListFile - used only in the YADE JOB - contains the file path - the same as result_set_file_name...
    // private SOSArgument<Path> resultListFile = new SOSArgument<>("result_list_file", false);
    private SOSArgument<Path> resultSetFileName = new SOSArgument<>("result_set_file_name", false);

    private SOSArgument<List<Path>> systemPropertyFiles = new SOSArgument<>("system_property_files", false);

    /** - Banner ------- */
    private SOSArgument<Path> bannerHeader = new SOSArgument<>("banner_header", false);
    private SOSArgument<Path> bannerFooter = new SOSArgument<>("banner_footer", false);

    /** - E-Mail Arguments ------- */
    // TODO - only if standalone .... ?
    private SOSArgument<Boolean> mailOnSuccess = new SOSArgument<>("mail_on_success", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> mailOnError = new SOSArgument<>("mail_on_error", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> mailOnEmptyFiles = new SOSArgument<>("mail_on_empty_files", false, Boolean.valueOf(false));

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

    public SOSArgument<Path> getResultSetFileName() {
        return resultSetFileName;
    }

    public SOSArgument<List<Path>> getSystemPropertyFiles() {
        return systemPropertyFiles;
    }

    public SOSArgument<Path> getBannerHeader() {
        return bannerHeader;
    }

    public SOSArgument<Path> getBannerFooter() {
        return bannerFooter;
    }

}
