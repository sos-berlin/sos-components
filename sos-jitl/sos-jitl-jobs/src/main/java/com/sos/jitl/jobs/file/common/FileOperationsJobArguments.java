package com.sos.jitl.jobs.file.common;

import com.sos.jitl.jobs.common.JobArgument;

public class FileOperationsJobArguments {

    private JobArgument<String> sourceFile = new JobArgument<String>("source_file");
    private JobArgument<String> targetFile = new JobArgument<String>("target_file");
    private JobArgument<String> sortCriteria = new JobArgument<String>("sort_criteria", "name");
    private JobArgument<String> sortOrder = new JobArgument<String>("sort_order", "asc");
    private JobArgument<String> fileSpec = new JobArgument<String>("file_spec", ".*");

    private JobArgument<String> fileAge = new JobArgument<String>("file_age", "0");// seconds or hh:mm:ss
    private JobArgument<String> minFileAge = new JobArgument<String>("min_file_age", "0");
    private JobArgument<String> maxFileAge = new JobArgument<String>("max_file_age", "0");
    private JobArgument<String> checkSteadyStateInterval = new JobArgument<String>("check_steady_state_interval", "1");

    private JobArgument<String> replacement = new JobArgument<String>("replacement");
    private JobArgument<String> replacing = new JobArgument<String>("replacing");

    private JobArgument<Integer> skipFirstFiles = new JobArgument<Integer>("skip_first_files", 0);
    private JobArgument<Integer> skipLastFiles = new JobArgument<Integer>("skip_last_files", 0);
    private JobArgument<String> minFileSize = new JobArgument<String>("min_file_size", "-1");
    private JobArgument<String> maxFileSize = new JobArgument<String>("max_file_size", "-1");

    private JobArgument<Integer> warningFileLimit = new JobArgument<Integer>("warning_file_limit", 0);
    private JobArgument<Integer> expectedSizeOfResultSet = new JobArgument<Integer>("expected_size_of_result_set", 0);
    private JobArgument<Integer> steadyStateCount = new JobArgument<Integer>("steady_state_count", 30);

    private JobArgument<String> raiseErrorIfResultSetIs = new JobArgument<String>("raise_error_if_result_set_is", "");
    private JobArgument<String> resultList2File = new JobArgument<String>("result_list_file", "");
    private JobArgument<String> onEmptyResultSet = new JobArgument<String>("on_empty_result_set", "");

    private JobArgument<Boolean> useNioLock = new JobArgument<Boolean>("use_nio_lock", false);
    private JobArgument<Boolean> gracious = new JobArgument<Boolean>("gracious", false);
    private JobArgument<Boolean> overwrite = new JobArgument<Boolean>("overwrite", true);
    private JobArgument<Boolean> recursive = new JobArgument<Boolean>("recursive", false);
    private JobArgument<Boolean> createFile = new JobArgument<Boolean>("createFile", false);// create_dir, create_files
    private JobArgument<Boolean> removeDir = new JobArgument<Boolean>("remove_dir", false);
    private JobArgument<Boolean> countFiles = new JobArgument<Boolean>("count_files", false);
    private JobArgument<Boolean> checkSteadyStateOfFiles = new JobArgument<Boolean>("check_steady_state_of_files", false);

    private JobArgument<Integer> flags = new JobArgument<Integer>(null, 0); // internal usage

    private JobArgument<Boolean> createOrders4AllFiles = new JobArgument<Boolean>("create_orders_for_all_files", false);
    private JobArgument<Boolean> createOrder = new JobArgument<Boolean>("create_order", false);// TODO dependency create_orders_for_all_files
    private JobArgument<Boolean> mergeOrderParameter = new JobArgument<Boolean>("merge_order_parameter", false);// TODO dependency create_orders_for_all_files
    private JobArgument<String> orderJobchainName = new JobArgument<String>("order_jobchain_name");
    private JobArgument<String> nextState = new JobArgument<String>("next_state");

    public JobArgument<String> getFileAge() {
        return fileAge;
    }

    public JobArgument<String> getMinFileAge() {
        return minFileAge;
    }

    public JobArgument<String> getMaxFileAge() {
        return maxFileAge;
    }

    public JobArgument<String> getMinFileSize() {
        return minFileSize;
    }

    public JobArgument<String> getMaxFileSize() {
        return maxFileSize;
    }

    public JobArgument<String> getFileSpec() {
        return fileSpec;
    }

    public JobArgument<String> getSourceFile() {
        return sourceFile;
    }

    public JobArgument<String> getTargetFile() {
        return targetFile;
    }

    public JobArgument<String> getSortCriteria() {
        return sortCriteria;
    }

    public JobArgument<String> getSortOrder() {
        return sortOrder;
    }

    public JobArgument<Integer> getSkipFirstFiles() {
        return skipFirstFiles;
    }

    public JobArgument<Integer> getSkipLastFiles() {
        return skipLastFiles;
    }

    public JobArgument<Integer> getWarningFileLimit() {
        return warningFileLimit;
    }

    public JobArgument<Integer> getExpectedSizeOfResultSet() {
        return expectedSizeOfResultSet;
    }

    public JobArgument<Integer> getSteadyStateCount() {
        return steadyStateCount;
    }

    public JobArgument<String> getCheckSteadyStateInterval() {
        return checkSteadyStateInterval;
    }

    public JobArgument<String> getRaiseErrorIfResultSetIs() {
        return raiseErrorIfResultSetIs;
    }

    public JobArgument<String> getResultList2File() {
        return resultList2File;
    }

    public JobArgument<String> getOnEmptyResultSet() {
        return onEmptyResultSet;
    }

    public JobArgument<String> getReplacement() {
        return replacement;
    }

    public JobArgument<String> getReplacing() {
        return replacing;
    }

    public JobArgument<Boolean> getUseNioLock() {
        return useNioLock;
    }

    public JobArgument<Boolean> getGracious() {
        return gracious;
    }

    public JobArgument<Boolean> getOverwrite() {
        return overwrite;
    }

    public JobArgument<Boolean> getRecursive() {
        return recursive;
    }

    public JobArgument<Boolean> getCreateFile() {
        return createFile;
    }

    public JobArgument<Boolean> getRemoveDir() {
        return removeDir;
    }

    public JobArgument<Boolean> getCountFiles() {
        return countFiles;
    }

    public JobArgument<Boolean> getCheckSteadyStateOfFiles() {
        return checkSteadyStateOfFiles;
    }

    public JobArgument<Boolean> getCreateOrders4AllFiles() {
        return createOrders4AllFiles;
    }

    public JobArgument<Boolean> getCreateOrder() {
        return createOrder;
    }

    public JobArgument<Boolean> getMergeOrderParameter() {
        return mergeOrderParameter;
    }

    public JobArgument<String> getOrderJobchainName() {
        return orderJobchainName;
    }

    public JobArgument<String> getNextState() {
        return nextState;
    }

    public Integer getFlags() {
        return flags.getValue();
    }

    public void setFlags(Integer val) {
        flags.setValue(val);
    }
}
