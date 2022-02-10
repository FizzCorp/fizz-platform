package io.fizz.chat.application.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AzureProviderConfigTest {
    @Test
    @DisplayName("it should create provider config object with valid params")
    void testAzureProviderConfig() {
        try {
            final String baseUrl = "http://sample.com";
            final String secret = "123456789";
            final AzureProviderConfig config = new AzureProviderConfig(baseUrl, secret);

            Assertions.assertNotNull(config);
            Assertions.assertEquals(baseUrl, config.baseUrl());
            Assertions.assertEquals(secret, config.secret());
            Assertions.assertEquals(ProviderType.Azure, config.type());
        } catch (IllegalArgumentException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    @DisplayName("it should not create provider config object with null base url")
    void testAzureProviderConfigNullBaseUrl() {
        try {
            new AzureProviderConfig(null, "123456789");
            Assertions.fail("Azure Provider Config should not be created!");
        } catch (IllegalArgumentException ex) {
            Assertions.assertEquals("invalid_base_url", ex.getMessage());
        }
    }

    @Test
    @DisplayName("it should not create provider config object with null secret")
    void testAzureProviderConfigNullSecret() {
        try {
            new AzureProviderConfig("http://sample.com", null);
            Assertions.fail("Azure Provider Config should not be created!");
        } catch (IllegalArgumentException ex) {
            Assertions.assertEquals("invalid_secret", ex.getMessage());
        }
    }
}
