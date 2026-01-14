package majorproject.maf.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import majorproject.maf.dto.request.EmailVerifyRequest;
import majorproject.maf.dto.request.LoginRequest;
import majorproject.maf.dto.request.SignUpRequest;
import majorproject.maf.dto.response.ApiResponse;
import majorproject.maf.service.AuthService;
import majorproject.maf.service.UserCacheService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    AuthService auth;
    UserCacheService userCacheService;
    public AuthController(AuthService auth, UserCacheService userCacheService) {
        this.userCacheService = userCacheService;
        this.auth = auth;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signUp(@Valid @RequestBody SignUpRequest req) {
        ApiResponse<?> apiResponse = auth.signUp(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<?>> resendOtp(@Valid @RequestBody SignUpRequest req) {
        auth.sendOtpEmail(req.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.successMessage("OTP resent successfully"));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<?>> verifyEmail(@Valid @RequestBody EmailVerifyRequest req, HttpServletResponse resp) {
        ApiResponse<?> apiResponse = auth.verifyEmail(req,resp);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login( @Valid @RequestBody LoginRequest req, HttpServletResponse response) {
        ApiResponse<?> apiResponse = auth.login(req,response);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh( @CookieValue(name = "refresh_token", required = false) String refreshToken) {
        return auth.refresh(refreshToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response, Authentication authentication) {
        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)        // true in prod
                .sameSite("Lax")
                .path("/auth/refresh")
                .maxAge(0)
                .build();
        userCacheService.evictCachedUser(authentication.getName());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        return ResponseEntity.status(HttpStatus.OK).body("Logged out successfully");
    }

}
