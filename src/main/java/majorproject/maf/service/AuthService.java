package majorproject.maf.service;

import majorproject.maf.dto.LoginRequest;
import majorproject.maf.dto.SignUpRequest;
import majorproject.maf.exception.auth.InvalidCredentialsException;
import majorproject.maf.exception.auth.UserAlreadyExistsException;
import majorproject.maf.exception.auth.UserNotFoundException;
import majorproject.maf.model.ApiResponse;
import majorproject.maf.model.User;
import majorproject.maf.dto.UserDto;
import majorproject.maf.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public ApiResponse signUp(SignUpRequest req) {
        if (userRepo.findByEmail(req.getEmail()) != null) {
            throw new UserAlreadyExistsException("User already registered");
        }
        User user = new User(req.getEmail(), req.getUsername(), passEnc.encode(req.getPassword()), req.getPhone(), req.getBalance());
        userRepo.save(user);
        String token=jwt.generateToken(user);
        UserDto dto=new UserDto(user.getUsername(), user.getEmail(),user.getPhone(), user.getBalance());
        return new ApiResponse(token,true, "User registered successfully", dto);
    }

    public ApiResponse login(LoginRequest req) {
        User dbUser = userRepo.findByEmail(req.getEmail());
        if (dbUser == null) {
            throw new UserNotFoundException("User not found with given email");
        }

        boolean isCorrect = passEnc.matches(req.getPassword(), dbUser.getPassword());
        if (!isCorrect) {
            throw new InvalidCredentialsException("Invalid password");
        }
        String token=jwt.generateToken(dbUser);
        UserDto dto=new UserDto(dbUser.getUsername(), dbUser.getEmail(),dbUser.getPhone(),dbUser.getBalance());
        return new ApiResponse(token,true, "User logged in successfully", dto);
    }
}
