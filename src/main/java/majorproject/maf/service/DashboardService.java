package majorproject.maf.service;

import majorproject.maf.dto.response.StockDto;
import majorproject.maf.dto.response.TransactionDto;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.model.Stock;
import majorproject.maf.model.Transaction;
import org.springframework.stereotype.Service;
import majorproject.maf.repository.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private final TransactionRepository transactionRepo;
    private final StockRepository stockRepo;
    private final StockPriceRepository stockPriceRepository;
    private final UserCacheService userCacheService;

    public DashboardService(TransactionRepository transactionRepo, StockRepository stockRepo, StockPriceRepository stockPriceRepository,UserCacheService userCacheService) {
        this.transactionRepo = transactionRepo;
        this.stockRepo = stockRepo;
        this.stockPriceRepository = stockPriceRepository;
        this.userCacheService = userCacheService;
    }

    public List<TransactionDto> getUserTransactions(String email) {
        UserDto user = userCacheService.getCachedUser(email);
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

    public void createTransaction(Transaction transaction) {
        transactionRepo.save(transaction);
    }

    public List<StockDto> getHoldingsDetails(String email) {
        List<StockDto> ans=new ArrayList<>();
        UserDto user = userCacheService.getCachedUser(email);
        int userId = user.getId();
        List<Stock> holdings= stockRepo.findByUserId(userId);
        List<TransactionDto> alltxns=transactionRepo.findByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(txn->new TransactionDto(
                txn.getId(),
                txn.getType(),
                txn.getAsset(),
                txn.getAssetQuantity(),
                txn.getAmount(),
                txn.getCreatedAt()
        )).toList();
        List<Double> prices= stockPriceRepository.batchFind(holdings.stream().map(Stock::getSymbol).toList());

        int i=0;
        for(Stock stock:holdings){
            StockDto stockDto=new StockDto();
            stockDto.setSymbol(stock.getSymbol());
            stockDto.setShares(stock.getShares());
            double avgBuyPrice = getAvgBuyPrice(stock, alltxns);
            stockDto.setAvgBuyPrice(avgBuyPrice);
            double currentPrice;
            currentPrice = prices.get(i++);
            stockDto.setTotalAmount(stock.getShares()*currentPrice);
            stockDto.setCurrentPrice(currentPrice);
            double gainLoss=(currentPrice - avgBuyPrice)*stock.getShares();
            stockDto.setGainLoss(gainLoss);
            ans.add(stockDto);
        }
        return ans;
    }

    private static double getAvgBuyPrice(Stock stock, List<TransactionDto> alltxns) {
        double totalAmount=0.0;
        long buyQty = stock.getShares();
        for(TransactionDto txn: alltxns){
            if(txn.getAsset().equals(stock.getSymbol()) && txn.getType().equalsIgnoreCase("BUY")){
                long q=txn.getAssetQuantity();
                if(buyQty>=q){
                    totalAmount+=txn.getAmount();
                    buyQty-=q;
                }else {
                    totalAmount += (txn.getAmount() / q) * buyQty;
                    buyQty = 0;
                }
            }
            if(buyQty<=0) break;
        }
        return totalAmount/ stock.getShares();
    }
}
