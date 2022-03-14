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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.jitl.jobs.common.JobLogger;

public class SQLPLUSCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLPLUSCommandHandler.class);
    private static final String EXIT_CODE = "exitCode";
    private static final String SQL_ERROR = "sql_error";
    private Map<String, Object> variables = new HashMap<>();
    private JobLogger logger = null;

    public SQLPLUSCommandHandler(Map<String, Object> variables, JobLogger logger) {
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
                debug(logger, String.format("Append file '%1$s' to script", includeFileName));
                Path dest = Paths.get(includeFileName);
                SOSPath.appendFile(sqlScript, dest);
            }
        }

        if ((args.getCommand() != null) && (!args.getCommand().isEmpty())) {
            writeln(sqlScript, args.getCommand());
        }

        String scriptFile = args.getCommandScriptFile();

        Path dest = Paths.get(scriptFile);
        SOSPath.appendFile(dest, sqlScript);

        String exit = "exit;";
        writeln(sqlScript, exit);
    }

    private String quotes2DoubleQuotes(final String inString) {
        String quotedString = inString;
        if (quotedString != null) {
            quotedString = inString.replaceAll("\"", "\"\"");
        }
        return quotedString;
    }

    private String addQuotes(final String inString) {
        return "\"" + quotes2DoubleQuotes(inString) + "\"";
    }

    public String[] getVariables(SQLPlusJobArguments args, SOSCommandResult sosCommandResult, Map<String, Object> resultMap,
            String[] stdOutStringArray) {

        int intRegExpFlags = Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL;

        boolean aVariableFound = false;

        String regExp = args.getVariableParserRegExpr();
        if (!regExp.isEmpty()) {
            Pattern regExprPattern = Pattern.compile(regExp, intRegExpFlags);
            for (String string : stdOutStringArray) {
                Matcher matcher = regExprPattern.matcher(string);
                if (matcher.matches() && matcher.group().length() > 1) {
                    resultMap.put(matcher.group(1), matcher.group(2).trim());
                    aVariableFound = true;
                }
            }
        }
        if (!aVariableFound) {
            debug(logger, String.format("no JS-variable definitions found using reg-exp '%1$s'.", regExp));
        }
        return stdOutStringArray;
    }

    public void handleMessages(SQLPlusJobArguments args, SOSCommandResult sosCommandResult, Map<String, Object> resultMap,
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
                    log(logger, "error found: " + stdoutLine);
                } else {
                    log(logger, String.format("Error '%1$s' ignored due to settings", stdoutLine));
                }
            }
        }
        String stdErr = sosCommandResult.getStdErr();
        resultMap.put(SQL_ERROR, sqlError.trim());
        if (sosCommandResult.getExitCode() == 0) {
            if (!stdErr.trim().isEmpty()) {
                sosCommandResult.setExitCode(99);
            }
            if (!sqlError.isEmpty()) {
                sosCommandResult.setExitCode(98);
            }
        }
        resultMap.put(EXIT_CODE, sosCommandResult.getExitCode());
        if (sosCommandResult.getExitCode() != 0 && !args.getIgnoreOraMessages().contains(strCC)) {
            throw new Exception(String.format("Exit-Code set to '%1$s': %2$s", sosCommandResult.getExitCode(), sqlError.trim()));
        }
    }

	private void log(JobLogger logger, String log) {
		if (logger != null) {
			logger.info(log);
		} else {
			LOGGER.info(log);
		}
	}

	private void debug(JobLogger logger, String log) {
		if (logger != null) {
			logger.debug(log);
		} else {
			LOGGER.debug(log);
		}
	}
}
