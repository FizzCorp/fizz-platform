package io.fizz.session;

import io.vertx.ext.auth.PRNG;
import io.vertx.ext.web.sstore.impl.SharedDataSessionImpl;

import java.util.Date;

public class FizzSessionImpl extends SharedDataSessionImpl {
    private final Date createdOn;

    public Date createdOn() {
        return createdOn;
    }

    public FizzSessionImpl(PRNG random, long timeout, int length) {
        super(random, timeout, length);
        createdOn = new Date();
    }

    public FizzSessionImpl(final Date aCreatedOn) {
        this.createdOn = aCreatedOn;
    }
}
