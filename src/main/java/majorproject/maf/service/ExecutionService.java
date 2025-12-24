package majorproject.maf.service;

import majorproject.maf.dto.ExecuteRequest;
import majorproject.maf.dto.TransactionDto;
import org.springframework.transaction.annotation.Transactional;
import majorproject.maf.dto.Share;
import majorproject.maf.model.Stock;
import majorproject.maf.model.Transaction;
import majorproject.maf.model.User;
import majorproject.maf.repository.StockRepository;
import majorproject.maf.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
public class ExecutionService {

    private final WebClient webClient;
    private final DashboardService ds;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final PriceFetch priceFetch;

    public ExecutionService(WebClient.Builder webClientBuilder, DashboardService ds, StockRepository stockRepository, UserRepository userRepository, PriceFetch priceFetch) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
        this.ds = ds;
        this.stockRepository = stockRepository;
        this.userRepository = userRepository;
        this.priceFetch = priceFetch;
    }

    @Transactional
    public TransactionDto buyShares(ExecuteRequest request, String email) {
        User u=userRepository.findByEmail(email);
        double price=priceFetch.fetchCurrentPrice(request.getSymbol());
        double totalCost=request.getQuantity()*price;
        if(u.getBalance()< totalCost){
            return null;
        }
        userRepository.debitBalance(u.getEmail(),totalCost);
        Transaction t=new Transaction();
        t.setAsset(request.getSymbol());
        t.setType("buy");
        t.setAmount(totalCost);
        t.setAssetQuantity(request.getQuantity());
        t.setUser(u);
        ds.createTransaction(t);
        if (stockRepository.findByUserIdAndSymbol(u.getId(), request.getSymbol()) == null) {
            stockRepository.save(new Stock(request.getSymbol(), request.getQuantity(), u));
        }else
            stockRepository.incrementShares(u.getId(), request.getSymbol(), request.getQuantity());

        return new TransactionDto(t.getId(),t.getType(),t.getAsset(),t.getAssetQuantity(),t.getAmount(),t.getCreatedAt());
    }

    @Transactional
    public TransactionDto sellShares(ExecuteRequest request, String email) {
        User u=userRepository.findByEmail(email);
        Stock stock=stockRepository.findByUserIdAndSymbol(u.getId(), request.getSymbol());
        if(stock==null||request.getQuantity()>stock.getShares()){
            return null;
        }
        double price=priceFetch.fetchCurrentPrice(request.getSymbol());
        double totalAmount=request.getQuantity()*price;
        userRepository.creditBalance(u.getEmail(),totalAmount);
        Transaction t=new Transaction();
        t.setAsset(request.getSymbol());
        t.setType("sell");
        t.setAmount(totalAmount);
        t.setAssetQuantity(request.getQuantity());
        t.setUser(u);
        ds.createTransaction(t);
        stockRepository.decrementShares(u.getId(), request.getSymbol(), request.getQuantity());
        if(stock.getShares()-request.getQuantity()<=0){
            stockRepository.deleteByUserIdAndSymbol(u.getId(),request.getSymbol());
        }

        return new TransactionDto(t.getId(),t.getType(),t.getAsset(),t.getAssetQuantity(),t.getAmount(),t.getCreatedAt());
    }

}
