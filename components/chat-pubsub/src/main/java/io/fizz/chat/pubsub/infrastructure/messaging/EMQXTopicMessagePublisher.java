package io.fizz.chat.pubsub.infrastructure.messaging;

import io.fizz.chat.pubsub.application.AbstractTopicMessagePublisher;
import io.fizz.chat.pubsub.domain.subscriber.SubscriberId;
import io.fizz.chat.pubsub.domain.topic.TopicMessage;
import io.fizz.chat.pubsub.domain.topic.TopicName;
import io.fizz.chat.pubsub.infrastructure.ConfigService;
import io.fizz.chat.pubsub.infrastructure.serde.AbstractMessageSerde;
import io.fizz.common.Utils;
import io.fizz.common.domain.DomainErrorException;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import okhttp3.Credentials;
import org.apache.http.HttpStatus;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class EMQXTopicMessagePublisher implements AbstractTopicMessagePublisher {

    private static final DomainErrorException ERROR_UNAUTHORIZED = new DomainErrorException("unauthorized");

    private static final String HOST = ConfigService.config().getString("chat.pubsub.emqx.host");
    private static final int PORT = ConfigService.config().getNumber("chat.pubsub.emqx.port").intValue();
    private static final String APP_ID = ConfigService.config().getString("chat.pubsub.emqx.app.id");
    private static final String APP_SECRET = ConfigService.config().getString("chat.pubsub.emqx.app.secret");

    private static final String API_VERSION = "v4";

    private final WebClient client;
    private final AbstractMessageSerde serde;

    public EMQXTopicMessagePublisher(final Vertx aVertx, final AbstractMessageSerde aSerde) {
        Utils.assertRequiredArgument(aVertx, "invalid vertx instance specified.");
        Utils.assertRequiredArgument(aSerde, "invalid message serde specified.");

        client = WebClient.create(aVertx);
        serde = aSerde;
    }

    @Override
    public CompletableFuture<Void> publish(final Set<TopicName> aTopics, final TopicMessage aMessage) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aTopics, "invalid_topics");
            Utils.assertRequiredArgument(aMessage, "invalid_message");

            if (aTopics.size() > 128) {
                throw new IllegalArgumentException("invalid_topics");
            }

            JsonObject bodyJson = new JsonObject()
                    .put("topics", publishTopics(aTopics))
                    .put("qos", 0)
                    .put("payload", serde.serialize(aMessage))
                    .put("clientid", aMessage.from().value());

            return sendRequest(String.format("/api/%s/mqtt/publish", API_VERSION), Buffer.buffer(bodyJson.toString()));
        });
    }

    @Override
    public CompletableFuture<Void> subscribe(final Set<TopicName> aTopics, final SubscriberId aId) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aTopics, "invalid_topics");
            Utils.assertRequiredArgument(aId, "invalid_subscriber_id");

            if (aTopics.size() > 128) {
                throw new IllegalArgumentException("invalid_topics");
            }

            JsonArray bodyJson = new JsonArray();
            for (TopicName topic: aTopics) {
                bodyJson.add(new JsonObject()
                        .put("topic", buildTopicName(topic))
                        .put("clientid", aId.value())
                        .put("qos", 1));
            }

            return sendRequest(String.format("/api/%s/mqtt/subscribe_batch", API_VERSION), Buffer.buffer(bodyJson.toString()));
        });
    }

    @Override
    public CompletableFuture<Void> unsubscribe(final Set<TopicName> aTopics, final SubscriberId aId) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aTopics, "invalid_topics");
            Utils.assertRequiredArgument(aId, "invalid_subscriber_id");

            if (aTopics.size() > 128) {
                throw new IllegalArgumentException("invalid_topics");
            }


            JsonArray bodyJson = new JsonArray();
            for (TopicName topic: aTopics) {
                bodyJson.add(new JsonObject()
                        .put("topic", buildTopicName(topic))
                        .put("clientid", aId.value()));
            }

            return sendRequest(String.format("/api/%s/mqtt/unsubscribe_batch", API_VERSION), Buffer.buffer(bodyJson.toString()));
        });
    }

    private String buildTopicName(final TopicName aTopic) {
        return aTopic.value('/');
    }

    private String publishTopics(Set<TopicName> aTopics) {
        StringBuilder topicsStr = new StringBuilder();
        for (TopicName topic: aTopics) {
            if (topicsStr.length() != 0) {
                topicsStr.append(",");
            }
            topicsStr.append(buildTopicName(topic));
        }

        return topicsStr.toString();
    }

    private CompletableFuture<Void> sendRequest(final String aURL, final Buffer bodyBuffer) {
        final CompletableFuture<Void> future = new CompletableFuture<>();

        final HttpRequest<Buffer> request = client
                .post(PORT, HOST, aURL)
                .putHeader("Authorization", Credentials.basic(APP_ID, APP_SECRET));

        request.sendBuffer(bodyBuffer, aResult -> {
            if (aResult.failed()) {
                future.completeExceptionally(new CompletionException(aResult.cause()));
            } else {
                try {
                    final HttpResponse<Buffer> response = aResult.result();
                    if (response.statusCode() == HttpStatus.SC_OK) {
                        JsonObject responseBody = response.bodyAsJsonObject();
                        int responseCode = 0;
                        /*if (responseBody.containsKey("data")) {
                            JsonArray requests = responseBody.getJsonArray("data");
                            for (int ri=0; ri < requests.size(); ri++) {
                                responseCode = requests.getJsonObject(ri).getInteger("code");
                                if (responseCode != 0) {
                                    break;
                                }
                            }
                        }*/

                        if (responseCode == 0) {
                            responseCode = responseBody.getInteger("code");
                        }
                        if (responseCode == 0) {
                            future.complete(null);
                        }
                        else {
                            String message = responseBody.getString("message");
                            String errorMessage = "Response Code: " +  responseCode + " Message: " + message;
                            future.completeExceptionally(new CompletionException(new DomainErrorException(errorMessage)));
                        }
                    }
                    else if (response.statusCode() == HttpStatus.SC_UNAUTHORIZED) {
                        future.completeExceptionally(new CompletionException(ERROR_UNAUTHORIZED));
                    }
                    else {
                        future.completeExceptionally(new CompletionException(String.valueOf(response.statusCode()), new DomainErrorException(response.statusMessage())));
                    }
                } catch (ClassCastException ex) {
                    future.completeExceptionally(new CompletionException(ex));
                }
            }
        });

        return future;
    }
}
