package io.fizz.gdpr.job.delete.group;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import io.fizz.gdpr.job.AbstractExecutor;
import io.fizz.gdpr.model.GDPRRequest;
import io.fizz.gdpr.repository.GDPRRequestRepository;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;
import org.apache.http.HttpStatus;

import java.util.List;

public class Executor extends AbstractExecutor {
    public static void main(String[] args) {
        try {
            System.exit(ToolRunner.run(new Configuration(), new Executor(), args));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int run(String[] args) {
        super.run(args);

        try {
            final String baseUrl = configService.get("fizz.gateway.url");
            final String resourceUri = "/v1/internal/9e5a3a19-b282-4237-bae0-89c6f835a922";

            final List<GDPRRequest> requests = GDPRRequestRepository.fetch();

            for (GDPRRequest request: requests) {
                String uri = baseUrl
                        + resourceUri
                        + "/apps/" + request.getAppId()
                        + "/users/" + request.getUserId()
                        + "/groups?"
                        + "requesterId=" + request.getId();
                HttpResponse<String> response = Unirest.delete(uri).asString();
                if (response.getStatus() != HttpStatus.SC_OK) {
                    System.out.println("Request Failed with status text: " + response.getStatusText());
                    return 1;
                }
            }

            return 0;
        } catch (Exception e) {
            System.out.println(e);
            return 1;
        }
    }
}
