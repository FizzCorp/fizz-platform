package io.fizz.command.bus.impl.cluster.command;

import com.google.protobuf.ByteString;
import io.fizz.command.bus.impl.generated.*;
import io.fizz.common.Utils;
import io.grpc.*;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

/**
 * Provides a GRPC interface for forwarding byte[] packets to consumers on a local event bus.
 * The proxy does not propagate consumer errors. However will send errors if:
 * 1. the consumer fails a reply using Message::fail
 * 2. no consumer is found on the specified address
 * 3. there is response parsing error
 *
 * In all of these cases an error message with UNKNOWN status is sent.
 *
 * */
public class ProxyService extends ProxyServiceGrpc.ProxyServiceVertxImplBase {
    private final Vertx vertx;

    public ProxyService(Vertx aVertx) {
        Utils.assertRequiredArgument(aVertx, "invalid vertx instance");

        this.vertx = aVertx;
    }

    @Override
    public void proxy(RequestModel aRequest, Promise<ReplyModel> aResponse) {
        String address = aRequest.getTo();
        byte[] payload = aRequest.getPayload().toByteArray();

        vertx.eventBus().<byte[]>request(address, payload, aResult -> {
            try {
                if (aResult.succeeded()) {
                    Message<byte[]> message = aResult.result();
                    ReplyModel reply = ReplyModel.newBuilder()
                            .setStatus(Status.OK.getCode().value())
                            .setPayload(ByteString.copyFrom(message.body()))
                            .build();
                    aResponse.complete(reply);
                }
                else {
                    aResponse.complete(
                        buildError(Status.Code.UNKNOWN.value(), aResult.cause().getMessage())
                    );
                }
            }
            catch (Throwable th) {
                aResponse.complete(buildError(Status.Code.UNKNOWN.value(), th.getMessage()));
            }
        });
    }

    ReplyModel buildError(int aStatus, String aMessage) {
        return ReplyModel.newBuilder()
                .setStatus(aStatus)
                .setCause(aMessage)
                .build();
    }
}
