package majorproject.maf.service;

import majorproject.maf.dto.response.*;
import majorproject.maf.exception.InsufficientBalanceException;
import majorproject.maf.exception.ResourceAlreadyExistsException;
import majorproject.maf.exception.ResourseNotFoundException;
import majorproject.maf.model.PortfolioDailySnapshot;
import majorproject.maf.model.Stock;
import majorproject.maf.model.StockPrice;
import majorproject.maf.model.Transaction;
import majorproject.maf.model.enums.Interval;
import majorproject.maf.model.enums.Period;
import majorproject.maf.model.enums.TransactionType;
import majorproject.maf.model.serving.CompanyMaster;
import majorproject.maf.model.user.UserProfile;
import majorproject.maf.model.user.Watchlist;
import majorproject.maf.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final UserProfileRepository userProfileRepository;
    private final WatchlistRepository watchlistRepository;
    private final CompanyMasterRepository companyMasterRepository;
    private final UserRepository userRepository;
    private final DashboardService dashboardService;
    private final PortfolioDailySnapshotRepository portfolioDailySnapshotRepository;

    public PortfolioService(StockRepository stockRepository, StockPriceRepository stockPriceRepository, UserProfileRepository userProfileRepository, WatchlistRepository watchlistRepository, CompanyMasterRepository companyMasterRepository, UserRepository userRepository, DashboardService dashboardService, PortfolioDailySnapshotRepository portfolioDailySnapshotRepository) {
        this.stockPriceRepository = stockPriceRepository;
        this.stockRepository = stockRepository;
        this.userProfileRepository = userProfileRepository;
        this.watchlistRepository = watchlistRepository;
        this.companyMasterRepository = companyMasterRepository;
        this.userRepository = userRepository;
        this.dashboardService = dashboardService;
        this.portfolioDailySnapshotRepository = portfolioDailySnapshotRepository;
    }

    public List<Share> getUserHoldings(int id) {
        List<Share> s=stockRepository.findByUserId(id).stream().map(
                stock -> new Share(stock.getSymbol(), stock.getShares())
        ).toList();
        Map<String, StockPrice> prices= stockPriceRepository.batchFind(
                s.stream().map(Share::getSymbol).toList()
        );
        for (Share share : s) {
            share.setPrice(prices.get(share.getSymbol()).getClose());
        }
        return s;
    }

    public void depositBalance(int id, double amount) {
        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setUser(userRepository.getReferenceById(id));
        transaction.setAsset("CASH");
        transaction.setAssetQuantity(0L);
        dashboardService.createTransaction(transaction);
        userProfileRepository.creditBalance(id, amount);
    }

    public void withdrawBalance(int id, double amount) {
        int res=userProfileRepository.debitIfSufficientBalance(id, amount);
        if(res==0) throw new InsufficientBalanceException("Insufficient balance to execute the withdrawal.");
        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setAmount(amount);
        transaction.setUser(userRepository.getReferenceById(id));
        transaction.setAsset("CASH");
        transaction.setAssetQuantity(0L);
        dashboardService.createTransaction(transaction);
    }

    public Double getBalance(int id) {
        UserProfile userProfile = userProfileRepository.findByUserId(id);
        return userProfile.getBalance();
    }

    public List<WatchlistDto> getWatchlist(int id) {
        List<Watchlist> wl= watchlistRepository.findByUser(id);
        List<WatchlistDto> wlDto=new ArrayList<>();
        for(Watchlist w: wl){
            CompanyDto c= new CompanyDto(w.getCompany().getId(),w.getCompany().getSymbol(), w.getCompany().getName(), new SectorDto(w.getCompany().getSector().getId(),w.getCompany().getSector().getName()));
            wlDto.add(new WatchlistDto(c, w.getAddedAt()));
        }
        return wlDto;
    }

    public void addToWatchlist(int id, String symbol) {
        CompanyMaster company= companyMasterRepository.findBySymbol(symbol);
        Watchlist w=watchlistRepository.findBySymbolAndUserId(symbol, id);
        if(w!=null) throw new ResourceAlreadyExistsException("Company already in watchlist");
        w=new Watchlist();
        w.setCompany(company);
        w.setUser(userRepository.getReferenceById(id));
        watchlistRepository.save(w);
    }

    public void removeFromWatchlist(int id, String symbol) {
        int i=watchlistRepository.deleteByUserIdAndCompanySymbol(id, symbol);
        if(i==0) throw new ResourseNotFoundException("Watchlist not found");
    }

    public void createEODPortfolioSnapshot() {
        List<UserProfile> users = userProfileRepository.findAll();
        List<Stock> allShares = stockRepository.findAll();
        Map<Integer, List<Stock>> sharesByUser = allShares.stream().collect(Collectors.groupingBy(s -> s.getUser().getId()));
        Map<String,StockPrice> sharePrice = stockPriceRepository.batchFind(allShares.stream().map(Stock::getSymbol).toList());
        List<PortfolioDailySnapshot>  snapshots=new ArrayList<>();
        for(UserProfile user:users){
            Double cashBalance = user.getBalance();
            PortfolioDailySnapshot portfolioDailySnapshot = new PortfolioDailySnapshot();
            double investedValue = 0.0;
            for(Stock stock:sharesByUser.get(user.getId())) {
                investedValue += stock.getShares() * sharePrice.get(stock.getSymbol()).getClose();
            }
            portfolioDailySnapshot.setUser(user.getUser());
            portfolioDailySnapshot.setDate(java.time.LocalDate.now());
            portfolioDailySnapshot.setInvestedValue(investedValue);
            portfolioDailySnapshot.setCashBalance(cashBalance);
            portfolioDailySnapshot.setTotalValue(investedValue + cashBalance);
            snapshots.add(portfolioDailySnapshot);
        }
        portfolioDailySnapshotRepository.saveAll(snapshots);
    }

    public List<PortfolioDailySnapshotDTO> getPortfolioHistory(int id, Period period, Interval interval) {
        LocalDateTime cutoff = dashboardService.resolvePeriod(period);
        List<PortfolioDailySnapshot> snapshots= portfolioDailySnapshotRepository.findByUserIdAndDateAfterOrderByDateAsc(id, cutoff.toLocalDate());
        if (snapshots.isEmpty()) {
            return List.of();
        }
        if (interval == Interval.WEEKLY) {
            WeekFields wf = WeekFields.ISO;

            List<PortfolioDailySnapshot> weekly = snapshots.stream()
                    .collect(Collectors.toMap(s -> {
                        LocalDate d = s.getDate();
                        int year = d.get(wf.weekBasedYear());
                        int week = d.get(wf.weekOfWeekBasedYear());
                        return year + "-" + week; // simple composite key
                    }, Function.identity(), BinaryOperator.maxBy(Comparator.comparing(PortfolioDailySnapshot::getDate))))
                    .values()
                    .stream()
                    .sorted(Comparator.comparing(PortfolioDailySnapshot::getDate))
                    .toList();

            return weekly.stream()
                    .map(s -> new PortfolioDailySnapshotDTO(
                            s.getDate(), s.getTotalValue(), s.getCashBalance(), s.getInvestedValue()))
                    .toList();
        }

        // MONTHLY (or any other interval you add later)
        if (interval == Interval.MONTHLY) {
            List<PortfolioDailySnapshot> monthly = snapshots.stream()
                    .collect(Collectors.toMap(s -> YearMonth.from(s.getDate()), Function.identity(), BinaryOperator.maxBy(Comparator.comparing(PortfolioDailySnapshot::getDate))))
                    .values()
                    .stream()
                    .sorted(Comparator.comparing(PortfolioDailySnapshot::getDate))
                    .toList();

            return monthly.stream()
                    .map(s -> new PortfolioDailySnapshotDTO(
                            s.getDate(), s.getTotalValue(), s.getCashBalance(), s.getInvestedValue()))
                    .toList();
        }

        // Fallback: daily
        return snapshots.stream()
                .map(s -> new PortfolioDailySnapshotDTO(
                        s.getDate(), s.getTotalValue(), s.getCashBalance(), s.getInvestedValue()))
                .toList();
    }
}
