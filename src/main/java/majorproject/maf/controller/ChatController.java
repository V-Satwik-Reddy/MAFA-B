package majorproject.maf.controller;

import majorproject.maf.dto.response.ChatDto;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ChatController {

    ChatService chatService;
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/general-chat")
    public ResponseEntity<String> generalChat(@RequestBody ChatDto request, Authentication auth) {
        UserDto user= (UserDto) auth.getPrincipal();
        String response =chatService.generalChat(request.getUserQuery(),user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mcp-chat")
    public ResponseEntity<String> mcpChat(@RequestBody ChatDto request, Authentication auth) {
        UserDto user= (UserDto) auth.getPrincipal();
        String response =chatService.mcpChat(request.getUserQuery(),user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/ea-chat")
    public ResponseEntity<String> eaChat(@RequestBody ChatDto request, Authentication auth) {
        UserDto user= (UserDto) auth.getPrincipal();
        String response =chatService.executeAgentChat(request.getUserQuery(),user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mra-chat")
    public ResponseEntity<String> mraChat(@RequestBody ChatDto request, Authentication auth) {
        UserDto user= (UserDto) auth.getPrincipal();
        String response =chatService.marketResearchAgentChat(request.getUserQuery(),user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pa-chat")
    public ResponseEntity<String> paChat(@RequestBody ChatDto request, Authentication auth) {
        UserDto user= (UserDto) auth.getPrincipal();
        String response =chatService.portfolioManagerAgentChat(request.getUserQuery(),user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/chats")
    public ResponseEntity<?> getChats(@RequestParam(required = false) Integer limit,@RequestParam(required = false) Integer page, Authentication auth) {
        UserDto user= (UserDto) auth.getPrincipal();
        List<ChatDto> res=chatService.getUserChats(user.getId(),limit,page);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }
}
