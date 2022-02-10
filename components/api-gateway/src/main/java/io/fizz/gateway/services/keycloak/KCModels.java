package io.fizz.gateway.services.keycloak;

public class KCModels {

    public static class Token {
        final String access_token;
        final String expires_in;

        public Token(String access_token, String expires_in) {
            this.access_token = access_token;
            this.expires_in = expires_in;
        }
    }

    public static class Role {
        final String name;

        public Role(String name) {
            this.name = name;
        }
    }

    public static class Secret {
        final String value;

        public Secret(String value) {
            this.value = value;
        }
    }
}
