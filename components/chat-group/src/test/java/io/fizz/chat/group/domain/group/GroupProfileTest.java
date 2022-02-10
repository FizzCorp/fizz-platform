package io.fizz.chat.group.domain.group;

import io.fizz.common.domain.UserId;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;

public class GroupProfileTest {
    private static final UserId validCreatedBy = new UserId("userA");
    private static final String validTitle = StringUtils.repeat("-", 128);
    private static final String validImageURL = StringUtils.repeat("-", 1024);
    private static final String validDescription = StringUtils.repeat("-", 1024);
    private static final String validType = StringUtils.repeat("-", 64);

    @Test
    @DisplayName("it should allow a valid group profile to be created")
    public void validCreationTest() {
        GroupProfile profile = new GroupProfile(validCreatedBy, validTitle, validImageURL, validDescription, validType);

        Assertions.assertEquals(validCreatedBy, profile.createdBy());
        Assertions.assertEquals(validTitle, profile.title());
        Assertions.assertEquals(validImageURL, profile.imageURL());
        Assertions.assertEquals(validDescription, profile.description());
        Assertions.assertEquals(validType, profile.type());
    }

    @Test
    @DisplayName("it should update profile with valid values")
    public void updateProfileTest() {
        GroupProfile profile = new GroupProfile(validCreatedBy, validTitle, validImageURL, validDescription, validType);
        final String newTitle = "newTitle";
        final String newDescription = "newDescription";
        final String newImageURL = "newImageURL";
        final String newType = "newType";

        profile.update(newTitle, newImageURL, newDescription, newType);

        Assertions.assertEquals(newTitle, profile.title());
        Assertions.assertEquals(newImageURL, profile.imageURL());
        Assertions.assertEquals(newDescription, profile.description());
        Assertions.assertEquals(newType, profile.type());
    }

    @Test
    @DisplayName("it should not allow a profile to be created if created by user is missing")
    public void missingCreatedByTest() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new GroupProfile(null, validTitle, validImageURL, validDescription, validType)
        );
    }

    @Test
    @DisplayName("it should not allow a profile to be created if the title is invalid")
    public void invalidTitleTest() {
        String longTitle = StringUtils.repeat("-", 129);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new GroupProfile(validCreatedBy, longTitle, validImageURL, validDescription, validType)
        );

        String invalidCharTitle = "#_%%";
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new GroupProfile(validCreatedBy, invalidCharTitle, validImageURL, validDescription, validType)
        );
    }

    @Test
    @DisplayName("it should not allow a profile to be created if the imageURL is larger than max length")
    public void maxLengthImageURLTest() {
        String longImageURL = StringUtils.repeat("-", 1025);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new GroupProfile(validCreatedBy, validTitle, longImageURL, validDescription, validType)
        );
    }

    @Test
    @DisplayName("it should not allow a profile to be created if the description is larger than max length")
    public void maxLengthDescriptionTest() {
        String longDescription = StringUtils.repeat("-", 1025);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new GroupProfile(validCreatedBy, validTitle, validImageURL, longDescription, validType)
        );
    }

    @Test
    @DisplayName("it should not allow a profile to be created if the type is invalid")
    public void invalidTypeTest() {
        String longType = StringUtils.repeat("-", 65);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new GroupProfile(validCreatedBy, validTitle, validImageURL, validDescription, longType)
        );

        String invalidCharType = "invalid type";

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new GroupProfile(validCreatedBy, validTitle, validImageURL, validDescription, invalidCharType)
        );
    }
}
