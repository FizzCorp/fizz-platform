/*
 * Fizz
 * API reference for the Fizz platform.  # Introduction The Fizz platform comprises of the following services:   - **Analytics**: Run analysis on ingested events to provide insights into user behaviour and sentiment.   - **Chat**: A multilingual chat solution for integrating global audiences.   - **Cloud Messaging**: Create topics for communicating between users, systems etc reliably. Communicate using in-app messages or push notifications.   - **Translate**: Translate text in 36 languages in real-time with slang support.  # Authentication Some Fizz APIs use a signature based authentication. Each application is provided with an Id and a secret. The application Id is specified as part of the API route. However the secret is used to compute an HMAC-SHA256 digest of the raw body of the request. In case of get call body will be replaced with json containing nonce as property. It is expected that the digest is supplied in the Authorization header with the keyword HMAC e.g<br/><br/> digest = hmac_sha256(body, app_secret)<br/> Authorization Header => HMAC-SHA256 digest  # Glossary ## Users  All operations in the Fizz services are performed by users. A user is anything that uses the Fizz system. Users can include users, bots, systems etc Please note that Fizz does not ensure that users are uniquely identified and is the responsibility of the application system.  ## Subscriber An entities that subscribes to messages published on a topic (or channel).  ## Topics Implements a topic-based publish-subscribe system that allows entities (publishers) to send messages to subscribers in a reliable way.  ## Channels Establishes a channel of communication (or \"chat-room\") between different entities. Entities can publish messages to a channel which are then delivered to all subscribing entities. Channels persist all messages to a message journal which allows offline entities to read through the message history. A channel can also comprise of multiple topics for sharding large channels.  ## Event A thing of interest that takes place in a system. Fizz allows events to be ingested into the system. The analytics service runs various kinds of analyses on the ingested data to extract valuable insights. 
 *
 * OpenAPI spec version: 1.1.0
 * Contact: support@fizz.io
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package io.swagger.client;

import com.squareup.okhttp.*;
import okio.Buffer;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

import java.io.IOException;

/**
 * Encodes request bodies using gzip.
 *
 * Taken from https://github.com/square/okhttp/issues/350
 */
class GzipRequestInterceptor implements Interceptor {
    @Override public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        if (originalRequest.body() == null || originalRequest.header("Content-Encoding") != null) {
            return chain.proceed(originalRequest);
        }

        Request compressedRequest = originalRequest.newBuilder()
                                                   .header("Content-Encoding", "gzip")
                                                   .method(originalRequest.method(), forceContentLength(gzip(originalRequest.body())))
                                                   .build();
        return chain.proceed(compressedRequest);
    }

    private RequestBody forceContentLength(final RequestBody requestBody) throws IOException {
        final Buffer buffer = new Buffer();
        requestBody.writeTo(buffer);
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return requestBody.contentType();
            }

            @Override
            public long contentLength() {
                return buffer.size();
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                sink.write(buffer.snapshot());
            }
        };
    }

    private RequestBody gzip(final RequestBody body) {
        return new RequestBody() {
            @Override public MediaType contentType() {
                return body.contentType();
            }

            @Override public long contentLength() {
                return -1; // We don't know the compressed length in advance!
            }

            @Override public void writeTo(BufferedSink sink) throws IOException {
                BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                body.writeTo(gzipSink);
                gzipSink.close();
            }
        };
    }
}