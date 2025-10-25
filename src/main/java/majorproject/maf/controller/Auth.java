package majorproject.maf.controller;


import majorproject.maf.model.ApiResponse;
import majorproject.maf.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import majorproject.maf.model.User;

@RestController
@RequestMapping("/auth")
public class Auth {

    @Autowired
    UserRepository userRepo;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signUp(@RequestBody User user){
        if (user.getEmail() == null || user.getEmail().isBlank() ||
                user.getPassword() == null || user.getPassword().isBlank()||user.getUsername()==null||user.getUsername().isBlank()){
            return new ResponseEntity<>(new ApiResponse<>(false,"SignUp Failed ","User data does contain username or email or password"), HttpStatus.BAD_REQUEST);
        }
        userRepo.save(user);
        return new ResponseEntity<>(new ApiResponse<>(true,"SignUp Successful",user), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody User user) {
        // Basic validation
        if (user.getEmail() == null || user.getEmail().isBlank() ||
                user.getPassword() == null || user.getPassword().isBlank()) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Login Failed", "Email or password missing"),
                    HttpStatus.BAD_REQUEST
            );
        }

        // Fetch user
        User dbUser = userRepo.findByEmail(user.getEmail());
        if (dbUser == null) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Login Failed", "User does not exist"),
                    HttpStatus.NOT_FOUND
            );
        }

        // Validate password (for demo only – no hashing)
        if (!user.getPassword().equals(dbUser.getPassword())) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Login Failed", "Invalid password"),
                    HttpStatus.UNAUTHORIZED
            );
        }

        // Success
        return new ResponseEntity<>(
                new ApiResponse<>(true, "Login Successful", dbUser),
                HttpStatus.OK
        );
    }
}
