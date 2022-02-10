package io.fizz.analytics.common.opentsdb.retrofit2;

import io.fizz.analytics.common.opentsdb.TSDBModels;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

import java.util.List;

public interface ITSDBRetrofit2Service {
    @GET("/api/version")
    Call<TSDBModels.Version> version();

    @POST("/api/query")
    Call<List<TSDBModels.Metric>> query(@Body TSDBModels.MultiMetricQueryRequest request);

    @POST("/api/put")
    Call<Void> put(@Body List<TSDBModels.DataPoint> request);
}
