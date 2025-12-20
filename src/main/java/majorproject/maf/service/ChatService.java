package majorproject.maf.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import majorproject.maf.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class ChatService {

    private final UserRepository userRepo;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Simple per-user memory (userId -> conversation)
    private final Map<Integer, List<String>> userMemory = new HashMap<>();

    @Value("${gemini.api.key}")
    private String GEMINI_API_KEY;

    private static final int MAX_HISTORY = 6; // last 6 messages only

    public ChatService(UserRepository userRepo) {
        this.userRepo = userRepo;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public String generalChat(String query, String email) {
        int userId = userRepo.findByEmail(email).getId();

        // Get or init memory
        List<String> history = userMemory.computeIfAbsent(userId, k -> new ArrayList<>());

        // Add user query
        history.add("User: " + query);

        // Trim memory
        if (history.size() > MAX_HISTORY) {
            history = history.subList(history.size() - MAX_HISTORY, history.size());
            userMemory.put(userId, history);
        }

        try {
            // Build prompt with memory
            StringBuilder prompt = new StringBuilder();
            prompt.append("You are a helpful, neutral assistant. ")
                    .append("Respond clearly and concisely.\n\n");

            for (String msg : history) {
                prompt.append(msg).append("\n");
            }

            // Build request body
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt.toString())
                                    )
                            )
                    )
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="
                                    + GEMINI_API_KEY))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Gemini API error: " + response.body());
            }

            // Parse response
            Map<?, ?> parsed = objectMapper.readValue(response.body(), Map.class);
            List<?> candidates = (List<?>) parsed.get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                return "I couldn’t generate a response right now.";
            }

            Map<?, ?> content = (Map<?, ?>) ((Map<?, ?>) candidates.get(0)).get("content");
            List<?> parts = (List<?>) content.get("parts");
            String answer = (String) ((Map<?, ?>) parts.get(0)).get("text");

            // Store assistant reply
            history.add("Assistant: " + answer);

            return answer;

        } catch (Exception e) {
            throw new RuntimeException("Chat service failed", e);
        }
    }
}
