package majorproject.maf.service;

import majorproject.maf.dto.SellRequest;
import org.springframework.transaction.annotation.Transactional;
import majorproject.maf.dto.BuyRequest;
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
    public ExecutionService(WebClient.Builder webClientBuilder, DashboardService ds, StockRepository stockRepository, UserRepository userRepository) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
        this.ds = ds;
        this.stockRepository = stockRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Share buyShares(BuyRequest request,String email) {
        User u=userRepository.findByEmail(email);
        if(u.getBalance()< request.getPrice()){
            return null;
        }
        Share s=webClient.post()
                .uri("/vanguard/buy")
                .body(Mono.just(request), BuyRequest.class)
                .retrieve()
                .bodyToMono(Share.class)
                .block(); // block() makes it synchronous
        if(s==null) return null;
        userRepository.debitBalance(u.getEmail(),s.getPrice());
        Transaction t=new Transaction();
        t.setAsset(s.getSymbol());
        t.setType("buy");
        t.setAmount(request.getPrice());
        t.setAssetQuantity(s.getQuantity());
        t.setUser(u);
        ds.createTransaction(t);
        if (stockRepository.findByUserIdAndSymbol(u.getId(), s.getSymbol()) == null) {
            stockRepository.save(new Stock(s.getSymbol(), 0.0, u));
        }
        stockRepository.incrementShares(u.getId(), s.getSymbol(), s.getQuantity());
        return s;
    }

    @Transactional
    public Share sellShares(SellRequest request, String email) {
        User u=userRepository.findByEmail(email);
        Stock stock=stockRepository.findByUserIdAndSymbol(u.getId(), request.getSymbol());
        if(stock==null||request.getQuantity()>stock.getShares()){
            return null;
        }
//        System.out.println(request);
        Share s=webClient.post()
                .uri("/vanguard/sell")
                .body(Mono.just(request), SellRequest.class)
                .retrieve()
                .bodyToMono(Share.class)
                .block(); // block() makes it synchronous
        if(s==null) return null;
//        System.out.println(s);
        userRepository.creditBalance(u.getEmail(),s.getPrice());
        Transaction t=new Transaction();
        t.setAsset(s.getSymbol());
        t.setType("sell");
        t.setAmount(s.getPrice());
        t.setAssetQuantity(s.getQuantity());
        t.setUser(u);
        ds.createTransaction(t);
//        System.out.println(t);
        stockRepository.decrementShares(u.getId(), s.getSymbol(), s.getQuantity());
        return s;
    }

}
