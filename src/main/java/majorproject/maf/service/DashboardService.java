package majorproject.maf.service;

import majorproject.maf.dto.StockDto;
import majorproject.maf.dto.TransactionDto;
import majorproject.maf.model.Stock;
import majorproject.maf.model.Transaction;
import majorproject.maf.model.User;
import org.springframework.stereotype.Service;
import majorproject.maf.repository.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private final TransactionRepository transactionRepo;
    private final UserRepository userRepo;
    private final StockRepository stockRepo;
    private final PriceFetch priceFetch;
    public DashboardService(TransactionRepository transactionRepo, UserRepository userRepo, StockRepository stockRepo, PriceFetch priceFetch) {
        this.transactionRepo = transactionRepo;
        this.userRepo = userRepo;
        this.stockRepo = stockRepo;
        this.priceFetch = priceFetch;
    }

    public List<TransactionDto> getUserTransactions(String email) {
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return transactionRepo.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(txn -> new TransactionDto(
                        txn.getId(),
                        txn.getType(),
                        txn.getAsset(),
                        txn.getAssetQuantity(),
                        txn.getAmount(),
                        txn.getCreatedAt()
                ))
                .toList();
    }

    public Transaction createTransaction(Transaction transaction) {
        return transactionRepo.save(transaction);
    }

    public List<StockDto> getHoldingsDetails(String email) {
        List<StockDto> ans=new ArrayList<>();
        int userid= userRepo.findByEmail(email).getId();
        List<Stock> holdings= stockRepo.findByUserId(userid);
        List<TransactionDto> alltxns=getUserTransactions(email);
        for(Stock stock:holdings){
            StockDto stockDto=new StockDto();
            stockDto.setSymbol(stock.getSymbol());
            stockDto.setShares(stock.getShares());
            double totalAmount=0.0;
            double avgBuyPrice=0.0;
            double buyQty = 0.0;
            for(TransactionDto txn:alltxns){
                if(txn.asset().equals(stock.getSymbol()) && txn.type().equalsIgnoreCase("BUY")){
                    totalAmount+=txn.amount();
                    buyQty+=txn.assetQuantity();
                }
            }
            avgBuyPrice=totalAmount/buyQty;
            stockDto.setTotalAmount(totalAmount);
            stockDto.setAvgBuyPrice(avgBuyPrice);
            double currentPrice;
            try {
                currentPrice = priceFetch.fetchCurrentPrice(stock.getSymbol());
            } catch (Exception e) {
                currentPrice = 0.0; // or last known price
            }

            stockDto.setCurrentPrice(currentPrice);
            double gainLoss=(currentPrice - avgBuyPrice)*stock.getShares();
            stockDto.setGainLoss(gainLoss);
            ans.add(stockDto);
//            System.out.println(stockDto);
        }
//        System.out.println(ans);
        return ans;
    }
}
