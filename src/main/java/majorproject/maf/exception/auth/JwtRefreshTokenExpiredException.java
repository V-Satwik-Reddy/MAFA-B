package majorproject.maf.exception.auth;

public class JwtRefreshTokenExpiredException extends RuntimeException {
    public JwtRefreshTokenExpiredException(String message) {
        super(message);
    }
}
