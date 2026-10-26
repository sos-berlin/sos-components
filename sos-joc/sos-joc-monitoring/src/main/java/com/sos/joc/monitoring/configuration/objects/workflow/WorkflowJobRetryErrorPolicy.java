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

    private static final String LAST_FAILURE = "last";

    // retry+0:0 or retry+0(10):0
    private static final Pattern RETRY_PATTERN = Pattern.compile("/retry\\+(\\d+)(?:\\((\\d+)\\))?:");
    private static final Pattern ENTRY_PATTERN = Pattern.compile("^(\\d+)(?:/(\\d+))?$");

    private final String failures;
    private final ParsedPolicy policy;

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

    private record ParsedPolicy(List<Rule> rules, boolean notifyOnLastFailure) {
    }

    public WorkflowJobRetryErrorPolicy(Node node) {
        super(node);

        failures = getAttributeValue(ATTRIBUTE_NAME_FAILURES, null);
        policy = process();
    }

    private ParsedPolicy process() {
        if (SOSString.isEmpty(failures)) {
            return new ParsedPolicy(null, false);
        }

        List<Rule> parsedRules = new ArrayList<>();
        boolean last = false;

        for (String entry : failures.split(";", -1)) {
            entry = entry.trim();

            if (LAST_FAILURE.equalsIgnoreCase(entry)) {
                last = true;
                continue;
            }

            Matcher matcher = ENTRY_PATTERN.matcher(entry);
            if (!matcher.matches()) {
                LOGGER.warn(String.format("[WorkflowJobRetryErrorPolicy][parse %s=%s][invalid failure rule]%s", ATTRIBUTE_NAME_FAILURES, failures,
                        entry));
                continue;
            }

            final int start;
            final int interval;

            try {
                start = Integer.parseInt(matcher.group(1));
                interval = matcher.group(2) == null ? 0 : Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException e) {
                LOGGER.warn(String.format("[WorkflowJobRetryErrorPolicy][parse %s=%s][failure number out of range]%s", ATTRIBUTE_NAME_FAILURES,
                        failures, entry));
                continue;
            }

            if (start <= 0 || (matcher.group(2) != null && interval <= 0)) {
                LOGGER.warn(String.format("[WorkflowJobRetryErrorPolicy][parse %s=%s][failure numbers and intervals must be positive]%s",
                        ATTRIBUTE_NAME_FAILURES, failures, entry));
                continue;
            }

            parsedRules.add(new Rule(start, interval));
        }

        if (parsedRules.isEmpty() && !last) {
            return new ParsedPolicy(null, false);
        }

        return new ParsedPolicy(List.copyOf(parsedRules), last);
    }

    public boolean shouldNotify(String workflowPosition, boolean isDebugEnabled, String debugMsgPrefixOnFound, String debugMsgPrefixOnNotFound) {
        if (policy.rules() == null) {
            if (isDebugEnabled) {
                LOGGER.debug(debugMsgPrefixOnFound + ", RetryErrorPolicy[configured=" + failures + "] notify on every failure (no rules found)");
            }
            return true;
        }

        Matcher retryMatcher = RETRY_PATTERN.matcher(workflowPosition);
        Integer retry = null;
        Integer maxTries = null;
        while (retryMatcher.find()) {
            try {
                retry = Integer.parseInt(retryMatcher.group(1)) + 1;
                maxTries = retryMatcher.group(2) == null ? null : Integer.parseInt(retryMatcher.group(2));
            } catch (NumberFormatException e) {
                LOGGER.warn("[WorkflowJobRetryErrorPolicy][parse workflowPosition][retry count out of range]{}", workflowPosition);
                retry = null;
                maxTries = null;
            }
        }

        if (retry == null) {
            if (isDebugEnabled) {
                LOGGER.debug(debugMsgPrefixOnFound + ", RetryErrorPolicy[configured=" + failures
                        + "] notify on every failure because no retry instruction was found in workflowPosition=" + workflowPosition);
            }
            return true;
        }

        final Integer failureCount = retry;
        boolean isLastFailure = policy.notifyOnLastFailure() && maxTries != null && failureCount.equals(maxTries);
        boolean matchesRule = policy.rules().stream().anyMatch(rule -> rule.matches(failureCount.intValue()));
        boolean result = isLastFailure || matchesRule;
        if (isDebugEnabled) {
            if (result) {
                LOGGER.debug(debugMsgPrefixOnFound + ", RetryErrorPolicy[configured=" + failures + "] matches current failureCount=" + failureCount
                        + (isLastFailure ? " (last failure)" : "") + " (derived from workflowPosition=" + workflowPosition + " + 1)");
            } else {
                LOGGER.debug(debugMsgPrefixOnNotFound + ", RetryErrorPolicy[configured=" + failures + "] does not match current failureCount="
                        + failureCount + " (derived from workflowPosition=" + workflowPosition + " + 1)");
            }
        }
        return result;
    }

}
