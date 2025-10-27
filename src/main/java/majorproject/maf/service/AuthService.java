package majorproject.maf.service;

import majorproject.maf.dto.LoginRequest;
import majorproject.maf.dto.SignUpRequest;
import majorproject.maf.exception.auth.InvalidCredentialsException;
import majorproject.maf.exception.auth.UserAlreadyExistsException;
import majorproject.maf.exception.auth.UserNotFoundException;
import majorproject.maf.model.User;
import majorproject.maf.dto.UserDto;
import majorproject.maf.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passEnc;

    public AuthService(UserRepository userRepo, PasswordEncoder passEnc) {
        this.userRepo = userRepo;
        this.passEnc = passEnc;
    }

    public UserDto signUp(SignUpRequest req) {
        if (userRepo.findByEmail(req.getEmail()) != null) {
            throw new UserAlreadyExistsException("User already registered");
        }
        User user = new User();
        user.setEmail(req.getEmail());
        user.setUsername(req.getUsername());
        user.setPassword(passEnc.encode(req.getPassword()));
        userRepo.save(user);
        System.out.println(user);
        return new UserDto(user.getUsername(), user.getEmail());
    }

    public UserDto login(LoginRequest req) {
        User dbUser = userRepo.findByEmail(req.getEmail());
        if (dbUser == null) {
            throw new UserNotFoundException("User not found with given email");
        }

        boolean isCorrect = passEnc.matches(req.getPassword(), dbUser.getPassword());
        if (!isCorrect) {
            throw new InvalidCredentialsException("Invalid password");
        }

        return new UserDto(dbUser.getUsername(), dbUser.getEmail());
    }
}
