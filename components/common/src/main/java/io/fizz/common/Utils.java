package io.fizz.common;

import com.amazonaws.util.EC2MetadataUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Utils {
    public static Date TIME_BEGIN = new Date(315532800000L); // 01/01/1980
    public static Date TIME_END = new Date(Long.MAX_VALUE);

    public static int MD5_LENGTH = 16;
    private static byte BYTE_VALUE = ~(0);
    public static byte[] MD5_MAX_VALUE = new byte[]{
            BYTE_VALUE, BYTE_VALUE, BYTE_VALUE, BYTE_VALUE,
            BYTE_VALUE, BYTE_VALUE, BYTE_VALUE, BYTE_VALUE,
            BYTE_VALUE, BYTE_VALUE, BYTE_VALUE, BYTE_VALUE,
            BYTE_VALUE, BYTE_VALUE, BYTE_VALUE, BYTE_VALUE
    };

    public static long now() {
        return new Date().getTime();
    }

    public static <T> CompletableFuture<T> failedFuture(final Throwable th) {
        final CompletableFuture<T> future = new CompletableFuture<>();

        failFuture(future, th);

        return future;
    }

    public static <T> void failFuture(final CompletableFuture<T> aFuture, final Throwable aEx) {
        if (aEx instanceof CompletionException) {
            aFuture.completeExceptionally(aEx);
        }
        else {
            aFuture.completeExceptionally(new CompletionException(aEx));
        }
    }

    public static boolean isAlphaNumeric(String str) {
        return str.matches("[A-Za-z0-9.\\-_/]+");
    }

    public static boolean isEmpty(String str) {
        return str.length() == 0;
    }

    public static boolean isNullOrEmpty(String aValue) {
        return Objects.isNull(aValue) || aValue.length() == 0;
    }

    public static <T> CompletableFuture<T> async(final Supplier<CompletableFuture<T>> aSupplier) {
        try {
            return aSupplier.get();
        }
        catch (Exception ex) {
            return Utils.failedFuture(ex);
        }
    }

    public static void assertRequiredArgument(Object aValue, String aErrorMessage) {
        if (Objects.isNull(aValue)) {
            throw new IllegalArgumentException(aErrorMessage);
        }
    }

    public static void assertArgumentRange(double aValue, double aMinValue, double aMaxValue, String aErrorMessage) {
        if (aValue < aMinValue || aValue > aMaxValue) {
            throw new IllegalArgumentException(aErrorMessage);
        }
    }

    public static void assertArgumentRange(long aValue, long aMinValue, long aMaxValue, String aErrorMessage) {
        if (aValue < aMinValue || aValue > aMaxValue) {
            throw new IllegalArgumentException(aErrorMessage);
        }
    }

    public static void assertArgumentRange(Integer aValue, int aMinValue, int aMaxValue, String aErrorMessage) {
        if (Objects.nonNull(aValue) && (aValue < aMinValue || aValue > aMaxValue)) {
            throw new IllegalArgumentException(aErrorMessage);
        }
    }

    public static void assertRequiredArgument(Object aValue, RuntimeException aException) {
        if (Objects.isNull(aValue)) {
            throw aException;
        }
    }

    public static void assertOptionalArgumentLength(String aValue, int aMaxLength, RuntimeException aException) {
        if (Objects.nonNull(aValue) && aValue.length() > aMaxLength) {
            throw aException;
        }
    }

    public static void assertOptionalArgumentLength(String aValue, int aMaxLength, String aMessage) {
        if (Objects.nonNull(aValue) && aValue.length() > aMaxLength) {
            throw new IllegalArgumentException(aMessage);
        }
    }

    public static void assertRequiredArgumentLength(String aValue, int aMinLength, int aMaxLength, String aErrorMessage) {
        if (Objects.isNull(aValue) || aValue.length() < aMinLength || aValue.length() > aMaxLength) {
            throw new IllegalArgumentException(aErrorMessage);
        }
    }

    public static void assertRequiredArgumentLength(String aValue, int aMaxLength, String aErrorMessage) {
        if (Objects.isNull(aValue) || aValue.length() > aMaxLength) {
            throw new IllegalArgumentException(aErrorMessage);
        }
    }

    public static void assertRequiredArgumentLength(String aValue, int aMaxLength, RuntimeException aException) {
        if (Objects.isNull(aValue) || aValue.length() > aMaxLength) {
            throw aException;
        }
    }

    public static void assertArgumentsEquals(final Object aLHS, final Object aRHS, final String aErrorMessage) {
        if (!Objects.equals(aLHS, aRHS)) {
            throw new IllegalArgumentException(aErrorMessage);
        }
    }

    public static void assertStateEquals(final Object aLHS, final Object aRHS, final String aErrorMessage) {
        if (!Objects.equals(aLHS, aRHS)) {
            throw new IllegalStateException(aErrorMessage);
        }
    }

    public static void assertArgumentNull(final Object aArgument, final String aErrorMessage) {
        if (Objects.nonNull(aArgument)) {
            throw new IllegalArgumentException(aErrorMessage);
        }
    }

    public static void assertRequiedArgumentMatches(final String aValue, final String aRegex, final String aErrorMessage) {
        if (Objects.isNull(aValue) || !aValue.matches(aRegex)) {
            throw new IllegalArgumentException(aErrorMessage);
        }
    }

    public static void assertOptionalArgumentMatches(final String aValue, final String aRegex, final String aErrorMessage) {
        if (Objects.nonNull(aValue) && !aValue.matches(aRegex)) {
            throw new IllegalArgumentException(aErrorMessage);
        }
    }

    public static String tsdbSegment(final String value) {
        if (value.equals("any")) {
            return value;
        }
        return hashMD5(value);
    }

    public static String hashMD5(final String value) {
        try {
            return DatatypeConverter.printHexBinary(md5Sum(value));
        }
        catch (Exception ex) {
            return value;
        }
    }

    public static byte[] md5Sum(final String value) {
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(value.getBytes());
            return md.digest();
        }
        catch (Exception ex) {
            return null;
        }
    }

    public static String computeSHA256(final String aValue) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] mac = digest.digest(aValue.getBytes(StandardCharsets.UTF_8));
        byte[] hash = Base64.getEncoder().encode(mac);

        return new String(hash, StandardCharsets.UTF_8);
    }

    public static String computeHMACSHA256(final String aPayload,
                                           final String aSecret) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                aSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        hmac.init(secretKeySpec);
        byte[] mac = hmac.doFinal(aPayload.getBytes(StandardCharsets.UTF_8));
        byte[] hash = Base64.getEncoder().encode(mac);
        return new String(hash, StandardCharsets.UTF_8);
    }

    public static String getLogString(final Throwable cause) {
        StringBuilder log = new StringBuilder("Cause: " + cause);
        log.append(" Message: ").append(cause.getMessage());
        if (!Objects.isNull(cause.getCause())) {
            log.append(" NestedCause: ").append(cause.getCause());
            log.append(" NestedMessage: ").append(cause.getCause().getMessage());
        }
        return log.toString();
    }

    public static String loadFileAsResource(String aPath) throws IOException {
        try (InputStream inputStream = Utils.class.getResourceAsStream(aPath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines()
                    .collect(Collectors.joining(System.lineSeparator()));
        }
        catch (NullPointerException ex) {
            throw new IOException("invalid file path: " + aPath);
        }
    }

    public static int murmur2(byte[] data) {
        return org.apache.kafka.common.utils.Utils.murmur2(data);
    }

    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException ex) {
            System.out.println("InetAddress failed");
            return EC2MetadataUtils.getPrivateIpAddress();
        }
    }

    public static Map<String, Object> buildNewRelicErrorMap(final NewRelicErrorPriority aPriority,
                                                            final String aCheckPoint) {
        Map<String, Object> newRelicErrorMap = new HashMap<>();
        newRelicErrorMap.put("priority", aPriority.value());
        newRelicErrorMap.put("checkpoint", aCheckPoint);
        return newRelicErrorMap;
    }
}

