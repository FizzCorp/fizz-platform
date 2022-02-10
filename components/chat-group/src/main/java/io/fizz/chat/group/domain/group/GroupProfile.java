package io.fizz.chat.group.domain.group;

import io.fizz.common.Utils;
import io.fizz.common.domain.UserId;

import java.util.Objects;

public class GroupProfile {
    private final UserId createdBy;
    private String title;
    private String imageURL;
    private String description;
    private String type;

    public GroupProfile(final UserId aCreatedBy,
                        final String aTitle,
                        final String aImageURL,
                        final String aDescription,
                        final String aType) {
        Utils.assertRequiredArgument(aCreatedBy, "invalid_created_by");

        createdBy = aCreatedBy;
        setTitle(aTitle);
        setImageURL(aImageURL);
        setDescription(aDescription);
        setType(aType);
    }

    public UserId createdBy() {
        return createdBy;
    }

    public String title() {
        return title;
    }

    public String imageURL() {
        return imageURL;
    }

    public String description() {
        return description;
    }

    public String type() {
        return type;
    }

    public void update(final String aNewTitle,
                       final String aNewImageURL,
                       final String aNewDescription,
                       final String aNewType) {
        if (Objects.nonNull(aNewTitle)) {
            setTitle(aNewTitle);
        }
        if (Objects.nonNull(aNewImageURL)) {
            setImageURL(aNewImageURL);
        }
        if (Objects.nonNull(aNewDescription)) {
            setDescription(aNewDescription);
        }
        if (Objects.nonNull(aNewType)) {
            setType(aNewType);
        }
    }

    private void setTitle(final String aTitle) {
        Utils.assertOptionalArgumentLength(aTitle, 128, new IllegalArgumentException("invalid_title"));
        Utils.assertOptionalArgumentMatches(aTitle, "[A-Za-z0-9 _-]*", "invalid_title");

        this.title = aTitle;
    }

    private void setImageURL(final String aImageURL) {
        Utils.assertOptionalArgumentLength(
                aImageURL, 1024, new IllegalArgumentException("invalid_image_url")
        );

        this.imageURL = aImageURL;
    }

    private void setDescription(final String aDescription) {
        Utils.assertOptionalArgumentLength(
                aDescription, 1024, new IllegalArgumentException("invalid_description")
        );

        this.description = aDescription;
    }

    private void setType(final String aType) {
        Utils.assertOptionalArgumentLength(aType, 64, new IllegalArgumentException("invalid_type"));
        Utils.assertOptionalArgumentMatches(aType, "[A-Za-z0-9_-]*", "invalid_type");

        this.type = aType;
    }
}
