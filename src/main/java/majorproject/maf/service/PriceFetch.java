package majorproject.maf.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class PriceFetch {
    static int counter = 0;
    private static final String BASE_URL =
            "https://www.alphavantage.co/query?function=%s&symbol=%s&apikey=%s";

    // Rotate API keys
    private static final String[] API_KEYS = {
            "UD1GU1VAGIUPJJJ8",
            "ALDBRKIZ0UVVWWLY",
            "Y02G2WHADMYSRKFH",
            "Q6576M1ODT5IKYJJ",
            "I3ZBIX5S6HYA9MXA",
            "9NGBH4WQU4Q1VV5Q",
            "72HGUXFWCNPMDDAW",
            "JLD7PUGED1CFJLXE",
            "DCHTXOLI6P9PHWSB",
            "5U2X9PNQAF0MLG4O"
    };
    private static final Map<String,Double> cache= new HashMap<>();
    private static final Map<String,JsonNode> l30cache= new HashMap<>();

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PriceFetch() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public double fetchCurrentPrice(String symbol) {
        try {
            if(cache.containsKey(symbol)){
                return cache.get(symbol);
            }
            String apiKey = getNextApiKey();
            String url = String.format(BASE_URL,"GLOBAL_QUOTE", symbol, apiKey);

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
            cache.put(symbol, Double.parseDouble(priceStr));
            return Double.parseDouble(priceStr);

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch price for " + symbol, e);
        }
    }

    public JsonNode fetchLast100DailyPrice(String symbol) {
        try {
            if(l30cache.containsKey(symbol)){
                return l30cache.get(symbol);
            }
            String apiKey = getNextApiKey();
            String url = String.format(BASE_URL,"TIME_SERIES_DAILY", symbol, apiKey);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = objectMapper.readTree(response.body());
            if (root.has("Note") || root.has("Information") || root.has("Error Message")) {
                throw new RuntimeException("Alpha Vantage limit hit for symbol: " + symbol);
            }

            JsonNode metaData = root.path("Meta Data");
            JsonNode timeSeries = root.path("Time Series (Daily)");
            if (metaData.isMissingNode() || timeSeries.isMissingNode() || timeSeries.isEmpty()) {
                throw new RuntimeException("Invalid daily data for symbol: " + symbol);
            }

            l30cache.put(symbol, timeSeries);
            return timeSeries;
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
