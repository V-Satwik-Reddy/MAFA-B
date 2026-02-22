package majorproject.maf.service;

import majorproject.maf.dto.response.CompanyDto;
import majorproject.maf.dto.response.SectorDto;
import majorproject.maf.dto.response.Share;
import majorproject.maf.dto.response.WatchlistDto;
import majorproject.maf.model.Transaction;
import majorproject.maf.model.enums.TransactionType;
import majorproject.maf.model.serving.CompanyMaster;
import majorproject.maf.model.user.UserProfile;
import majorproject.maf.model.user.Watchlist;
import majorproject.maf.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PortfolioService {

    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final UserProfileRepository userProfileRepository;
    private final WatchlistRepository watchlistRepository;
    private final CompanyMasterRepository companyMasterRepository;
    private final UserRepository userRepository;
    private final DashboardService dashboardService;

    public PortfolioService(StockRepository stockRepository, StockPriceRepository stockPriceRepository, UserProfileRepository userProfileRepository, WatchlistRepository watchlistRepository, CompanyMasterRepository companyMasterRepository, UserRepository userRepository, DashboardService dashboardService) {
        this.stockPriceRepository = stockPriceRepository;
        this.stockRepository = stockRepository;
        this.userProfileRepository = userProfileRepository;
        this.watchlistRepository = watchlistRepository;
        this.companyMasterRepository = companyMasterRepository;
        this.userRepository = userRepository;
        this.dashboardService = dashboardService;
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
}
