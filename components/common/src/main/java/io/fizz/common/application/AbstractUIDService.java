package io.fizz.common.application;

import java.util.concurrent.CompletableFuture;

public interface AbstractUIDService {
    CompletableFuture<Long> nextId();
    CompletableFuture<Long> nextId(final byte[] aNamespace);
    CompletableFuture<Long> findId(final byte[] aName);
    CompletableFuture<Long> createOrFindId(final byte[] aName, boolean aRandomizeId);
    CompletableFuture<Long> createOrFindId(final byte[] aNamespace, final byte[] aName, boolean aRandomizeId);
}
