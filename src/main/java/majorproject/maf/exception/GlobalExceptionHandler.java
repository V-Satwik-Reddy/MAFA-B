package majorproject.maf.exception;

import jakarta.servlet.http.HttpServletRequest;
import majorproject.maf.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import majorproject.maf.exception.auth.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger=LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserExists() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("User already registered with same Email"));
    }

    @ExceptionHandler(InvalidEmailOrPasswordFormatException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidEmailOrPassword() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Invalid Email or Password Format"));
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidOtp() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Invalid OTP"));
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleOtpExpired() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("OTP Expired"));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("User Does Not Exist"));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid Credentials"));
    }

    @ExceptionHandler(JwtValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtValidationException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid Token"));
    }

    @ExceptionHandler(JwtAccessTokenExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtAccessTokenExpiredException() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access Token Expired"));
    }

    @ExceptionHandler(JwtRefreshTokenExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtRefreshTokenExpiredException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Refresh Token Expired Please Login Again"));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientBalanceException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Insufficient balance to execute the buy order."));
    }

    @ExceptionHandler(InsufficientSharesException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientSharesException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Insufficient shares to execute the sell order."));
    }

    @ExceptionHandler(ResourseNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourseNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOtherExceptions(Exception ex) {
        logger.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Internal Server Error"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleException(RuntimeException ex, HttpServletRequest request) {
        logger.error("Unhandled RuntimeException at {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Internal Server Error"));
    }

}
