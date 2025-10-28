package majorproject.maf.service;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import majorproject.maf.model.User;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JWTService {

    private String key="";

    public JWTService() {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("HMACSHA256");

            SecretKey sKey = keygen.generateKey();
            key= Base64.getEncoder().encodeToString(sKey.getEncoded());
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String generateToken(User user) {
        Map<String,Object> claims=new HashMap<>();

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+ 60*60*10))
                .and()
                .signWith(getkey())
                .compact();
    }

    private Key getkey() {
        byte[] bytes= Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(bytes);
    }
}
