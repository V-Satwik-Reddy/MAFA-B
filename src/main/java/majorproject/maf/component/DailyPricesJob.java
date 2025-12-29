package majorproject.maf.component;

import majorproject.maf.service.PriceFetch;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyPricesJob {

    private final PriceFetch priceFetch;

    public DailyPricesJob(PriceFetch priceFetch) {
        this.priceFetch = priceFetch;
    }

    @Scheduled(cron = "0 40 22 * * *")
    public void updateDailyPrices() {
        System.out.println("Running Daily Prices Job...");
        priceFetch.addPreviousDayPrices();
    }
}
