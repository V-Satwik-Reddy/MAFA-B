package majorproject.maf.service;

import jakarta.servlet.http.HttpServletResponse;
import majorproject.maf.dto.request.EmailVerifyRequest;
import majorproject.maf.dto.request.LoginRequest;
import majorproject.maf.dto.request.SignUpRequest;
import majorproject.maf.exception.auth.*;
import majorproject.maf.dto.response.ApiResponse;
import majorproject.maf.model.User;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.model.UserOtp;
import majorproject.maf.repository.UserOtpRepository;
import majorproject.maf.repository.UserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CookieValue;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passEnc;
    private final JWTService jwt;
    private final EmailService emailService;
    private static final SecureRandom secureRandom = new SecureRandom();
    private final StringRedisTemplate simpleRedisCache;
//    private final UserOtpRepository userOtpRepository;

    public AuthService(UserRepository userRepo, PasswordEncoder passEnc, JWTService jwt, EmailService emailService, UserOtpRepository userOtpRepository, StringRedisTemplate simpleRedisCache) {
        this.emailService = emailService;
        this.userRepo = userRepo;
        this.passEnc = passEnc;
        this.jwt = jwt;
        this.simpleRedisCache = simpleRedisCache;
//        this.userOtpRepository = userOtpRepository;
    }

    public ApiResponse<?> signUp(SignUpRequest req) {
        if (userRepo.findByEmail(req.getEmail()) != null) {
            throw new UserAlreadyExistsException("User already registered with same Email");
        }
        if(!req.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,32}$"))
            throw new InvalidEmailOrPasswordFormatException("Invalid password. Password must be 8-32 characters long and include at least one uppercase letter, one lowercase letter, one digit, and one special character.");
        sendOtpEmail(req.getEmail());
        return ApiResponse.successMessage("User email verified successfully. Verify OTP sent to email");
    }

    public void sendOtpEmail(String email){
        String otp = generateOtp();
//        userOtpRepository.save(new UserOtp(email,otp, System.currentTimeMillis() + Duration.ofMinutes(5).toMillis()));
        simpleRedisCache.opsForValue().set("otp:email:" + email, otp, Duration.ofMinutes(5));
        emailService.sendOtpEmail(email,otp);
    }

    public ApiResponse<?> verifyEmail(EmailVerifyRequest e,HttpServletResponse resp) {
//        UserOtp userOtp = userOtpRepository.findByEmail(e.getEmail());
//        String savedOtp = userOtp.getOtp();
//        if(userOtp.getExpiresAt()< System.currentTimeMillis()){
//            userOtpRepository.delete(userOtp);
//            throw new OtpExpiredException("OTP has expired. Please request a new one.");
//        }
        String savedOtp = simpleRedisCache.opsForValue().get("otp:email:" + e.getEmail());
        if(savedOtp == null) {
            throw new OtpExpiredException("OTP has expired. Please request a new one.");
        }
        if(!savedOtp.equals(e.getOtp())) {
            throw new InvalidOtpException("Invalid OTP. Please try again.");
        }
        User newUser = new User(e.getEmail(), passEnc.encode(e.getPassword()));
        userRepo.save(newUser);
//        userOtpRepository.delete(userOtp);
        simpleRedisCache.delete("otp:email:" + e.getEmail());
        UserDto dto =new UserDto(newUser.getId(), newUser.getEmail(), newUser.getPhone(),newUser.getStatus());
        return ApiResponse.success( "Email verified and user registered successfully",getResponse(resp, dto));
    }

    public ApiResponse<?> login(LoginRequest req, HttpServletResponse response) {

        User user = userRepo.findByEmail(req.getEmail());
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        if (!passEnc.matches(req.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid Credentials");
        }

        UserDto dto =new UserDto(user.getId(),user.getEmail(),user.getPhone(),user.getStatus());
        return ApiResponse.success( "Login successful",getResponse(response, dto));
    }

    private Map<String, Object> getResponse(HttpServletResponse response, UserDto dbUser) {
        String accessToken = jwt.generateAccessToken(dbUser);
        String refreshToken = jwt.generateRefreshToken(dbUser);

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)          // false only for local HTTP testing
                .sameSite("None")
                .path("/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return Map.of(
                "accessToken", accessToken,
                "user", dbUser
        );
    }

    public ResponseEntity<?> refresh( @CookieValue(name = "refresh_token", required = false) String refreshToken) {

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!jwt.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UserDto user= jwt.extractUser(refreshToken);
        if(user==null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String newAccessToken = jwt.generateAccessToken(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "accessToken", newAccessToken,"user", user
        ));
    }

    public String generateOtp() {
        int otp = secureRandom.nextInt(1_000_000); // 0 to 999999
        return String.format("%06d", otp); // zero-pad to 6 digits
    }

}
