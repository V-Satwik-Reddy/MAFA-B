package majorproject.maf.controller;

import majorproject.maf.model.ApiResponse;
import majorproject.maf.model.UserDto;
import majorproject.maf.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import majorproject.maf.model.User;

@RestController
@RequestMapping("/auth")
public class Auth {

    @Autowired
    AuthService auth;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signUp(@RequestBody User user){
        if (user.getEmail() == null || user.getEmail().isBlank() ||
                user.getPassword() == null || user.getPassword().isBlank()||user.getUsername()==null||user.getUsername().isBlank()){
            return new ResponseEntity<>(

                    new ApiResponse<>(false,"SignUp Failed ","User data does contain username or email or password"),
                    HttpStatus.BAD_REQUEST
            );
        }

        UserDto udto= auth.signUp(user);
        if(udto==null){
            return new ResponseEntity<>(new ApiResponse<>(false,"SignUp Failed","User already exists"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new ApiResponse<>(true,"SignUp Successful",udto), HttpStatus.OK);
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

        UserDto udto= auth.login(user);

        //No user
        if(udto==null){
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Login Failed", "User does not exist"),
                    HttpStatus.NOT_FOUND
            );
        }
        //Wrong Password
        if(udto.getUsername()==null){
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Login Failed", "Invalid password"),
                    HttpStatus.UNAUTHORIZED
            );

        }
        // Success
        return new ResponseEntity<>(
                new ApiResponse<>(true, "Login Successful", udto),
                HttpStatus.OK
        );
    }
}
