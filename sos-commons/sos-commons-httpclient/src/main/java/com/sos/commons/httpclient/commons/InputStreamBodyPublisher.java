package com.sos.commons.httpclient.commons;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow;
import java.util.function.Supplier;

public class InputStreamBodyPublisher implements HttpRequest.BodyPublisher {

    private final Supplier<InputStream> supplier;
    private final long length;

    InputStreamBodyPublisher(Supplier<InputStream> supplier, long length) {
        this.supplier = supplier;
        this.length = length;
    }

    @Override
    public long contentLength() {
        return length;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
        InputStream is;
        try {
            is = supplier.get();
        } catch (Exception e) {
            subscriber.onError(e);
            return;
        }

        subscriber.onSubscribe(new Flow.Subscription() {

            private volatile boolean done = false;

            @Override
            public void request(long n) {
                if (done) {
                    return;
                }
                try {
                    byte[] buf = new byte[8192];
                    int read;
                    while ((read = is.read(buf)) != -1) {
                        if (done) {
                            return;
                        }
                        subscriber.onNext(ByteBuffer.wrap(buf, 0, read));
                    }
                    done = true;
                    subscriber.onComplete();
                } catch (Throwable t) {
                    done = true;
                    subscriber.onError(t);
                }
            }

            @Override
            public void cancel() {
                done = true;
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        });
    }
}
