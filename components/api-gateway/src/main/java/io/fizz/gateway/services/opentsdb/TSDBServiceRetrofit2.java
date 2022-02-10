package io.fizz.gateway.services.opentsdb;

import io.fizz.gateway.services.opentsdb.retrofit2.ITSDBRetrofit2Service;
import io.fizz.gateway.services.opentsdb.retrofit2.TSDBServiceFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class TSDBServiceRetrofit2 implements AbstractTSDBService {
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
    public List<TSDBModels.Metric> query(TSDBModels.MultiMetricQueryRequest request) throws TSDBAPIErrorException, IOException {
        Response<List<TSDBModels.Metric>> res = service.query(request).execute();
        ValidateResponse(res);
        return res.body();
    }

    @Override
    public CompletableFuture<List<TSDBModels.Metric>> queryAsync(TSDBModels.MultiMetricQueryRequest request) {
        final Call<List<TSDBModels.Metric>> call = service.query(request);
        final CompletableFuture<List<TSDBModels.Metric>> future = new CompletableFuture<>();
        call.enqueue(new Callback<List<TSDBModels.Metric>>() {
            @Override
            public void onResponse(Call<List<TSDBModels.Metric>> call, Response<List<TSDBModels.Metric>> response) {
                future.complete(response.body());
            }

            @Override
            public void onFailure(Call<List<TSDBModels.Metric>> call, Throwable throwable) {
                future.completeExceptionally(new CompletionException(throwable));
            }
        });

        return future;
    }

    @Override
    public void put(List<TSDBModels.DataPoint> request) throws Exception {
        Response<Void> res = service.put(request).execute();
        ValidateResponse(res);
    }

    private <T> void ValidateResponse(Response<T> resp) throws TSDBAPIErrorException {
        if (!resp.isSuccessful()) {
            throw new TSDBAPIErrorException(resp.code(), resp.message());
        }
    }
}
