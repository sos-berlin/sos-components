package com.sos.jitl.jobs.file.common;

import java.nio.file.Path;

import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;
import com.sos.jitl.jobs.common.JobReturnVariable;

public class FileOperationsJobArguments extends JobArguments {

    /* Return arguments */
    private JobReturnVariable<String> returnResultSet = new JobReturnVariable<String>("file_operations_result_set");
    private JobReturnVariable<Integer> returnResultSetSize = new JobReturnVariable<Integer>("file_operations_result_set_size");

    /* Internal arguments */
    private JobArgument<Integer> flags = new JobArgument<Integer>(null, false, 0); // internal usage

    /* Input arguments */
    // argument "source_file" with possible alias "file"
    // private JobArgument<String> sourceFile = new JobArgument<String>("source_file", true, Collections.singletonList("file"));
    private JobArgument<String> sourceFile = new JobArgument<String>("source_file", true);

    private JobArgument<String> targetFile = new JobArgument<String>("target_file", false);
    private JobArgument<String> sortCriteria = new JobArgument<String>("sort_criteria", false, "name");
    private JobArgument<String> sortOrder = new JobArgument<String>("sort_order", false, "asc");
    private JobArgument<String> fileSpec = new JobArgument<String>("file_spec", false, ".*");

    private JobArgument<String> fileAge = new JobArgument<String>("file_age", false, "0");// seconds or hh:mm:ss
    private JobArgument<String> minFileAge = new JobArgument<String>("min_file_age", false, "0");// seconds or hh:mm:ss
    private JobArgument<String> maxFileAge = new JobArgument<String>("max_file_age", false, "0");// seconds or hh:mm:ss

    private JobArgument<String> replacement = new JobArgument<String>("replacement", false);
    private JobArgument<String> replacing = new JobArgument<String>("replacing", false);

    private JobArgument<Integer> skipFirstFiles = new JobArgument<Integer>("skip_first_files", false, 0);
    private JobArgument<Integer> skipLastFiles = new JobArgument<Integer>("skip_last_files", false, 0);
    private JobArgument<String> minFileSize = new JobArgument<String>("min_file_size", false, "-1");
    private JobArgument<String> maxFileSize = new JobArgument<String>("max_file_size", false, "-1");

    private JobArgument<Boolean> useFileLock = new JobArgument<Boolean>("use_file_lock", false, false);
    private JobArgument<Boolean> gracious = new JobArgument<Boolean>("gracious", false, false);
    private JobArgument<Boolean> overwrite = new JobArgument<Boolean>("overwrite", false, true);
    private JobArgument<Boolean> recursive = new JobArgument<Boolean>("recursive", false, false);
    private JobArgument<Boolean> createFile = new JobArgument<Boolean>("create_file", false, false);// create_dir, create_files
    private JobArgument<Boolean> removeDir = new JobArgument<Boolean>("remove_dir", false, false);

    // steady state
    private JobArgument<Integer> steadyStateCount = new JobArgument<Integer>("steady_state_count", false, 0);
    private JobArgument<String> steadyStateInterval = new JobArgument<String>("steady_state_interval", false, "1");

    // result set
    private JobArgument<Integer> expectedSizeOfResultSet = new JobArgument<Integer>("expected_size_of_result_set", false, 0);
    private JobArgument<String> raiseErrorIfResultSetIs = new JobArgument<String>("raise_error_if_result_set_is", false);
    private JobArgument<Path> resultSetFile = new JobArgument<Path>("result_set_file", false);

    public JobReturnVariable<String> getReturnResultSet() {
        return returnResultSet;
    }

    public JobReturnVariable<Integer> getReturnResultSetSize() {
        return returnResultSetSize;
    }

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

    public void setFileSpec(String val) {
        fileSpec.setValue(val);
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

    public JobArgument<Integer> getExpectedSizeOfResultSet() {
        return expectedSizeOfResultSet;
    }

    public JobArgument<Integer> getSteadyStateCount() {
        return steadyStateCount;
    }

    public JobArgument<String> getSteadyStateInterval() {
        return steadyStateInterval;
    }

    public JobArgument<String> getRaiseErrorIfResultSetIs() {
        return raiseErrorIfResultSetIs;
    }

    public JobArgument<Path> getResultSetFile() {
        return resultSetFile;
    }

    public JobArgument<String> getReplacement() {
        return replacement;
    }

    public JobArgument<String> getReplacing() {
        return replacing;
    }

    public JobArgument<Boolean> getUseFileLock() {
        return useFileLock;
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

    public Integer getFlags() {
        return flags.getValue();
    }

    public void setFlags(Integer val) {
        flags.setValue(val);
    }
}
