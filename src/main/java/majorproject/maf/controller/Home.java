package majorproject.maf.controller;

import majorproject.maf.model.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Home {

    @GetMapping
    public ResponseEntity<ApiResponse<String>> home() {
        ApiResponse<String> response = new ApiResponse<>(true,"Welcome",new String("Welcome to the Multi Agent Financial Assitant "));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
