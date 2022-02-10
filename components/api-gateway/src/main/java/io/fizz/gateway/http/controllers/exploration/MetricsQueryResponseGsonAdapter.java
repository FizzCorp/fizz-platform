package io.fizz.gateway.http.controllers.exploration;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.fizz.gateway.services.opentsdb.TSDBModels;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class MetricsQueryResponseGsonAdapter extends TypeAdapter<TSDBModels.Metric> {
    @Override
    public void write(JsonWriter jsonWriter, TSDBModels.Metric metric) throws IOException {
        if (Objects.isNull(metric)) {
            jsonWriter.nullValue();
        }
        else {
            jsonWriter.beginObject();
            jsonWriter.name("metric");
            jsonWriter.value(MetricId.valueBy(metric.metric()).toString().toLowerCase());
            jsonWriter.name("dps");

            jsonWriter.beginObject();
            for (Map.Entry<String,Object> entry: metric.dps().entrySet()) {
                final Object value = entry.getValue();
                jsonWriter.name(entry.getKey());

                if (value instanceof Double) {
                    jsonWriter.value((double)value);
                }
                else
                if (value instanceof Long) {
                    jsonWriter.value((long)value);
                }
                else
                if (value instanceof Integer) {
                    jsonWriter.value((int)value);
                }
                else {
                    jsonWriter.value(value.toString());
                }
            }
            jsonWriter.endObject();
            jsonWriter.endObject();
        }
    }

    @Override
    public TSDBModels.Metric read(JsonReader jsonReader) throws IOException {
        return null;
    }
}
