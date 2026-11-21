package software.ulpgc.kata5;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Webservice {
    private static final String SERVICE_NAME = "primes_service_v1";
    private final String id;
    private final String host;
    private final int port;
    private final String consul;

    public Webservice(String id, String host, int port, String consul) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.consul = consul;
    }

    public void start() throws IOException, InterruptedException {
        Javalin app = Javalin.create(this::init).start(host, port);
        try {
            register();
            System.out.println("Registered : " + id + " (" + SERVICE_NAME + ") as " + host() + ":" + port);

        } catch (IOException | InterruptedException | RuntimeException failure) {
            app.stop();
            throw failure;
        }
    }

    private void init(JavalinConfig config) {
        config.routes.get("/", this::primes);
        config.routes.get("/health", this::health);
    }

    private void health(Context context) {
        context.result("OK");
    }

    private void primes(Context context) {
        String rawN = context.queryParam("n");
        int count = rawN == null ? 10 : Integer.parseInt(rawN);
        if (count < 0) count = 0;
        if (count > 500) count = 500;
        context.header("X-Node", id);
        context.result(new Gson().toJson(firstPrimes(count)));
    }

    private List<Integer> firstPrimes(int count) {
        List<Integer> result = new ArrayList<>();
        int candidate = 2;
        while (result.size() < count) {
            if (isPrime(candidate)) {
                result.add(candidate);
            }
            candidate++;
        }
        return result;
    }

    private boolean isPrime(int num) {
        if (num < 2) return false;
        for (int i=2; i*i <= num; i++) {
            if (num % i == 0) return false;
        }
        return true;
    }

    private void register() throws IOException, InterruptedException {
        try(HttpClient client = client()) {
            HttpResponse<String> response = client.send(requestWith(json()), HttpResponse.BodyHandlers.ofString());
            if  (response.statusCode() / 100 != 2) {
                throw new IOException("Invalid response code (HTTP " + response.statusCode() + "): " + response.body());

            }
        }
    }

    private static HttpClient client() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    private HttpRequest requestWith(String json) {
        return HttpRequest
                .newBuilder(URI.create(consul).resolve("/v1/agent/service/register"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }

    private String host() {
        return isLocalHost() ? "host.docker.internal" : host;

    }

    private boolean isLocalHost() {
        return "localhost".equals(host) ||
                "127.0.0.1".equals(host) ||
                "0.0.0.0".equals(host);
    }

    private String healthUrl() {
        return "http://" + host() + ":" + port + "/health";
    }

    private String json() {
        return new Gson().toJson(Map.of(
                "ID", id,
                "Name", SERVICE_NAME,
                "Address", host(),
                "Port", port,
                "Check", Map.of(
                        "HTTP", healthUrl(),
                        "Interval", "5s",
                        "Timeout", "2s",
                        "DeregisterCriticalServiceAfter", "10m"
                )
        ));
    }


}
