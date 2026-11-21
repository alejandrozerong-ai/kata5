package software.ulpgc.kata5;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Main {
    private static final String BASE_URL = "http://localhost:8080";

    public static void main(String[] args) throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build()) {
            System.out.println("=== Requests of /api/v1/fibonacci/ ===");
            for (int i = 1; i <= 6; i++) {
                sendAndPrint(client, "/api/v1/fibonacci/?n=6", i);
            }

            System.out.println("\n=== Requests of /api/v1/primes/ ===");
            for (int i = 1; i <= 6; i++) {
                sendAndPrint(client, "/api/v1/primes/?n=5", i);
            }
        }

    }

    private static void sendAndPrint(HttpClient client, String path, int requestNumber) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            System.out.printf("#%d [%s] -> HTTP %d | Body: %s%n",
                    requestNumber, path, response.statusCode(), response.body());
            return;
        }
        String node = response.headers().firstValue("X-Node").orElse("unknown");
        System.out.printf("#%d [%s] -> Node: %s | Body: %s%n",
                requestNumber, path, node, response.body());
    }


}
