package majorproject.maf.controller;

import majorproject.maf.dto.response.ChatDto;
import majorproject.maf.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChatController {

    ChatService chatService;
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/ea-chat")
    public ResponseEntity<String> eaChat(@RequestBody ChatDto request, Authentication auth) {
        String response =chatService.executeAgentChat(request.getUserQuery(),auth.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mra-chat")
    public ResponseEntity<String> mraChat(@RequestBody ChatDto request, Authentication auth) {
        String response =chatService.marketResearchAgentChat(request.getUserQuery(),auth.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/chats")
    public ResponseEntity<?> getChats(Authentication auth) {
        List<ChatDto> res=chatService.getUserChats(auth.getName());
        return ResponseEntity.ok(res);
    }
}
