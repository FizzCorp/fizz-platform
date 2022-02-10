package io.fizz.chat.moderation.application.service;

import io.fizz.chat.application.AbstractApplicationRepository;
import io.fizz.chat.application.impl.ApplicationService;
import io.fizz.chat.application.impl.HBaseApplicationRepository;
import io.fizz.chat.application.impl.MockApplicationService;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.client.hbase.client.MockHBaseClient;
import io.fizz.common.domain.ApplicationId;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
public class ContentModerationServiceTest {
    private static final int TEST_TIMEOUT = 10;

    private static Vertx vertx;
    private static ApplicationId appId;
    private static ApplicationId appA;
    private static ApplicationId appB;
    private static ContentModerationService moderationService;

    @BeforeAll
    static void setUp(VertxTestContext aContext) throws Exception {
        vertx = Vertx.vertx();
        appId = new ApplicationId("testApp");
        appA = new ApplicationId("appA");
        appB  = new ApplicationId("appB");
        AbstractHBaseClient defaultClient = new MockHBaseClient();
        final AbstractApplicationRepository appRepo = new HBaseApplicationRepository(defaultClient);
        ApplicationService appService = new MockApplicationService(appRepo);

        moderationService = new MockContentModerationService(
                appService, vertx);

        aContext.completeNow();
    }

    @AfterAll
    static void tearDown(VertxTestContext aContext) {
        vertx.close(res -> aContext.completeNow());
    }

    @Test
    @DisplayName("it should replace filtered word")
    void testFilterWord(VertxTestContext aContext) throws InterruptedException {
        final String str = "This list contains filter Filter 1, that should be replaced";
        final String strFiltered = "This list contains filter ***, that should be replaced";

        moderationService.filter(appId, str).thenApply(result -> {
            Assertions.assertEquals(strFiltered, result);
            aContext.completeNow();
            return null;
        });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should replace filtered word")
    void testFilterLongWord(VertxTestContext aContext) throws InterruptedException {
        final String str = "This list contains filter Filter 1 Filter 2 Filter 3 Filter 4 and Filter 5, that should be replaced";
        final String strFiltered = "This list contains filter *** Filter 2 *** Filter 4 and ***, that should be replaced";

        moderationService.filter(appId, str).thenApply(result -> {
            Assertions.assertEquals(strFiltered, result);
            aContext.completeNow();
            return null;
        });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should moderate text messages")
    void validModerateTextMessagesTest(VertxTestContext aContext) throws InterruptedException {
        List<String> requestTexts = new ArrayList<String>() {{
            add("Hi, This is sample Filter 1. See you around");
            add("this is sample Filter 6");
            add("this is the Filter 3. Bye");
        }};

        List<String> expectedResult = new ArrayList<String>() {{
            add("Hi, This is sample ***. See you around");
            add("this is sample Filter 6");
            add("this is the ***. Bye");
        }};

        moderationService.filter(appId.value(), requestTexts)
                .thenApply(actualResult -> {
                    Assertions.assertEquals(requestTexts.size(), actualResult.size());
                    for (int ti = 0; ti < requestTexts.size(); ti++) {
                        Assertions.assertEquals(expectedResult.get(ti), actualResult.get(ti));
                    }
                    aContext.completeNow();
                   return null;
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should moderate empty text messages")
    void validModerateEmptyTextMessagesTest(VertxTestContext aContext) throws InterruptedException {

        List<String> listWithEmptyText = new ArrayList<String>() {{
            add("Text 1");
            add("");
            add("Text 3");
        }};

        moderationService.filter(appId.value(), listWithEmptyText)
                .thenApply(actualResult -> {
                    Assertions.assertEquals(listWithEmptyText.size(), actualResult.size());
                    for (int ti = 0; ti < listWithEmptyText.size(); ti++) {
                        Assertions.assertEquals(listWithEmptyText.get(ti), actualResult.get(ti));
                    }
                    aContext.completeNow();
                    return null;
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should throw exception for oversize text array")
    void invalidTextOverSizeArrayTest(VertxTestContext aContext) throws InterruptedException {
        List<String> oversizeList = new ArrayList<String>() {{
            add("Text 1");
            add("Text 2");
            add("Text 3");
            add("Text 4");
            add("Text 5");
            add("Text 6");
        }};
        moderationService.filter(appId.value(), oversizeList)
                .handle((result, error) -> {
                    Assertions.assertEquals("java.lang.IllegalArgumentException: invalid_texts_array_size", error.getMessage());
                    aContext.completeNow();
                    return null;
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should throw exception for invalid text length")
    void invalidMaximumLengthTextTest(VertxTestContext aContext) throws InterruptedException {
        final byte[] largeText = new byte[2000];
        new Random().nextBytes(largeText);
        String text = new String(largeText, StandardCharsets.UTF_8);

        List<String> listWithLongText = new ArrayList<String>() {{
            add(text);
        }};
        moderationService.filter(appId.value(), listWithLongText)
                .handle((result, error) -> {
                    Assertions.assertEquals("java.lang.IllegalArgumentException: invalid_text_length", error.getMessage());
                    aContext.completeNow();
                    return null;
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should not replace any word")
    void testNoFilterWord(VertxTestContext aContext) throws InterruptedException {
        final String str = "This list does not contains any filter, that can be replaced";

        moderationService.filter(appId, str).thenApply(result -> {
            Assertions.assertEquals(str, result);
            aContext.completeNow();
            return null;
        });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Azure should replace filtered word")
    void testAzureFilterWord(VertxTestContext aContext) throws InterruptedException {
        final String str = "This list contains filter Filter 1, that should be replaced";
        final String strFiltered = "This list contains filter ***, that should be replaced";

        moderationService.filter(appA, str).thenApply(result -> {
            Assertions.assertEquals(strFiltered, result);
            aContext.completeNow();
            return null;
        });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }
}
