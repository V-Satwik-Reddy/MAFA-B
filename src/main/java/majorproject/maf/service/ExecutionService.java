package majorproject.maf.service;

import majorproject.maf.dto.request.ExecuteRequest;
import majorproject.maf.dto.response.TransactionDto;
import majorproject.maf.model.UserProfile;
import majorproject.maf.repository.UserProfileRepository;
import org.springframework.transaction.annotation.Transactional;
import majorproject.maf.model.Stock;
import majorproject.maf.model.Transaction;
import majorproject.maf.model.User;
import majorproject.maf.repository.StockRepository;
import majorproject.maf.repository.UserRepository;
import org.springframework.stereotype.Service;


@Service
public class ExecutionService {

    private final DashboardService ds;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final PriceFetch priceFetch;
    private final UserProfileRepository userProfileRepository;

    public ExecutionService( DashboardService ds, StockRepository stockRepository, UserRepository userRepository, PriceFetch priceFetch, UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
        this.ds = ds;
        this.stockRepository = stockRepository;
        this.userRepository = userRepository;
        this.priceFetch = priceFetch;
    }

    @Transactional
    public TransactionDto buyShares(ExecuteRequest request, int id) {
        User user = userRepository.getReferenceById(id);
        UserProfile userProfile = userProfileRepository.findByUserId(id);
        double price=priceFetch.fetchCurrentPrice(request.getSymbol());
        double totalCost=request.getQuantity()*price;
        if(userProfile.getBalance()< totalCost){
            return null;
        }
        userProfileRepository.debitBalance(id,totalCost);
        Transaction t=new Transaction();
        t.setAsset(request.getSymbol());
        t.setType("buy");
        t.setAmount(totalCost);
        t.setAssetQuantity(request.getQuantity());
        t.setUser(user);
        ds.createTransaction(t);
        if (stockRepository.findByUserIdAndSymbol(id, request.getSymbol()) == null) {
            stockRepository.save(new Stock(request.getSymbol(), request.getQuantity(), user));
        }
        else {
            stockRepository.incrementShares(id, request.getSymbol(), request.getQuantity());
        }
        return new TransactionDto(t.getId(),t.getType(),t.getAsset(),t.getAssetQuantity(),t.getAmount(),t.getCreatedAt());
    }

    @Transactional
    public TransactionDto sellShares(ExecuteRequest request, int id) {
        User user = userRepository.getReferenceById(id);
        Stock stock=stockRepository.findByUserIdAndSymbol(id, request.getSymbol());
        if(stock==null||request.getQuantity()>stock.getShares()){
            return null;
        }
        double price=priceFetch.fetchCurrentPrice(request.getSymbol());
        double totalAmount=request.getQuantity()*price;
        userProfileRepository.creditBalance(id,totalAmount);
        Transaction t=new Transaction();
        t.setAsset(request.getSymbol());
        t.setType("sell");
        t.setAmount(totalAmount);
        t.setAssetQuantity(request.getQuantity());
        t.setUser(user);
        ds.createTransaction(t);
        stockRepository.decrementShares(id, request.getSymbol(), request.getQuantity());
        if(stock.getShares()-request.getQuantity()<=0){
            stockRepository.deleteByUserIdAndSymbol(id,request.getSymbol());
        }

        return new TransactionDto(t.getId(),t.getType(),t.getAsset(),t.getAssetQuantity(),t.getAmount(),t.getCreatedAt());
    }

}
