package io.fizz.chat.emqx.infrastructure.hooks;

import io.fizz.chat.emqx.infrastructure.EMQXConnectedEvent;
import io.fizz.chat.emqx.infrastructure.EMQXDisconnectedEvent;
import io.fizz.chat.emqx.infrastructure.hooks.generated.*;
import io.fizz.chat.emqx.infrastructure.service.AuthService;
import io.fizz.chatcommon.domain.events.DomainEventBus;
import io.fizz.common.Utils;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.Date;

public class ExHookProvider extends HookProviderGrpc.HookProviderImplBase {
    public void DEBUG(String fn, Object req) {
        System.out.println(fn + ", request: " + req);
    }

    private final AuthService authService;
    DomainEventBus eventBus;


    public ExHookProvider(AuthService aService, DomainEventBus aEventBus) {
        Utils.assertRequiredArgument(aService, "invalid hook instance");

        this.authService = aService;
        this.eventBus = aEventBus;
    }

    @Override
    public void onProviderLoaded(ProviderLoadedRequest request, StreamObserver<LoadedResponse> responseObserver) {
        DEBUG("onProviderLoaded", request);
        HookSpec[] specs = {
                HookSpec.newBuilder().setName("client.connected").build(),
                HookSpec.newBuilder().setName("client.disconnected").build(),
                HookSpec.newBuilder().setName("client.authenticate").build(),
                HookSpec.newBuilder().setName("client.check_acl").build(),
        };
        LoadedResponse reply = LoadedResponse.newBuilder().addAllHooks(Arrays.asList(specs)).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void onProviderUnloaded(ProviderUnloadedRequest request, StreamObserver<EmptySuccess> responseObserver) {
        DEBUG("onProviderUnloaded", request);
        EmptySuccess reply = EmptySuccess.newBuilder().build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void onClientConnected(ClientConnectedRequest request, StreamObserver<EmptySuccess> responseObserver) {
        DEBUG("onClientConnected", request);
        final String clientId = request.getClientinfo().getClientid();
        final String username = request.getClientinfo().getUsername();
        final EMQXConnectedEvent event = new EMQXConnectedEvent(clientId, username, new Date().getTime());

        eventBus.publish(event)
                .thenApply(result -> {
                    DEBUG("onClientConnectedResult:", result);
                    EmptySuccess reply = EmptySuccess.newBuilder().build();
                    responseObserver.onNext(reply);
                    responseObserver.onCompleted();
                    return null;
                });
    }

    @Override
    public void onClientDisconnected(ClientDisconnectedRequest request, StreamObserver<EmptySuccess> responseObserver) {
        DEBUG("onClientDisconnected", request);
        final String clientId = request.getClientinfo().getClientid();
        final EMQXDisconnectedEvent event = new EMQXDisconnectedEvent(clientId, new Date().getTime());

        eventBus.publish(event)
                .thenApply(result -> {
                    DEBUG("onClientDisConnectedResult:", result);
                    EmptySuccess reply = EmptySuccess.newBuilder().build();
                    responseObserver.onNext(reply);
                    responseObserver.onCompleted();
                    return null;
                });
    }

    @Override
    public void onClientAuthenticate(ClientAuthenticateRequest request, StreamObserver<ValuedResponse> responseObserver) {
        DEBUG("onClientAuthenticate", request);
        final String clientId = request.getClientinfo().getClientid();
        final String username = request.getClientinfo().getUsername();
        final String password = request.getClientinfo().getPassword();

        authService.onClientAuthenticate(clientId, username, password)
                .thenApply(result -> {
                    ValuedResponse reply = ValuedResponse.newBuilder()
                            .setBoolResult(result)
                            .setType(ValuedResponse.ResponsedType.STOP_AND_RETURN)
                            .build();
                    DEBUG("onClientAuthenticateResult:", result);

                    responseObserver.onNext(reply);
                    responseObserver.onCompleted();
                    return null;
                });
    }

    @Override
    public void onClientCheckAcl(ClientCheckAclRequest request, StreamObserver<ValuedResponse> responseObserver) {
        DEBUG("onClientCheckAcl", request);
        ValuedResponse reply = ValuedResponse.newBuilder()
                .setBoolResult(false)
                .setType(ValuedResponse.ResponsedType.STOP_AND_RETURN)
                .build();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
