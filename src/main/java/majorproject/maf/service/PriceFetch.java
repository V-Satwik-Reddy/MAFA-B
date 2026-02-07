package majorproject.maf.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import majorproject.maf.cache.PriceCacheService;
import majorproject.maf.dto.response.*;
import majorproject.maf.model.StockPrice;
import majorproject.maf.repository.StockPriceRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PriceFetch {
    static int counter = 0;
    private static final String BASE_URL = "https://www.alphavantage.co/query?function=%s&symbol=%s&apikey=%s";
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
            "5U2X9PNQAF0MLG4O",
            "C93EGUKLTINJ27L6",
            "1IQMMKS54FSBM5BN",
            "O97Y65K6NZHLYYKD",
            "X7A3C3DZ5UZM3JYH",
            "GA55UPGG7B5G9WDA",
            "ZGKFZD2XZEYMTSI2",
            "IPX9HKGA2AI2O5X4"
    };

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final StockPriceRepository stockPriceRepository;
    private final ProfileService profileService;
    private final PriceCacheService priceCacheService;

    public PriceFetch(StockPriceRepository stockPriceRepository, ProfileService profileService, PriceCacheService priceCacheService) {
        this.priceCacheService = priceCacheService;
        this.profileService = profileService;
        this.stockPriceRepository = stockPriceRepository;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Cacheable(value = "currentPrices",key = "#symbol")
    public double fetchCurrentPrice(String symbol) {
        try {
            StockPrice stockPrice = stockPriceRepository.findTopBySymbolOrderByDateDesc(symbol);
            if(stockPrice != null){
                return stockPrice.getClose();
            }
            stockPrice=fetchLast100DailyPrice(symbol).getFirst();
            return stockPrice.getClose();

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch price for " + symbol, e);
        }
    }

    @Cacheable(value = "historicalPrices",key = "#symbol")
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
                stockPrice.setDate(LocalDate.parse(dateStr));
                stockPrice.setOpen(dailyData.path("1. open").asDouble());
                stockPrice.setHigh(dailyData.path("2. high").asDouble());
                stockPrice.setLow(dailyData.path("3. low").asDouble());
                stockPrice.setClose(dailyData.path("4. close").asDouble());
                stockPrice.setVolume(dailyData.path("5. volume").asLong());
                prices.add(stockPrice);
            }
            stockPriceRepository.saveAll(prices);
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

    public StockChange fetchPriceChange(String symbol) {
        StockChange cachedChange = priceCacheService.cacheStockChange(symbol,null);
        if(cachedChange != null){
            return cachedChange;
        }
        List<Double> prices= stockPriceRepository.singleSymbolPriceChange(symbol);
        Double latestPrice= prices.get(0);
        Double previousPrice= prices.get(1);
        return getChange(symbol,latestPrice,previousPrice);
    }

    public void addPreviousDayPrices(){
        try {
            List<String> symbols = stockPriceRepository.findAllSymbols();
            int c=0;
            for (String symbol : symbols) {
                if(c==API_KEYS.length){
                    Thread.sleep(120000);
                    c=0;
                }
                c++;
                String apiKey = getNextApiKey();
                String url = String.format(BASE_URL, "GLOBAL_QUOTE", symbol, apiKey);

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
                JsonNode globalQuote = root.path("Global Quote");
                if (globalQuote.isMissingNode() || globalQuote.isEmpty()) {
                    throw new RuntimeException("Invalid global quote data for symbol: " + symbol);
                }
                StockPrice stockPrice = new StockPrice();
                stockPrice.setSymbol(symbol);
                stockPrice.setDate(LocalDate.parse(globalQuote.get("07. latest trading day").asText()));
                if(stockPriceRepository.findTopBySymbolOrderByDateDesc(symbol).getDate().equals(stockPrice.getDate())){
                    continue;
                }
                stockPrice.setOpen(globalQuote.get("02. open").asDouble());
                stockPrice.setHigh(globalQuote.get("03. high").asDouble());
                stockPrice.setLow(globalQuote.get("04. low").asDouble());
                stockPrice.setClose(globalQuote.get("05. price").asDouble());
                stockPrice.setVolume(globalQuote.get("06. volume").asLong());
                System.out.println("Saving price for "+symbol+" on "+stockPrice.getDate());
                stockPriceRepository.save(stockPrice);
            }
        }catch (Exception e){
            throw new RuntimeException("Failed to fetch previous day prices", e);
        }
    }

    public List<StockChange> fetchUserStockChanges(UserDto u) {
        PreferenceResponse prefs = profileService.getPreferences(u);
        Set<String> symbols = prefs.getCompanyIds()
                .stream()
                .map(CompanyDto::getSymbol)
                .collect(Collectors.toSet());
        if(symbols.isEmpty()){
            //implement the if no companies are selected case get the selected sectors and fetch top companies from those sectors
            symbols = new HashSet<>();
        }
        if(symbols.isEmpty()){
            symbols= profileService.getUserHoldings(u.getId()).stream().map(Share::getSymbol).collect(Collectors.toSet());
        }
        List<StockChange> stockChanges = new ArrayList<>();
        Set<String> nonCachedSymbols=new HashSet<>();
        for(String symbol: symbols){
            StockChange change = priceCacheService.cacheStockChange(symbol,null);
            if(change == null){
                nonCachedSymbols.add(symbol);
                System.out.println("Cache miss for symbol: " + symbol);
            }else{
                stockChanges.add(change);
            }
        }
        if(nonCachedSymbols.isEmpty()){
            return stockChanges;
        }
        List<StockPrice> changes = stockPriceRepository.multipleSymbolPriceChange(nonCachedSymbols,nonCachedSymbols.size()*2);
        changes.sort(Comparator.comparing(StockPrice::getSymbol).thenComparing(StockPrice::getDate));
        for (int i=0;i<changes.size();i+=2) {
            StockChange change = getChange(changes.get(i+1).getSymbol(), changes.get(i+1).getClose(), changes.get(i).getClose());
            stockChanges.add(change);
        }
        return stockChanges;
    }
    public StockChange getChange(String symbol, Double d1, Double d2) {
        Double change = d1 - d2;
        Double changePercent = (change / d2) * 100;
        return priceCacheService.cacheStockChange(symbol,new StockChange(symbol,d1,change,changePercent));
    }
}
