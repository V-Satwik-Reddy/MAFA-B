package majorproject.maf.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import majorproject.maf.cache.PriceCacheService;
import majorproject.maf.dto.response.*;
import majorproject.maf.model.StockPrice;
import majorproject.maf.repository.StockPriceRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class PriceFetch {
    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final String BASE_URL = "https://www.alphavantage.co/query?function=%s&symbol=%s&apikey=%s";
    private final String[] API_KEYS;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final StockPriceRepository stockPriceRepository;
    private final ProfileService profileService;
    private final PriceCacheService priceCacheService;
    private final PortfolioService portfolioService;
    private final AlertService alertService;

    public PriceFetch(@Value("${alpha_vantage_api_keys:}") String allApiKeys,StockPriceRepository stockPriceRepository, ProfileService profileService, PriceCacheService priceCacheService, PortfolioService portfolioService, AlertService alertService) {
        this.priceCacheService = priceCacheService;
        this.profileService = profileService;
        this.stockPriceRepository = stockPriceRepository;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.portfolioService = portfolioService;
        this.alertService = alertService;
        API_KEYS = allApiKeys.split(",");
    }

    @Cacheable(value = "currentPrices",key = "#symbol")
    public double fetchCurrentPrice(String symbol) {
        try {
            StockPrice stockPrice = stockPriceRepository.findTopBySymbolOrderByDateDesc(symbol);
            if(stockPrice != null){
                return stockPrice.getClose();
            }
            return fetchLast100DailyPrice(symbol,1,0,null,null,null,null).getHistoricalPrices().getFirst().getClose();


        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch price for " + symbol, e);
        }
    }

    public List<StockPriceDto> fetchBulkCurrentPrice(List<String> symbols) {
        Map<String,StockPrice> sp=stockPriceRepository.batchFind(symbols).stream()
                .collect(Collectors.toMap(StockPrice::getSymbol, sp1 -> sp1));
        List<StockPriceDto> prices=new ArrayList<>();
        for(StockPrice s: sp.values()){
            prices.add(new StockPriceDto(s.getSymbol(), s.getClose(), s.getDate(), s.getOpen(), s.getHigh(), s.getLow(), s.getVolume()));
        }
        return prices;
    }

    @Cacheable(value = "historicalPrices",key = "#symbol")
    public HistoricalPricesWrapperDto fetchLast100DailyPrice(String symbol, Integer limit, Integer offset,
                                                              LocalDate startDate, LocalDate endDate,
                                                              LocalDate beforeDate, LocalDate afterDate) {
        try {
            List<StockPrice> prices;
            PageRequest pageRequest = null;
            if (limit != null) {
                if (offset == null) offset = 0;
                pageRequest = PageRequest.of(offset, limit);
            }

            // Date range filter takes highest priority, then before, then after, then default
            if (startDate != null && endDate != null) {
                if (pageRequest != null) {
                    prices = stockPriceRepository.findBySymbolAndDateBetweenOrderByDateDesc(symbol, startDate, endDate, pageRequest);
                } else {
                    prices = stockPriceRepository.findBySymbolAndDateBetweenOrderByDateDesc(symbol, startDate, endDate);
                }
            } else if (beforeDate != null) {
                if (pageRequest != null) {
                    prices = stockPriceRepository.findBySymbolAndDateBeforeOrderByDateDesc(symbol, beforeDate, pageRequest);
                } else {
                    prices = stockPriceRepository.findBySymbolAndDateBeforeOrderByDateDesc(symbol, beforeDate);
                }
            } else if (afterDate != null) {
                if (pageRequest != null) {
                    prices = stockPriceRepository.findBySymbolAndDateAfterOrderByDateDesc(symbol, afterDate, pageRequest);
                } else {
                    prices = stockPriceRepository.findBySymbolAndDateAfterOrderByDateDesc(symbol, afterDate);
                }
            } else {
                // Default: no date filter
                if (pageRequest != null) {
                    prices = stockPriceRepository.findBySymbolOrderByDateDesc(symbol, pageRequest);
                } else {
                    prices = stockPriceRepository.findBySymbolOrderByDateDesc(symbol);
                }
            }

            if(prices != null && !prices.isEmpty() && prices.size()>=100){
                return new HistoricalPricesWrapperDto(prices.stream().map(s -> new StockPriceDto(s.getSymbol(), s.getClose(), s.getDate(), s.getOpen(), s.getHigh(), s.getLow(), s.getVolume())).collect(Collectors.toCollection(ArrayList::new)));
            }
            // If not enough data in DB and no date filters were applied, fetch from API
            if (startDate != null || endDate != null || beforeDate != null || afterDate != null) {
                // Return whatever we have from DB when date filters are applied
                if (prices == null) prices = new ArrayList<>();
                return new HistoricalPricesWrapperDto(prices.stream().map(s -> new StockPriceDto(s.getSymbol(), s.getClose(), s.getDate(), s.getOpen(), s.getHigh(), s.getLow(), s.getVolume())).collect(Collectors.toCollection(ArrayList::new)));
            }
            String apiKey = getNextApiKey();
            String url = String.format(BASE_URL,"TIME_SERIES_DAILY", symbol, apiKey);
            JsonNode root=alphaVantageRequest(symbol, url);

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
            return new HistoricalPricesWrapperDto(prices.stream().map(s -> new StockPriceDto(s.getSymbol(), s.getClose(), s.getDate(), s.getOpen(), s.getHigh(), s.getLow(), s.getVolume())).collect(Collectors.toCollection(ArrayList::new)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch price for " + symbol, e);
        }
    }

    private String getNextApiKey() {
        int index = counter.getAndUpdate(c -> (c + 1) % API_KEYS.length);
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
            Map<String,StockPrice> stockPrices=new HashMap<>();
            for (String symbol : symbols) {
                if(c==API_KEYS.length){
                    Thread.sleep(10000);
                    c=0;
                }
                c++;
                String apiKey = getNextApiKey();
                String url = String.format(BASE_URL, "GLOBAL_QUOTE", symbol, apiKey);

                JsonNode root=alphaVantageRequest(symbol, url);
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
                stockPrices.put(symbol,stockPrice);
            }
            stockPriceRepository.saveAll(stockPrices.values());
            alertService.checkAlerts(stockPrices);
        }catch (Exception e){
            throw new RuntimeException("Failed to fetch previous day prices", e);
        }
    }

    private JsonNode alphaVantageRequest(String symbol, String url) throws java.io.IOException, InterruptedException {
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
        return root;
    }

    public List<StockChange> fetchUserStockChanges(UserDto u) {
        Set<String> symbols;
        symbols = portfolioService.getWatchlist(u.getId())
                .stream()
                .map(WatchlistDto::getCompany)
                .map(CompanyDto::getSymbol)
                .collect(Collectors.toSet());
        if(symbols.isEmpty()){
            PreferenceResponse prefs = profileService.getPreferences(u);
            symbols = prefs.getCompanyIds()
                    .stream()
                    .map(CompanyDto::getSymbol)
                    .collect(Collectors.toSet());
        }
        if(symbols.isEmpty()){
            symbols= portfolioService.getUserHoldings(u.getId()).stream().map(Share::getSymbol).collect(Collectors.toSet());
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
        List<StockPrice> changes = stockPriceRepository.multipleSymbolPrice(nonCachedSymbols,nonCachedSymbols.size()*2);
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
