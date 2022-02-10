package io.fizz.analytics.jobs.streamProcessing.store;

import org.apache.spark.sql.Dataset;

import java.util.List;

public interface AbstractRecordStore<T> {
    void put(final List<String> records);
    Dataset<T> createDataset();
}
