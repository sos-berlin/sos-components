package com.sos.joc.monitoring.configuration.objects.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.sos.commons.util.SOSString;
import com.sos.joc.monitoring.configuration.AElement;

public class WorkflowJobRetryErrorPolicy extends AElement {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowJobRetryErrorPolicy.class);

    private static final String ATTRIBUTE_NAME_FAILURES = "failures";

    private static final Pattern RETRY_PATTERN = Pattern.compile("/retry\\+(\\d+):");
    private static final Pattern ENTRY_PATTERN = Pattern.compile("^(\\d+)(?:/(\\d+))?$");

    private final String failures;
    private final List<Rule> rules;

    private record Rule(int start, int interval) {

        boolean matches(int failureCount) {
            if (failureCount < start) {
                return false;
            }
            if (interval == 0) {
                return failureCount == start;
            }
            return (failureCount - start) % interval == 0;
        }
    }

    public WorkflowJobRetryErrorPolicy(Node node) {
        super(node);

        failures = getAttributeValue(ATTRIBUTE_NAME_FAILURES, null);
        rules = process();
    }

    private List<Rule> process() {
        if (SOSString.isEmpty(failures)) {
            return null;
        }

        List<Rule> rules = new ArrayList<>();
        l: for (String entry : failures.split(";", -1)) {
            Matcher matcher = ENTRY_PATTERN.matcher(entry.trim());
            if (!matcher.matches()) {
                LOGGER.warn(String.format("[WorkflowJobRetryErrorPolicy][parse %s=%s][invalid failure rule]%s", ATTRIBUTE_NAME_FAILURES, failures,
                        entry));
                continue l;
            }

            int start = Integer.parseInt(matcher.group(1));
            int interval = matcher.group(2) == null ? 0 : Integer.parseInt(matcher.group(2));

            if (start <= 0 || (matcher.group(2) != null && interval <= 0)) {
                LOGGER.warn(String.format("[WorkflowJobRetryErrorPolicy][parse %s=%s][failure numbers and intervals must be positive]%s",
                        ATTRIBUTE_NAME_FAILURES, failures, entry));
                continue l;
            }

            rules.add(new Rule(start, interval));
        }
        return rules.size() == 0 ? null : rules;
    }

    public boolean shouldNotify(String workflowPosition, boolean isDebugEnabled, String debugMsgPrefixOnFound, String debugMsgPrefixOnNotFound) {
        if (rules == null) {
            if (isDebugEnabled) {
                LOGGER.debug(debugMsgPrefixOnFound + ", RetryErrorPolicy[configured=" + failures + "] notify on every failure (no rules found)");
            }
            return true;
        }
        Matcher retryMatcher = RETRY_PATTERN.matcher(workflowPosition);
        Integer retry = null;
        while (retryMatcher.find()) { // last retry
            retry = Integer.parseInt(retryMatcher.group(1)) + 1;
        }
        if (retry == null) {
            if (isDebugEnabled) {
                LOGGER.debug(debugMsgPrefixOnFound + ", RetryErrorPolicy[configured=" + failures
                        + "] notify on every failure because no retry instructions was found in the workflowPosition=" + workflowPosition);
            }
            return true;
        }

        final int failureCount = retry.intValue();
        boolean result = rules.stream().anyMatch(rule -> rule.matches(failureCount));
        if (isDebugEnabled) {
            if (result) {
                LOGGER.debug(debugMsgPrefixOnFound + ", RetryErrorPolicy[configured=" + failures + "] matches current failureCount=" + failureCount
                        + " (derived from workflowPosition=" + workflowPosition + " + 1)");
            } else {
                LOGGER.debug(debugMsgPrefixOnNotFound + "[RetryErrorPolicy not match] configured failures[" + failures
                        + "] does not match current failureCount=" + failureCount + " (derived from workflowPosition=" + workflowPosition + " + 1)");
            }
        }
        return result;
    }

}
