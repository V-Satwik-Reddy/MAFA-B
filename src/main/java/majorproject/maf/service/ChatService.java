package majorproject.maf.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import majorproject.maf.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class ChatService {

    private final UserRepository userRepo;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ChatService(UserRepository userRepo) {
        this.userRepo = userRepo;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public String generalChat(String query, String email) {
        try {
            // Resolve user
            int userId = userRepo.findByEmail(email).getId();

            // 🔐 Get JWT from SecurityContext
            Authentication auth = SecurityContextHolder
                    .getContext()
                    .getAuthentication();

            String token = (String) auth.getCredentials();

            // Build request body
            Map<String, Object> body = Map.of(
                    "query", query,
                    "userId", userId
            );

            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:5000/market-research-agent"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token) // ✅ forward JWT
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
//            System.out.println(request);
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Agent service error: " + response.statusCode() + " " + response.body()
                );
            }
            Map<?, ?> parsed = objectMapper.readValue(response.body(), Map.class);

            Object data = parsed.get("data");
            if (data == null) {
                throw new RuntimeException("Agent response missing 'data' field");
            }
            return data.toString();


        } catch (Exception e) {
            throw new RuntimeException("Chat service failed", e);
        }
    }
}
