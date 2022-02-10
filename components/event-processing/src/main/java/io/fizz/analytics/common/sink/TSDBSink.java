package io.fizz.analytics.common.sink;

import io.fizz.analytics.common.opentsdb.TSDBAPIErrorException;
import io.fizz.common.LoggingService;
import io.fizz.analytics.common.opentsdb.TSDBModels;
import io.fizz.analytics.common.opentsdb.TSDBService;
import io.fizz.analytics.common.opentsdb.TSDBServiceRetrofit2;
import io.fizz.analytics.common.source.tsdb.TSDBMetricRowAdapter;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TSDBSink implements AbstractSink<Row>, Serializable {
    private final LoggingService.Log logger = LoggingService.getLogger(TSDBSink.class);

    private final URL endpoint;

    public TSDBSink(final URL aEndpoint) {
        if (Objects.isNull(aEndpoint)) {
            throw new IllegalArgumentException("invalid opentsdb endpoint specified.");
        }
        endpoint = aEndpoint;
    }

    @Override
    public void put(final Dataset<Row> ds) {
        ds.foreachPartition(rowIterator -> {
            if (!rowIterator.hasNext()) {
                return;
            }

            final TSDBService service = new TSDBServiceRetrofit2(endpoint);
            final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
            final List<TSDBModels.DataPoint> dps = new ArrayList<>();
            int rowCount = 0;

            do {
                final Row row = rowIterator.next();
                dps.addAll(adapter.run(row));
                rowCount++;
            } while (rowIterator.hasNext());

            logger.info(String.format("Uploading %d data points for partition", dps.size()));
            if (dps.size() > 0) {
                try {
                    service.put(dps);
                } catch (TSDBAPIErrorException | IOException ex) {
                    logger.error(ex.getMessage());
                    throw ex;
                }
            }
            else {
                logger.warn(String.format("No data points created for %d rows", rowCount));
            }
        });
    }
}
