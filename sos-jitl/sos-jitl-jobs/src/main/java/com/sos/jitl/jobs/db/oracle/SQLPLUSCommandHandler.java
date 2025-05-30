package com.sos.jitl.jobs.db.oracle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.js7.job.OrderProcessStepOutcome;

public class SQLPLUSCommandHandler {

    private static final String EXIT_CODE = "exitCode";
    private static final String SQL_ERROR = "sql_error";

    private Map<String, Object> variables = new HashMap<>();
    private final ISOSLogger logger;

    public SQLPLUSCommandHandler(Map<String, Object> variables, ISOSLogger logger) {
        this.variables.putAll(variables);
        this.logger = logger;
    }

    private void writeln(Path file, String line) throws IOException {
        line = line + "\n";
        Files.write(file, line.getBytes(), StandardOpenOption.APPEND);
    }

    public void createSqlFile(SQLPlusJobArguments args, String tempFileName) throws IOException {
        Path sqlScript = Paths.get(tempFileName);

        if (!args.getIncludeFiles().isEmpty()) {
            String[] includeFileNames = args.getIncludeFiles().split(";");
            for (String includeFileName : includeFileNames) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Append file '%s' to script", includeFileName);
                }
                Path dest = Paths.get(includeFileName);
                SOSPath.appendFile(sqlScript, dest);
            }
        }

        if ((args.getCommand() != null) && (!args.getCommand().isEmpty())) {
            writeln(sqlScript, args.getCommand());
        }

        if (!args.getCommandScriptFile().isEmpty()) {

            String scriptFile = args.getCommandScriptFile();

            Path dest = Paths.get(scriptFile);
            SOSPath.appendFile(dest, sqlScript);
        }
        String exit = "exit;";
        writeln(sqlScript, exit);

    }

    public String[] getVariables(SQLPlusJobArguments args, SOSCommandResult sosCommandResult, OrderProcessStepOutcome outcome,
            String[] stdOutStringArray) {

        int intRegExpFlags = Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL;

        boolean aVariableFound = false;

        String regExp = args.getVariableParserRegExpr();
        if (!regExp.isEmpty()) {
            Pattern regExprPattern = Pattern.compile(regExp, intRegExpFlags);
            for (String string : stdOutStringArray) {
                Matcher matcher = regExprPattern.matcher(string);
                if (matcher.matches() && matcher.group().length() > 1) {
                    outcome.putVariable(matcher.group(1), matcher.group(2).trim());
                    aVariableFound = true;
                }
            }
        }
        if (!aVariableFound) {
            if (logger.isDebugEnabled()) {
                logger.debug("no JS-variable definitions found using reg-exp '%s'.", regExp);
            }
        }
        return stdOutStringArray;
    }

    public void handleMessages(SQLPlusJobArguments args, SOSCommandResult sosCommandResult, OrderProcessStepOutcome outcome,
            String[] stdOutStringArray) throws Exception {
        int intRegExpFlags = Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL;

        String strCC = String.valueOf(sosCommandResult.getExitCode());
        String f = "00000";
        strCC = f.substring(0, strCC.length() - 1) + strCC;
        String sqlError = "";

        boolean ignoreSP2MsgNo = false;
        if (args.getIgnoreSp2Messages().contains("*all")) {
            ignoreSP2MsgNo = true;
        }
        boolean ignoreOraMsgNo = false;
        if (args.getIgnoreOraMessages().contains("*all")) {
            ignoreOraMsgNo = true;
        }

        Pattern errorPattern = Pattern.compile("^\\s*SP2-(\\d\\d\\d\\d):\\s*(.*)$", intRegExpFlags);
        Pattern oraPattern = Pattern.compile("^ORA-(\\d\\d\\d\\d\\d):\\s*(.*)$", intRegExpFlags);
        for (String stdoutLine : stdOutStringArray) {
            stdoutLine = stdoutLine.trim();
            Matcher matcher = errorPattern.matcher(stdoutLine);
            Matcher matcher2 = oraPattern.matcher(stdoutLine);
            if (matcher.matches() || matcher2.matches()) {
                boolean isError = false;
                if (matcher.matches() && !ignoreSP2MsgNo) {
                    String msgNo = matcher.group(1).toString();
                    if (!args.getIgnoreSp2Messages().contains(msgNo)) {
                        isError = true;
                    }
                }
                if (matcher2.matches() && !ignoreOraMsgNo) {
                    String msgNo = matcher2.group(1).toString();
                    if (!args.getIgnoreOraMessages().contains(msgNo)) {
                        isError = true;
                    }
                }
                if (isError) {
                    sqlError += stdoutLine;
                    logger.info("error found: %s", stdoutLine);
                } else {
                    logger.info("Error '%s' ignored due to settings", stdoutLine);
                }
            } else {
                logger.info(stdoutLine);
            }
        }
        String stdErr = sosCommandResult.getStdErr();
        outcome.putVariable(SQL_ERROR, sqlError.trim());
        if (sosCommandResult.getExitCode() == 0) {
            if (!stdErr.trim().isEmpty()) {
                sosCommandResult.setExitCode(99);
            }
            if (!sqlError.isEmpty()) {
                sosCommandResult.setExitCode(98);
            }
        }
        outcome.putVariable(EXIT_CODE, sosCommandResult.getExitCode());
        if (sosCommandResult.getExitCode() != 0 && !args.getIgnoreOraMessages().contains(strCC)) {
            throw new Exception(String.format("Exit-Code set to '%1$s': %2$s", sosCommandResult.getExitCode(), sqlError.trim()));
        }
    }

}
