package com.sos.commons.httpclient;

import java.net.http.HttpClient;

import com.sos.commons.httpclient.commons.ABaseHttpClient;
import com.sos.commons.httpclient.commons.ABaseHttpClientBuilder;
import com.sos.commons.util.loggers.base.ISOSLogger;

/** Base HTTP client wrapper for Java's HttpClient.<br/>
 * Provides convenient methods for executing HTTP requests with or without parsing the response body.<br/>
 * Supports GET, PUT, DELETE, and conditional HEAD fallback.<br/>
 */
public class BaseHttpClient extends ABaseHttpClient {

    protected BaseHttpClient(ISOSLogger logger, HttpClient client) {
        super(logger, client);
    }

    public static Builder withBuilder() {
        return new Builder();
    }

    public static Builder withBuilder(HttpClient.Builder builder) {
        return new Builder(builder);
    }

    public static class Builder extends ABaseHttpClientBuilder<BaseHttpClient, Builder> {

        public Builder() {
            super();
        }

        public Builder(HttpClient.Builder builder) {
            super(builder);
        }

        @Override
        protected BaseHttpClient createInstance(ISOSLogger logger, HttpClient client) {
            return new BaseHttpClient(logger, client);
        }
    }
}
