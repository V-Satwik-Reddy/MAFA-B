package majorproject.maf.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import majorproject.maf.dto.request.EmailVerifyRequest;
import majorproject.maf.dto.request.LoginRequest;
import majorproject.maf.dto.request.SignUpRequest;
import majorproject.maf.dto.response.ApiResponse;
import majorproject.maf.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    AuthService auth;
    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@Valid @RequestBody SignUpRequest req) {
        ApiResponse<Void> apiResponse = auth.signUp(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@Valid @RequestBody SignUpRequest req) {
        auth.sendOtpEmail(req.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.successMessage("OTP resent successfully"));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Map<String,Object>>> verifyEmail(@Valid @RequestBody EmailVerifyRequest req, HttpServletResponse resp) {
        ApiResponse<Map<String,Object>> apiResponse = auth.verifyEmail(req,resp);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String,Object>>> login( @Valid @RequestBody LoginRequest req, HttpServletResponse response) {
        ApiResponse<Map<String,Object>> apiResponse = auth.login(req,response);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String,Object>>> refresh( @CookieValue(name = "refresh_token", required = false) String refreshToken) {
        ApiResponse<Map<String,Object>> apiResponse = auth.refresh(refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)        // true in prod
                .sameSite("Lax")
                .path("/auth/refresh")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.successMessage("Logged out successfully"));
    }

}
