package majorproject.maf.exception;

import jakarta.servlet.http.HttpServletRequest;
import majorproject.maf.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import majorproject.maf.exception.auth.*;
import org.springframework.web.bind.annotation.RequestBody;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleUserExists() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("User already registered with same Email"));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleUserNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("User Does Not Exist"));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid Credentials"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleOtherExceptions(Exception ex) {
        System.out.println("An unexpected error occurred." +ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Internal Server Error"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleException(RuntimeException ex, HttpServletRequest request, @RequestBody(required = false) Object body) {
        System.out.println(request.getRequestURI()+" "+request.getContextPath()+" "+body.toString());
        System.out.println("An unexpected error occurred." +ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Internal Server Error"));
    }
    @ExceptionHandler(JwtValidationException.class)
    public ResponseEntity<?> handleJwtValidationException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid Token"));
    }

    @ExceptionHandler(JwtAccessTokenExpiredException.class)
    public ResponseEntity<?> handleJwtAccessTokenExpiredException() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access Token Expired"));
    }

    @ExceptionHandler(JwtRefreshTokenExpiredException.class)
    public ResponseEntity<?> handleJwtRefreshTokenExpiredException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Refresh Token Expired Please Login Again"));
    }
}
