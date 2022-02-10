package io.fizz.analytics.common;

import io.fizz.analytics.common.HiveTime;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

/**
 * Transforms from one dataset to another
 */
public interface AbstractTransformer<SourceType, DestType> {
   Dataset<DestType>  transform(final Dataset<SourceType> sourceDS, final HiveTime time);
}
