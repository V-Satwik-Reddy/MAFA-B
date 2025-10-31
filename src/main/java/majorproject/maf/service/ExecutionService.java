package majorproject.maf.service;

import majorproject.maf.dto.BuyRequest;
import majorproject.maf.model.Coin;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
public class ExecutionService {

    private WebClient webClient;

    public ExecutionService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
    }

    public Coin buyCoin(BuyRequest request, String place) {
        return webClient.post()
                .uri("/"+place+"/buy")
                .body(Mono.just(request), BuyRequest.class)
                .retrieve()
                .bodyToMono(Coin.class)
                .block(); // block() makes it synchronous
    }
    public Coin sellCoin(BuyRequest request, String place) {
        return webClient.post()
                .uri("/"+place+"/sell")
                .body(Mono.just(request), BuyRequest.class)
                .retrieve()
                .bodyToMono(Coin.class)
                .block();
    }
}
