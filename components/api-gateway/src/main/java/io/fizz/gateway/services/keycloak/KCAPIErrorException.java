package io.fizz.gateway.services.keycloak;

class KCAPIExceptions {

    public static class ClientNotFound extends Exception {
        private final String clientId;

        public ClientNotFound(String clientId) {
            this.clientId = clientId;
        }
    }

    public static class OperationalError extends Exception {
        OperationalError(String message) {
            super(message);
        }
    }
}
