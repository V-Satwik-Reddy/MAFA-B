package majorproject.maf.service;

import majorproject.maf.dto.response.StockDto;
import majorproject.maf.dto.response.TransactionDto;
import majorproject.maf.model.Stock;
import majorproject.maf.model.Transaction;
import majorproject.maf.model.enums.Period;
import majorproject.maf.model.enums.TransactionType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import majorproject.maf.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DashboardService {

    private final TransactionRepository transactionRepo;
    private final StockRepository stockRepo;
    private final StockPriceRepository stockPriceRepository;

    public DashboardService(TransactionRepository transactionRepo, StockRepository stockRepo, StockPriceRepository stockPriceRepository) {
        this.transactionRepo = transactionRepo;
        this.stockRepo = stockRepo;
        this.stockPriceRepository = stockPriceRepository;
    }

    public List<TransactionDto> getUserTransactions(int id) {
        return transactionRepo.findByUserIdOrderByCreatedAtDesc(id)
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

    public List<TransactionDto> getUserTransactions(int id, Integer limit, Integer page, Period period) {
        List<Transaction> transactions;
        int pageNumber = (page == null || page < 1) ? 0 : page - 1;

        Pageable pageable = null;
        if (limit != null) {
            pageable = PageRequest.of(pageNumber, limit, Sort.by("createdAt").descending());
        }
        LocalDateTime cutoff = resolvePeriod(period);
        // 🔹 CASE 1 — no filters
        if (cutoff == null && limit == null) {
            transactions = transactionRepo.findByUserIdOrderByCreatedAtDesc(id);
        }
        // 🔹 CASE 2 — lastDays only
        else if (cutoff != null && limit == null) {
            transactions = transactionRepo.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(id, cutoff);
        }
        // 🔹 CASE 3 — limit only
        else if (cutoff == null) {
            transactions = transactionRepo.findByUserIdOrderByCreatedAtDesc(id, pageable);
        }
        // 🔹 CASE 4 — limit + lastDays
        else {
            transactions = transactionRepo.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(id, cutoff, pageable);
        }

        return transactions.stream()
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

    private LocalDateTime resolvePeriod(Period period) {
        if (period == null || period == Period.ALL) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();

        return switch (period) {
            case LAST_7_DAYS -> now.minusDays(7);
            case LAST_30_DAYS -> now.minusDays(30);
            case LAST_90_DAYS -> now.minusDays(90);
            case LAST_1_YEAR -> now.minusYears(1);
            default -> null;
        };
    }

    public void createTransaction(Transaction transaction) {
        transactionRepo.save(transaction);
    }

    public List<StockDto> getHoldingsDetails(int userId) {
        List<StockDto> ans = new ArrayList<>();
        List<Stock> holdings = stockRepo.findByUserId(userId);
        holdings.sort(Comparator.comparing(Stock::getSymbol));
        List<TransactionDto> alltxns = getUserTransactions(userId);
        List<Double> prices = stockPriceRepository.batchFind(holdings.stream().map(Stock::getSymbol).toList());

        int i = 0;
        for (Stock stock : holdings) {
            StockDto stockDto = new StockDto();
            stockDto.setSymbol(stock.getSymbol());
            stockDto.setShares(stock.getShares());
            double avgBuyPrice = getAvgBuyPrice(stock, alltxns);
            stockDto.setAvgBuyPrice(avgBuyPrice);
            double currentPrice;
            currentPrice = prices.get(i++);
            stockDto.setTotalAmount(stock.getShares() * currentPrice);
            stockDto.setCurrentPrice(currentPrice);
            double gainLoss = (currentPrice - avgBuyPrice) * stock.getShares();
            stockDto.setGainLoss(gainLoss);
            ans.add(stockDto);
        }
        return ans;
    }

    private static double getAvgBuyPrice(Stock stock, List<TransactionDto> alltxns) {
        double totalAmount = 0.0;
        long buyQty = stock.getShares();
        for (TransactionDto txn : alltxns) {
            if (txn.getAsset().equals(stock.getSymbol()) && txn.getType() == TransactionType.BUY) {
                long q = txn.getAssetQuantity();
                if (buyQty >= q) {
                    totalAmount += txn.getAmount();
                    buyQty -= q;
                } else {
                    totalAmount += (txn.getAmount() / q) * buyQty;
                    buyQty = 0;
                }
            }
            if (buyQty <= 0) break;
        }
        return totalAmount / stock.getShares();
    }
}
