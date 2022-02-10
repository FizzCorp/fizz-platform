package io.fizz.gateway.http.controllers;

import io.fizz.common.Utils;
import io.swagger.client.ApiClient;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class TestUtils {
    public static String createSignature(String aPayload, String aSecret) {
        try {
            return Utils.computeHMACSHA256(aPayload, aSecret);
        }
        catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            return null;
        }
    }

    public static void signV2(final ApiClient aClient,
                              final String aAppId,
                              final String aMethod,
                              final String aPath,
                              final String aBody,
                              final String aSecretKey) throws NoSuchAlgorithmException, InvalidKeyException {
        final String bodyDigest = Utils.computeSHA256(aBody);
        final String stringToSign = aMethod.trim().toUpperCase() + "\n" + aPath + "\n" + bodyDigest;
        final String digest = Utils.computeHMACSHA256(stringToSign, aSecretKey);

        aClient.addDefaultHeader("x-fizz-app-id", aAppId);
        aClient.addDefaultHeader("Authorization", "HMAC-SHA256 " + digest);
    }
}
