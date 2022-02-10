package io.fizz.analytics.common.sink;

import org.apache.spark.sql.Dataset;

@FunctionalInterface
public interface AbstractSink<T> {
    void put(final Dataset<T> ds);
}
