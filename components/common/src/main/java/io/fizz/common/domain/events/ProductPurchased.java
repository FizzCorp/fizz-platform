package io.fizz.common.domain.events;

import io.fizz.common.domain.*;
import io.fizz.common.domain.events.AbstractDomainEvent;

import java.util.Objects;

public class ProductPurchased extends AbstractDomainEvent {
    public static class Builder extends AbstractDomainEvent.Builder {
        private int amountInCents = -1;
        private String productId;
        private String receipt;

        public ProductPurchased get() throws DomainErrorException {
            return new ProductPurchased(
                id, appId, countryCode, userId, version, sessionId,
                occurredOn, platform, build, custom01, custom02, custom03,
                amountInCents, productId, receipt
            );
        }

        @Override
        public Builder setId(String id) {
            super.setId(id);
            return this;
        }

        @Override
        public Builder setAppId(ApplicationId appId) {
            super.setAppId(appId);
            return this;
        }

        @Override
        public Builder setCountryCode(CountryCode countryCode) {
            super.setCountryCode(countryCode);
            return this;
        }

        @Override
        public Builder setUserId(UserId userId) {
            super.setUserId(userId);
            return this;
        }

        @Override
        public Builder setVersion(Integer version) {
            super.setVersion(version);
            return this;
        }

        @Override
        public Builder setSessionId(String sessionId) {
            super.setSessionId(sessionId);
            return this;
        }

        @Override
        public Builder setOccurredOn(Long occurredOn) {
            super.setOccurredOn(occurredOn);
            return this;
        }

        @Override
        public Builder setPlatform(Platform platform) {
            super.setPlatform(platform);
            return this;
        }

        @Override
        public Builder setBuild(String build) {
            super.setBuild(build);
            return this;
        }

        @Override
        public Builder setCustom01(String custom01) {
            super.setCustom01(custom01);
            return this;
        }

        @Override
        public Builder setCustom02(String custom02) {
            super.setCustom02(custom02);
            return this;
        }

        @Override
        public Builder setCustom03(String custom03) {
            super.setCustom03(custom03);
            return this;
        }

        public Builder setAmountInCents(int amountInCents) {
            this.amountInCents = amountInCents;
            return this;
        }

        public Builder setProductId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder setReceipt(String receipt) {
            this.receipt = receipt;
            return this;
        }
    }

    public static final DomainErrorException ERROR_INVALID_AMOUNT = new DomainErrorException(new DomainError("invalid_purchase_amount"));
    private static final DomainErrorException ERROR_INVALID_PRODUCT_ID = new DomainErrorException(new DomainError("invalid_product_id"));

    private final int amountInCents;
    private final String productId;
    private final String receipt;

    private ProductPurchased(final String aId, final ApplicationId aAppId, CountryCode aCountryCode, final UserId aUserId, int aVersion,
                             final String aSessionId, long aOccurredOn, final Platform aPlatform, final String aBuild,
                             final String aCustom01, final String aCustom02, final String aCustom03,
                             int aAmountInCents, final String aProductId, final String aReceipt) throws DomainErrorException {
        super(aId, aAppId, aCountryCode, aUserId, EventType.PRODUCT_PURCHASED, aVersion, aSessionId,
            aOccurredOn, aPlatform, aBuild, aCustom01, aCustom02, aCustom03);

        if (aAmountInCents < 0) {
            throw ERROR_INVALID_AMOUNT;
        }
        if (Objects.isNull(aProductId) ||  aProductId.length() > MAX_ID_LEN) {
            throw ERROR_INVALID_PRODUCT_ID;
        }

        amountInCents = aAmountInCents;
        productId = aProductId;
        receipt = aReceipt;
    }

    public int getAmountInCents() {
        return amountInCents;
    }

    public String getProductId() {
        return productId;
    }

    public String getReceipt() {
        return receipt;
    }
}
