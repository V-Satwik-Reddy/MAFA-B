package majorproject.maf.service;

import majorproject.maf.dto.request.ExecuteRequest;
import majorproject.maf.dto.response.TransactionDto;
import majorproject.maf.exception.InsufficientBalanceException;
import majorproject.maf.model.enums.TransactionType;
import majorproject.maf.repository.UserProfileRepository;
import org.springframework.transaction.annotation.Transactional;
import majorproject.maf.model.Stock;
import majorproject.maf.model.Transaction;
import majorproject.maf.model.user.User;
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
        double price=priceFetch.fetchCurrentPrice(request.getSymbol());
        double totalCost=request.getQuantity()*price;
        int res= userProfileRepository.debitIfSufficientBalance(id,totalCost);
        if(res==0){
            throw new InsufficientBalanceException("Insufficient balance to execute the buy order.");
        }
        Transaction t=new Transaction();
        t.setAsset(request.getSymbol());
        t.setType(TransactionType.BUY);
        t.setAmount(totalCost);
        t.setAssetQuantity(request.getQuantity());
        t.setUser(user);
        ds.createTransaction(t);
        if (stockRepository.findByUserIdAndSymbol(id, request.getSymbol()) == null) {
            Stock s=new Stock();
                s.setSymbol(request.getSymbol());
                s.setShares(request.getQuantity());
                s.setUser(user);
            stockRepository.save(s);
        }
        else {
            stockRepository.incrementShares(id, request.getSymbol(), request.getQuantity());
        }
        return new TransactionDto(t.getId(),t.getType(),t.getAsset(),t.getAssetQuantity(),t.getAmount(),t.getCreatedAt());
    }

    @Transactional
    public TransactionDto sellShares(ExecuteRequest request, int id) {
        User user = userRepository.getReferenceById(id);
        int res= stockRepository.decrementIfSufficientShares(id, request.getSymbol(), request.getQuantity());
        if(res==0){
            throw new InsufficientBalanceException("Insufficient shares to execute the sell order.");
        }
        double price=priceFetch.fetchCurrentPrice(request.getSymbol());
        double totalAmount=request.getQuantity()*price;
        userProfileRepository.creditBalance(id,totalAmount);
        Transaction t=new Transaction();
        t.setAsset(request.getSymbol());
        t.setType(TransactionType.SELL);
        t.setAmount(totalAmount);
        t.setAssetQuantity(request.getQuantity());
        t.setUser(user);
        ds.createTransaction(t);
        stockRepository.deleteIfSharesZero(id, request.getSymbol());

        return new TransactionDto(t.getId(),t.getType(),t.getAsset(),t.getAssetQuantity(),t.getAmount(),t.getCreatedAt());
    }

}
