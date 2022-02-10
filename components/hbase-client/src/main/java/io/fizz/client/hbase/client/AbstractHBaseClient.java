package io.fizz.client.hbase.client;

import io.fizz.client.hbase.HBaseClientModels;

import java.util.concurrent.CompletableFuture;

public interface AbstractHBaseClient {
    CompletableFuture<Boolean> put(final HBaseClientModels.Put put);
    CompletableFuture<Void> delete(final HBaseClientModels.Delete delete);
    CompletableFuture<HBaseClientModels.Result> get(final HBaseClientModels.Get aGetModel);
    CompletableFuture<HBaseClientModels.Scanner> scan(final HBaseClientModels.Scan aScanModel);
    CompletableFuture<HBaseClientModels.Result> increment(final HBaseClientModels.Increment aIncrementModel);
}
