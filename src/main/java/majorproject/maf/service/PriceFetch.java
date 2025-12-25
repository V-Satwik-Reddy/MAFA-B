package majorproject.maf.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import majorproject.maf.model.StockPrice;
import majorproject.maf.repository.StockPriceRepository;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class PriceFetch {
    static int counter = 0;
    private static final String BASE_URL =
            "https://www.alphavantage.co/query?function=%s&symbol=%s&apikey=%s";

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

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final StockPriceRepository stockPriceRepository;

    public PriceFetch(StockPriceRepository stockPriceRepository) {
        this.stockPriceRepository = stockPriceRepository;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public double fetchCurrentPrice(String symbol) {
        try {
            StockPrice stockPrice = stockPriceRepository.findTopBySymbolOrderByDateDesc(symbol);
            if(stockPrice != null){
                return stockPrice.getClose();
            }
            return fetchLast100DailyPrice(symbol).getFirst().getClose();

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch price for " + symbol, e);
        }
    }

    public List<StockPrice> fetchLast100DailyPrice(String symbol) {
        try {
            List<StockPrice> prices= stockPriceRepository.findBySymbolOrderByDateDesc(symbol);
            if(prices != null && !prices.isEmpty() && prices.size()>=100){
                return prices;
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

            prices=new ArrayList<>();
            for (Iterator<String> it = timeSeries.fieldNames(); it.hasNext(); ) {
                String dateStr = it.next();
                JsonNode dailyData = timeSeries.path(dateStr);
                StockPrice stockPrice = new StockPrice();
                stockPrice.setSymbol(symbol);
                stockPrice.setDate(java.time.LocalDate.parse(dateStr));
                stockPrice.setOpen(dailyData.path("1. open").asDouble());
                stockPrice.setHigh(dailyData.path("2. high").asDouble());
                stockPrice.setLow(dailyData.path("3. low").asDouble());
                stockPrice.setClose(dailyData.path("4. close").asDouble());
                stockPrice.setVolume(dailyData.path("5. volume").asLong());

                prices.add(stockPrice);
                stockPriceRepository.save(stockPrice);
            }
            return prices;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch price for " + symbol, e);
        }
    }

    private String getNextApiKey() {
        int index = counter++;
        counter=counter%API_KEYS.length;
        return API_KEYS[index];
    }
}
