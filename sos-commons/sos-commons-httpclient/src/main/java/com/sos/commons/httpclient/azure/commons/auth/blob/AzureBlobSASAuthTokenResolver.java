package com.sos.commons.httpclient.azure.commons.auth.blob;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.httpclient.azure.commons.auth.AAzureStorageAuthProvider;

public class AzureBlobSASAuthTokenResolver {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

    public static String resolveToken(AzureBlobSASAuthProvider authProvider, String sasToken) throws Exception {
        if (sasToken == null || !sasToken.contains("[")) {
            return sasToken;
        }

        Map<String, String> contextVars = new HashMap<>();
        contextVars.put("api_version", authProvider.getApiVersion());
        contextVars.put("now", FORMATTER.format(Instant.now()));

        // 1. Replace time variables (e.g., [now+1h])
        sasToken = replaceTimeVariables(sasToken);

        // 2. Replace static variables like [now], [api_version]
        String tokenResolvedExceptSign = replaceStaticVariables(sasToken, contextVars);

        if (authProvider.getLogger().isDebugEnabled()) {
            authProvider.getLogger().debug("[resolveToken][tokenResolvedExceptSign]%s", tokenResolvedExceptSign);
        }

        // 3. If [sign] is used, calculate the signature
        if (tokenResolvedExceptSign.contains("[sign]")) {
            if (authProvider.hasAccountKey()) {
                String toSign = extractStringToSign(tokenResolvedExceptSign, authProvider.getAccountName());
                if (authProvider.getLogger().isDebugEnabled()) {
                    authProvider.getLogger().debug("[resolveToken][toSign]%s", toSign);
                }
                String signature = encode(authProvider.signString(toSign));
                contextVars.put("sign", signature);
            } else {
                throw new SOSRequiredArgumentMissingException(
                        "[SASToken->Token]The [sign] variable is used, but the signature cannot be generated because the AccountKey is missing");
            }
        }

        // 4. Final variable replacement including [sign]
        return replaceStaticVariables(tokenResolvedExceptSign, contextVars);
    }

    private static String encode(String s) {
        if (s == null) {
            return null;
        }
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String replaceStaticVariables(String input, Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            input = input.replace("[" + entry.getKey() + "]", entry.getValue());
        }
        return input;
    }

    private static String replaceTimeVariables(String input) {
        Pattern pattern = Pattern.compile("\\[now([+-])(\\d+)([mhd])]");
        Matcher matcher = pattern.matcher(input);
        Instant now = Instant.now();

        while (matcher.find()) {
            String sign = matcher.group(1);
            int amount = Integer.parseInt(matcher.group(2));
            String unit = matcher.group(3);

            Duration delta = switch (unit) {
            case "m" -> Duration.ofMinutes(amount);
            case "h" -> Duration.ofHours(amount);
            case "d" -> Duration.ofDays(amount);
            default -> Duration.ZERO;
            };

            Instant adjusted = sign.equals("+") ? now.plus(delta) : now.minus(delta);
            String replacement = FORMATTER.format(adjusted);
            input = input.replace(matcher.group(0), replacement);
        }

        return input;
    }

    private static String extractStringToSign(String token, String accountName) {
        Map<String, String> params = new TreeMap<>();
        for (String part : token.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && !kv[0].startsWith("[")) {
                params.put(kv[0], kv[1]);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(accountName).append(AAzureStorageAuthProvider.NEW_LINE);
        sb.append(params.getOrDefault("sp", "")).append(AAzureStorageAuthProvider.NEW_LINE);
        sb.append(params.getOrDefault("ss", "")).append(AAzureStorageAuthProvider.NEW_LINE);
        sb.append(params.getOrDefault("srt", "")).append(AAzureStorageAuthProvider.NEW_LINE);
        sb.append(params.getOrDefault("st", "")).append(AAzureStorageAuthProvider.NEW_LINE);
        sb.append(params.getOrDefault("se", "")).append(AAzureStorageAuthProvider.NEW_LINE);
        sb.append("").append(AAzureStorageAuthProvider.NEW_LINE);  // IP range
        sb.append(params.getOrDefault("spr", "")).append(AAzureStorageAuthProvider.NEW_LINE);
        sb.append(params.getOrDefault("sv", "")).append(AAzureStorageAuthProvider.NEW_LINE);

        return sb.toString();
    }
}
