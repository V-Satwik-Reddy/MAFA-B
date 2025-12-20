package majorproject.maf.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicInteger;
import java.math.*;
@Service
public class PriceFetch {
    static int counter = 0;
    private static final String BASE_URL =
            "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s";

    // Rotate API keys
    private static final String[] API_KEYS = {
            "UD1GU1VAGIUPJJJ8",
            "ALDBRKIZ0UVVWWLY",
            "Y02G2WHADMYSRKFH",
            "Q6576M1ODT5IKYJJ",
            "I3ZBIX5S6HYA9MXA",
            "9NGBH4WQU4Q1VV5Q",
            "72HGUXFWCNPMDDAW",
            "JLD7PUGED1CFJLXE."
    };

    private static final AtomicInteger keyIndex = new AtomicInteger(0);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PriceFetch() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public double fetchCurrentPrice(String symbol) {
        try {
            String apiKey = getNextApiKey();
            String url = String.format(BASE_URL, symbol, apiKey);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());

            // 🚨 Alpha Vantage rate limit / error response
            if (root.has("Note") || root.has("Information") || root.has("Error Message")) {
                throw new RuntimeException("Alpha Vantage limit hit for symbol: " + symbol);
            }

            JsonNode quote = root.path("Global Quote");
            if (quote.isMissingNode() || quote.isEmpty()) {
                throw new RuntimeException("No Global Quote for symbol: " + symbol);
            }

            String priceStr = quote.path("05. price").asText();
            if (priceStr == null || priceStr.isBlank()) {
                throw new RuntimeException("Missing price for symbol: " + symbol);
            }

            return Double.parseDouble(priceStr);

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch price for " + symbol, e);
        }
    }


    private String getNextApiKey() {
        int index = counter%API_KEYS.length;
        counter++;
        return API_KEYS[index];
    }
}
