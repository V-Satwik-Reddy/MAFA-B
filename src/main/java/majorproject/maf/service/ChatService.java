package majorproject.maf.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import majorproject.maf.dto.response.ChatDto;
import majorproject.maf.model.Chat;
import majorproject.maf.model.user.User;
import majorproject.maf.repository.ChatRepository;
import majorproject.maf.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
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

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ChatRepository chatRepo;
    private final UserRepository userRepo;

    @Value("${agents_endpoint:}")
    String url;

    public ChatService(ChatRepository chatRepo, UserRepository userRepo) {
        this.userRepo = userRepo;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.chatRepo = chatRepo;
    }

    public String generalChat(String query, int id) {
        return callAgentService(query,id,"general-agent");
    }

    public String executeAgentChat(String query, int id) {
        return callAgentService(query,id,"execute-agent");
    }

    public String marketResearchAgentChat(String query, int id) {
        return callAgentService(query,id,"market-research-agent");
    }

    public String portfolioManagerAgentChat(String userQuery, int id) {
        return callAgentService(userQuery,id,"portfolio-manager-agent");
    }

    public void saveChat(User u, String userQuery, String agentResponse) {
        Chat c=new Chat(u,userQuery,agentResponse);
        chatRepo.save(c);
    }

    public List<ChatDto> getUserChats(int id) {
        List<Chat> chats=chatRepo.findAllByUserIdOrderByCreatedAtAsc(id);
        return chats.stream()
                .map(chat -> new ChatDto(chat.getUserQuery(), chat.getAgentResponse()))
                .collect(Collectors.toList());
    }

    public String callAgentService(String query, int userId, String agentEndpoint) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String token = (String) auth.getCredentials();

            Map<String, Object> body = Map.of(
                    "query", query,
                    "userId", userId
            );
            String jsonBody = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url +agentEndpoint))
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
            User user = userRepo.getReferenceById(userId);
            saveChat(user,query,data.toString());
            return data.toString();
        } catch (Exception e) {
            throw new RuntimeException("Chat service failed", e);
        }
    }

}
