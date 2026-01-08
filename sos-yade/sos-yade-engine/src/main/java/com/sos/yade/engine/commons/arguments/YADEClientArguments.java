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
    private SOSArgument<SOSComparisonOperator> raiseErrorIfResultSetIs = new SOSArgument<>("RaiseErrorIfResultSetIs", false);
    private SOSArgument<Integer> expectedResultSetCount = new SOSArgument<>("ExpectedResultSetCount", false);
    // YADE1 - resultListFile - used only in the YADE JOB - contains the file path - the same as result_set_file_name...
    // private SOSArgument<Path> resultListFile = new SOSArgument<>("result_list_file", false);
    // String because can contains expressions, e.g. [date:.... timezone:]
    private SOSArgument<String> resultSetFile = new SOSArgument<>("ResultSetFile", false);

    private SOSArgument<List<Path>> systemPropertyFiles = new SOSArgument<>("SystemPropertyFiles", false);

    /** - Banner ------- */
    private SOSArgument<Path> bannerHeader = new SOSArgument<>("banner_header", false);
    private SOSArgument<Path> bannerFooter = new SOSArgument<>("banner_footer", false);

    public boolean isCheckResultSetCountEnabled() {
        return expectedResultSetCount.isDirty() || raiseErrorIfResultSetIs.isDirty();
    }

    public SOSArgument<Integer> getExpectedResultSetCount() {
        return expectedResultSetCount;
    }

    public SOSArgument<SOSComparisonOperator> getRaiseErrorIfResultSetIs() {
        return raiseErrorIfResultSetIs;
    }

    public SOSArgument<String> getResultSetFile() {
        return resultSetFile;
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
