package majorproject.maf.exception.auth;

public class JwtAccessTokenExpiredException extends RuntimeException {
    public JwtAccessTokenExpiredException(String message) {
        super(message);
    }
}
