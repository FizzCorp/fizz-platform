package io.fizz.gateway.services.keycloak;

public interface AbstractKCService {
    boolean authenticate(String applicationId, String payload, String signature) throws Exception;
    boolean isPermitted(String applicationId, String scope) throws Exception;
}
