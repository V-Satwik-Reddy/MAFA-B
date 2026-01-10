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
    private final UserCacheService userCacheService;

    public AuthService(UserRepository userRepo, PasswordEncoder passEnc, JWTService jwt, UserCacheService userCacheService) {
        this.userRepo = userRepo;
        this.passEnc = passEnc;
        this.jwt = jwt;
        this.userCacheService = userCacheService;
    }

    public ApiResponse<?> signUp(SignUpRequest req, HttpServletResponse response) {
        if (userRepo.findByEmail(req.getEmail()) != null) {
            throw new UserAlreadyExistsException("User already registered with same Email");
        }
        User user = new User(req.getEmail(), req.getUsername(), passEnc.encode(req.getPassword()), req.getPhone(), req.getBalance());
        try{
            userRepo.save(user);
        }catch(Exception e){
            throw new RuntimeException("Error occurred while registering user",e);
        }
        UserDto dto =new UserDto(user.getUsername(),user.getEmail(),user.getPhone(),user.getBalance(),user.getId());
        userCacheService.cacheUser(dto);
        return getResponse(response,dto);
    }

    public ApiResponse<?> login(LoginRequest req, HttpServletResponse response) {

        User user = userRepo.findByEmail(req.getEmail());
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        if (!passEnc.matches(req.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid Credentials");
        }

        UserDto dto =new UserDto(user.getUsername(),user.getEmail(),user.getPhone(),user.getBalance(),user.getId());
        userCacheService.cacheUser(dto);
        return getResponse(response, dto);
    }

    private ApiResponse<Map<String, Object>> getResponse(HttpServletResponse response, UserDto dbUser) {
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

        return ApiResponse.success( "Login successful", Map.of(
                "accessToken", accessToken,
                "user", dbUser
        ));
    }

    public ResponseEntity<?> refresh( @CookieValue(name = "refresh_token", required = false) String refreshToken) {

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!jwt.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String email = jwt.extractUserName(refreshToken);

        UserDto user = userCacheService.getCachedUser(email);
        if(user==null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String newAccessToken = jwt.generateAccessToken(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "accessToken", newAccessToken
        ));
    }

}
