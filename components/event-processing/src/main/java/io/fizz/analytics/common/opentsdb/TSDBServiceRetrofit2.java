package io.fizz.analytics.common.opentsdb;

import io.fizz.analytics.common.Utils;
import io.fizz.analytics.common.opentsdb.retrofit2.ITSDBRetrofit2Service;
import io.fizz.analytics.common.opentsdb.retrofit2.TSDBServiceFactory;
import io.fizz.analytics.common.source.tsdb.TSDBMetricRowAdapter;
import io.fizz.common.ConfigService;
import io.fizz.common.LoggingService;
import retrofit2.Response;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TSDBServiceRetrofit2 implements TSDBService {
    private static LoggingService.Log logger = LoggingService.getLogger(TSDBService.class);
    private static final int TSDB_PUT_THRESHOLD = ConfigService.instance().getNumber("tsdb.put.threshold").intValue();
    private final ITSDBRetrofit2Service service;

    public TSDBServiceRetrofit2(URL url) {
        service = TSDBServiceFactory.build(url);
    }

    @Override
    public TSDBModels.Version version() throws Exception {
        Response<TSDBModels.Version> res = service.version().execute();
        ValidateResponse(res);
        return res.body();
    }

    @Override
    public List<TSDBModels.Metric> query(TSDBModels.MultiMetricQueryRequest request) throws Exception {
        Response<List<TSDBModels.Metric>> res = service.query(request).execute();
        ValidateResponse(res);
        return res.body();
    }

    @Override
    public void put(List<TSDBModels.DataPoint> request) throws TSDBAPIErrorException, IOException {
        int requestStartIndex = 0;

        while (requestStartIndex < request.size()) {

            int requestEndIndex = requestStartIndex + TSDB_PUT_THRESHOLD;
            if (requestEndIndex > request.size()) {
                requestEndIndex = request.size();
            }

            List<TSDBModels.DataPoint> mutableRequest = request.subList(requestStartIndex, requestEndIndex);

            Response<Void> res = service.put(mutableRequest).execute();
            try {
                ValidateResponse(res);
            } catch (TSDBAPIErrorException ex) {
                logger.error("TSDB put failed with request\n" +
                        "\nmetric: " + mutableRequest.get(0).metric +
                        "\ntags: " + mutableRequest.get(0).tags.toString() +
                        "\nts: " + mutableRequest.get(0).timestamp +
                        "\nvalue: " + mutableRequest.get(0).value.toString());
            }
            requestStartIndex += TSDB_PUT_THRESHOLD;
        }
    }

    private <T> void ValidateResponse(Response<T> resp) throws TSDBAPIErrorException {
        if (!resp.isSuccessful()) {
            throw new TSDBAPIErrorException(resp.code(), resp.message());
        }
    }
}
