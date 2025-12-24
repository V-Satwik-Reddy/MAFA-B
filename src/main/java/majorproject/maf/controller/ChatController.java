package majorproject.maf.controller;

import majorproject.maf.dto.ChatDto;
import majorproject.maf.model.ChatRequest;
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
    public ResponseEntity<String> eaChat(@RequestBody ChatRequest request, Authentication auth) {
        String basicresponse =chatService.executeAgentChat(request.getQuery(),auth.getName());
        return ResponseEntity.ok(basicresponse);
    }

    @PostMapping("/mra-chat")
    public ResponseEntity<String> mraChat(@RequestBody ChatRequest request, Authentication auth) {
        String basicresponse =chatService.marketResearchAgentChat(request.getQuery(),auth.getName());
        return ResponseEntity.ok(basicresponse);
    }

    @GetMapping("/chats")
    public ResponseEntity<?> getChats(Authentication auth) {
        List<ChatDto> res=chatService.getUserChats(auth.getName());
        return ResponseEntity.ok(res);
    }
}
