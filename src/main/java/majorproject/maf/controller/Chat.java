package majorproject.maf.controller;

import majorproject.maf.model.ChatRequest;
import majorproject.maf.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class Chat {
    ChatService chatService;
    public Chat(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody ChatRequest request, Authentication auth) {
        // Friendly, informative message while chat features are being implemented
        String responseMessage = "Chat module is currently under development. We're building intelligent chat features — please check back soon.";
        String basicresponse =chatService.generalChat(request.getQuery(),auth.getName());
        return ResponseEntity.ok(basicresponse);
    }


}
