package com.sos.jitl.jobs.encrypt;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.job.UnitTestJobHelper;

import js7.data_for_java.order.JOutcome;

public class EncryptJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptJobTest.class);

    @Ignore
    @Test
    public void testMonitoringJob() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("in", "secret");
        args.put("enciphermentCertificate", "-----BEGIN CERTIFICATE-----\r\n" + "MIIEJzCCAg+gAwIBAgIJAMxQSEdXteqPMA0GCSqGSIb3DQEBCwUAMIGNMQswCQYD\r\n"
                + "VQQGEwJERTEPMA0GA1UECAwGQmVybGluMQ8wDQYDVQQHDAZCZXJsaW4xDDAKBgNV\r\n"
                + "BAoMA1NPUzELMAkGA1UECwwCSVQxHDAaBgNVBAMME1NPUyBJbnRlcm1lZGlhdGUg\r\n"
                + "Q0ExIzAhBgkqhkiG9w0BCQEWFGFkbWluQHNvcy1iZXJsaW4uY29tMB4XDTIzMTAw\r\n"
                + "NDE0MDAwNFoXDTI4MTAwMjE0MDAwNFowcjELMAkGA1UEBhMCREUxDzANBgNVBAgM\r\n"
                + "BkJlcmxpbjEPMA0GA1UEBwwGQmVybGluMQwwCgYDVQQKDANTT1MxCzAJBgNVBAsM\r\n"
                + "AklUMRUwEwYDVQQDDAx1ci1ocC1hdC1zb3MxDzANBgNVBC4TBlNPUyBDQTBZMBMG\r\n"
                + "ByqGSM49AgEGCCqGSM49AwEHA0IABEE4iU93tL0iP6yvs5y/KITpipNTEFmk36TU\r\n"
                + "guK25uul07T7GUM8WXQhkZV/aXP5Xg5I3UwLNHTeDjtDKFva15+jbzBtMCkGA1Ud\r\n"
                + "EQQiMCCCDHVyLWhwLWF0LXNvc4IQdXItaHAtYXQtc29zLnNvczARBglghkgBhvhC\r\n"
                + "AQEEBAMCBsAwDgYDVR0PAQH/BAQDAgXgMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggr\r\n"
                + "BgEFBQcDAjANBgkqhkiG9w0BAQsFAAOCAgEAWUdaGkeP9K04uLHHyw3/RQ/FYd6y\r\n"
                + "n3N3Fl4fXUyh9S9yvAM2J843M/BZWFQqJbhQEpz1HUGw0pMYKcG0HmmOHg5x4hH3\r\n"
                + "z7tnR/PQGFp47t2o+k70gzIs5QaH8XtSb4SCxWaYDCxgeBQxO8xArsAKGPQoJ/Zx\r\n"
                + "EopTIJUhH9LJgVbYIH9LrNeJ03N9iFALPLdxZPJWX9Xv203E8YPT/D4P4uJ8jVxb\r\n"
                + "E3CF2Hn9KAy/SycIL4dAe8TKno082c1iRFDZx3khvvdo5d9prHjCylW+HRDUtCMO\r\n"
                + "W8cPjJkkvzQpHu3QQhqb3tBWj0g4fnODoSnP1fj0HYm1fwNW5YfecYn2HCtyssRV\r\n"
                + "yDpfAIjPO5Apgnt28RU3yL8vLrG8lA8cnRKVVhbe1TViHy8VxzlezYMeeaaBwDfQ\r\n"
                + "UKZ97EsXHHRitho2XHFMUR2EC4ci935YmTLj74izEI+v/DelmOKxRlknQU9KXHHW\r\n"
                + "edql9TPxqR6R48Oyirb9T1H5eNw4NtdqGICdhr7/1VfXqiDxPtVynCfeH9Sljdio\r\n"
                + "3DzHTQhUA9t80t8JT2LmW5SmPySdCNcRJizZLd/G2yeMQBffxORcbixhRwkU+pVA\r\n"
                + "P8dZ7KCSEbDZ/hY9xajPopivlFRHyion1K9kNmalAA0xdmu9ZovfpvIic3tvxUrD\r\n" + "gCEOgbK8wjwDlHw=\r\n" + "-----END CERTIFICATE-----");

        UnitTestJobHelper<EncryptJobArguments> h = new UnitTestJobHelper<>(new EncryptJob(null));

        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

}
