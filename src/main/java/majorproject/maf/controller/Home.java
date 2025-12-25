package majorproject.maf.controller;

import majorproject.maf.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Home {

    @GetMapping
    public ResponseEntity<ApiResponse<String>> home() {
        ApiResponse<String> response = new ApiResponse<>(true,"Welcome", "Welcome to the Multi Agent Financial Assistant ");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
