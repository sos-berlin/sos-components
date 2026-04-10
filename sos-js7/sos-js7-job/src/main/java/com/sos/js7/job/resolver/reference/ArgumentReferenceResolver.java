package com.sos.js7.job.resolver.reference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.exception.JobArgumentException;

/** Resolves cross-argument references of the form: %{argument_name}
 *
 * This is a post-processing step and must be executed AFTER all value-resolvers (e.g. base64:, enc:, ...) have been applied. */
public final class ArgumentReferenceResolver {

    private static final String IDENTIFIER = ArgumentReferenceResolver.class.getSimpleName();
    private static final Pattern PLACEHOLDER = Pattern.compile("%\\{(.+?)\\}");

    private ArgumentReferenceResolver() {
    }

    /** Resolves all {@link JobArgument} values that contain cross-references in the form of <code>%{argument_name}</code>.
     *
     * <p>
     * This method performs in-place mutation of the provided argument map.<br />
     * Each argument value is evaluated, references are resolved recursively, and the final resolved value is written back via {@code applyValue()}.
     * </p>
     *
     * <p>
     * Supported features:
     * <ul>
     * <li>Recursive argument resolution:<br/>
     * a = %{b}<br/>
     * b = %{c}<br/>
     * c = js7</li>
     * <li>Cycle detection:<br/>
     * a = %{b}<br/>
     * b = %{a}<br/>
     * </li>
     * <li>Self-reference detection:<br/>
     * a = %{a}</li>
     * <li>Null-safe processing</li>
     * </ul>
     * </p>
     *
     * @param logger logger used for error reporting during resolution
     * @param arguments map of argument names to {@link JobArgument} instances (mutated in-place)
     * @throws Exception if a cyclic or invalid reference is detected or resolution fails */
    public static void resolve(ISOSLogger logger, Map<String, JobArgument<?>> arguments) throws Exception {
        boolean isDebugEnabled = logger.isDebugEnabled();
        Map<String, String> cache = new HashMap<>();

        for (Map.Entry<String, JobArgument<?>> entry : arguments.entrySet()) {
            String key = entry.getKey();
            JobArgument<?> arg = entry.getValue();

            Object raw = arg.getValue();
            String original = (raw == null) ? null : raw.toString();

            if (original == null || !original.contains("%{")) {
                continue;
            }

            if (isDebugEnabled) {
                logger.debug("[" + IDENTIFIER + "][resolve][argument]" + arg.toString(true));
            }
            String resolved = resolveValue(arg, key, original, arguments, cache, new HashSet<>());
            if (!Objects.equals(original, resolved)) {
                arg.applyValue(resolved);
            }
        }
    }

    private static String resolveValue(JobArgument<?> arg, String key, String value, Map<String, JobArgument<?>> arguments, Map<String, String> cache,
            Set<String> visited) throws Exception {
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        if (!visited.add(key)) {
            // a = %{b}
            // b = %{a}
            String msg = String.format("[%s][argument=%s, reference=%s][cyclic reference not resolvable]%s", IDENTIFIER, arg.getName(), key, arg
                    .getValue());
            throw new JobArgumentException(msg);
        }

        Matcher matcher = PLACEHOLDER.matcher(value);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String refKey = matcher.group(1).trim();
            JobArgument<?> refArg = arguments.get(refKey);

            if (refKey.equals(key)) {
                // a = %{a}
                String msg = String.format("[%s][argument=%s, reference=%s][self reference not resolvable]%s", IDENTIFIER, arg.getName(), refKey, arg
                        .getValue());
                throw new JobArgumentException(msg);
            }

            if (refArg == null) {
                // a = %{argument name is not in all arguments map}
                String msg = String.format("[%s][argument=%s, reference=%s][unknown reference not resolvable]%s", IDENTIFIER, arg.getName(), refKey,
                        arg.getValue());
                throw new JobArgumentException(msg);
            }

            Object refRaw = refArg.getValue();
            String refValue = (refRaw == null) ? "" : refRaw.toString();

            // recursion
            String replacement = resolveValue(refArg, refKey, refValue, arguments, cache, visited);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement == null ? "" : replacement));
        }
        matcher.appendTail(sb);

        String result = sb.toString();

        cache.put(key, result);
        visited.remove(key);

        return result;
    }
}