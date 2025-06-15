package uz.navbatuz.backend.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;


// so waht is JWT? its like a proof ID card.
// Backend creates it, gives it to the frontend. The frontend stores it
// (in memory or localStorage) and uses it to make future requests.
// its like: “I am Fatkhullakh. Here’s my identity. Signed by the backend.”

@Service
public class JwtService {
    private static final String SECRET_KEY = "12345678901234567890123456789012"; // at least 32 characters
    private static final long EXPIRATION_TIME = 86400000; // 24 hours

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }


    //JWT contains:
    //sub (subject) = your email
    //iat (issued at)
    //exp (expiration)
    //And a signature to prevent tampering
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    /*
    {
      "sub": "fatkh@example.com",
      "iat": 1718279200,
      "exp": 1718365600
    }
     */

    // Parses the token and returns the email stored inside.
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject(); /// this is the user's email we stored in token
    }

    // Verifies if the token is correctly signed and not expired.
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
}
