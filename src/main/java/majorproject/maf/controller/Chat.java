package majorproject.maf.controller;

import majorproject.maf.model.ChatRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class Chat {

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody ChatRequest request) {
        // Friendly, informative message while chat features are being implemented
        String responseMessage = "Chat module is currently under development. We're building intelligent chat features — please check back soon.";
        return ResponseEntity.ok(responseMessage);
    }


}
