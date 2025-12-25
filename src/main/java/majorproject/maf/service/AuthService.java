package majorproject.maf.service;

import jakarta.servlet.http.HttpServletResponse;
import majorproject.maf.dto.request.LoginRequest;
import majorproject.maf.dto.request.SignUpRequest;
import majorproject.maf.exception.auth.InvalidCredentialsException;
import majorproject.maf.exception.auth.UserAlreadyExistsException;
import majorproject.maf.exception.auth.UserNotFoundException;
import majorproject.maf.dto.response.ApiResponse;
import majorproject.maf.model.User;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CookieValue;

import java.time.Duration;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passEnc;
    private final JWTService jwt;
    public AuthService(UserRepository userRepo, PasswordEncoder passEnc, JWTService jwt) {
        this.userRepo = userRepo;
        this.passEnc = passEnc;
        this.jwt = jwt;
    }

    public ApiResponse signUp(SignUpRequest req, HttpServletResponse response) {
        if (userRepo.findByEmail(req.getEmail()) != null) {
            throw new UserAlreadyExistsException("User already registered");
        }
        User user = new User(req.getEmail(), req.getUsername(), passEnc.encode(req.getPassword()), req.getPhone(), req.getBalance());
        try{
            userRepo.save(user);
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Error occurred while registering user");
        }
        return getResponse(response,user);
    }

    public ApiResponse<?> login(LoginRequest req, HttpServletResponse response) {

        User dbUser = userRepo.findByEmail(req.getEmail());
        if (dbUser == null) {
            throw new UserNotFoundException("User not found");
        }

        if (!passEnc.matches(req.getPassword(), dbUser.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        return getResponse(response, dbUser);
    }

    private ApiResponse<Map<String, Object>> getResponse(HttpServletResponse response, User dbUser) {
        String accessToken = jwt.generateAccessToken(dbUser);
        String refreshToken = jwt.generateRefreshToken(dbUser);

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)          // false only for local HTTP testing
                .sameSite("Strict")
                .path("/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        UserDto dto = new UserDto(
                dbUser.getUsername(),
                dbUser.getEmail(),
                dbUser.getPhone(),
                dbUser.getBalance()
        );

        return new ApiResponse<>(true, "Login successful", Map.of(
                "accessToken", accessToken,
                "user", dto
        ));
    }

    public ResponseEntity<?> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!jwt.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String email = jwt.extractUserName(refreshToken);
        User user = userRepo.findByEmail(email);

        String newAccessToken = jwt.generateAccessToken(user);

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken
        ));
    }

}
