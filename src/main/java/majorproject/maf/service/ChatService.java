package majorproject.maf.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import majorproject.maf.dto.response.ChatDto;
import majorproject.maf.model.Chat;
import majorproject.maf.model.User;
import majorproject.maf.repository.ChatRepository;
import majorproject.maf.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final UserRepository userRepo;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ChatRepository chatRepo;

    public ChatService(UserRepository userRepo, ChatRepository chatRepo) {
        this.userRepo = userRepo;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.chatRepo = chatRepo;
    }

    public String generalChat(String query, String name) {
        return callAgentService(query,name,"general-agent");
    }

    public String executeAgentChat(String query, String email) {
        return callAgentService(query,email,"execute-agent");
    }

    public String marketResearchAgentChat(String query, String email) {
        return callAgentService(query,email,"market-research-agent");
    }

    public void saveChat(User u, String userQuery, String agentResponse) {
        Chat c=new Chat(u,userQuery,agentResponse);
        chatRepo.save(c);
    }

    public List<ChatDto> getUserChats(String email) {
        User u=userRepo.findByEmail(email);
        List<Chat> chats=chatRepo.findAllByUserIdOrderByCreatedAtAsc(u.getId());
        return chats.stream()
                .map(chat -> new ChatDto(chat.getUserQuery(), chat.getAgentResponse()))
                .collect(Collectors.toList());
    }

    public String callAgentService(String query, String email, String agentEndpoint) {
        try {
            User u=userRepo.findByEmail(email);
            int userId = u.getId();

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String token = (String) auth.getCredentials();

            Map<String, Object> body = Map.of(
                    "query", query,
                    "userId", userId
            );
            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:5000/"+agentEndpoint))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token) // ✅ forward JWT
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
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
            saveChat(u,query,data.toString());
            return data.toString();
        } catch (Exception e) {
            throw new RuntimeException("Chat service failed", e);
        }
    }
}
