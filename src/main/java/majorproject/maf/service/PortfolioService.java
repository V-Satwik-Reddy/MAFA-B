package majorproject.maf.service;

import majorproject.maf.dto.response.*;
import majorproject.maf.model.PortfolioDailySnapshot;
import majorproject.maf.model.Transaction;
import majorproject.maf.model.enums.Interval;
import majorproject.maf.model.enums.Period;
import majorproject.maf.model.enums.TransactionType;
import majorproject.maf.model.serving.CompanyMaster;
import majorproject.maf.model.user.User;
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
        List<Double> prices= stockPriceRepository.batchFind(
                s.stream().map(Share::getSymbol).toList()
        );
        for(int i=0;i<s.size();i++){
            s.get(i).setPrice(prices.get(i));
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

    public boolean withdrawBalance(int id, double amount) {
        Double balance = userProfileRepository.findByUserId(id).getBalance();
        if (balance < amount) return false;
        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setAmount(amount);
        transaction.setUser(userRepository.getReferenceById(id));
        transaction.setAsset("CASH");
        transaction.setAssetQuantity(0L);
        dashboardService.createTransaction(transaction);
        userProfileRepository.debitBalance(id, amount);
        return true;
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

    public int addToWatchlist(int id, String symbol) {
        CompanyMaster company= companyMasterRepository.findBySymbol(symbol);
        Watchlist w=watchlistRepository.findBySymbolAndUserId(symbol, id);
        if(w!=null) return 0;
        w=new Watchlist();
        w.setCompany(company);
        w.setUser(userRepository.getReferenceById(id));
        watchlistRepository.save(w);
        return 1;
    }

    public boolean removeFromWatchlist(int id, String symbol) {
        try{
            int i=watchlistRepository.deleteByUserIdAndCompanySymbol(id, symbol);
            if(i==0) return false;
        }catch (Exception e){
            return false;
        }
        return true;
    }

    public void createEODPortfolioSnapshot() {
        List<User> users = userRepository.findAll();
        for(User user:users){
            Double cashBalance = getBalance(user.getId());
            PortfolioDailySnapshot portfolioDailySnapshot = new PortfolioDailySnapshot();
            List<Share> investedShares = getUserHoldings(user.getId());
            double investedValue = 0.0;
            for(Share share: investedShares) {
                investedValue += share.getQuantity() * share.getPrice();
            }
            portfolioDailySnapshot.setUser(user);
            portfolioDailySnapshot.setDate(java.time.LocalDate.now());
            portfolioDailySnapshot.setInvestedValue(investedValue);
            portfolioDailySnapshot.setCashBalance(cashBalance);
            portfolioDailySnapshot.setTotalValue(investedValue + cashBalance);
            portfolioDailySnapshotRepository.save(portfolioDailySnapshot);
        }
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
