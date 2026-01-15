package majorproject.maf.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.exception.auth.JwtAccessTokenExpiredException;
import majorproject.maf.exception.auth.JwtValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.sql.SQLOutput;
import java.util.Date;
import java.util.function.Function;

@Service
public class JWTService {

    @Value("${jwt.secret:}")
    private String base64Secret;
    private SecretKey key;

    private static final long ACCESS_TOKEN_EXPIRY = 60 * 60 * 1000; // 60 min
    private static final long REFRESH_TOKEN_EXPIRY = 7 * 24 * 60 * 60 * 1000; // 7 days

    @PostConstruct
    private void initKey() {
        if (base64Secret == null || base64Secret.isBlank()) {
            throw new IllegalStateException("Missing jwt.secret (Base64) in configuration");
        }
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UserDto user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("type", "ACCESS")
                .claim("userId", user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UserDto user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("type", "REFRESH")
                .claim("userId", user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    public UserDto extractUser(String token) {
        Claims claims = extractAllClaims(token);
        String email = claims.getSubject();
        Integer userId = claims.get("userId", Integer.class);
        return new UserDto(userId, email);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateAccessToken(String token) {
        if (isTokenExpired(token)) {
            throw new JwtAccessTokenExpiredException("Access Token Expired");
        }
        return true;
    }

    public boolean validateRefreshToken(String token) {
        if(isTokenExpired(token)) {
            throw new JwtValidationException("Refresh Token Expired Please Login Again");
        }
        return "REFRESH".equals(extractTokenType(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
