package majorproject.maf.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import majorproject.maf.dto.request.LoginRequest;
import majorproject.maf.dto.request.SignUpRequest;
import majorproject.maf.dto.response.ApiResponse;
import majorproject.maf.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    AuthService auth;
    public AuthController(AuthService auth) {
        this.auth = auth;
    }
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signUp(@Valid @RequestBody SignUpRequest req,
                                                 HttpServletResponse response) {
//        System.out.println(req);
        ApiResponse<?> apiResponse = auth.signUp(req,response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest req,
            HttpServletResponse response) {

        return ResponseEntity.ok(auth.login(req, response));
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verify() {
        return ResponseEntity.ok("User is verified");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {

        return auth.refresh(refreshToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {

        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)        // true in prod
                .sameSite("Lax")
                .path("/auth/refresh")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        return ResponseEntity.ok().build();
    }

}
