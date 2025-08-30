package uz.navbatuz.backend.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import uz.navbatuz.backend.user.model.User;


import java.security.Key;
import java.util.Date;


// so waht is JWT? its like a proof ID card.
// Backend creates it, gives it to the frontend. The frontend stores it
// (in memory or localStorage) and uses it to make future requests.
// its like: “I am Fatkhullakh. Here’s my identity. Signed by the backend.”

@Service
public class JwtService {
    private static final String SECRET = "12345678901234567890123456789012";
    private static final long EXPIRATION = 1000L * 60 * 60 * 24;

    private Key getKey() { return Keys.hmacShaKeyFor(SECRET.getBytes()); }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())               // sub = UUID
                .claim("email", user.getEmail())                   // email as claim
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractSubject(String token) {
        return Jwts.parserBuilder().setSigningKey(getKey()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder().setSigningKey(getKey()).build()
                .parseClaimsJws(token).getBody().get("email", String.class);
    }

    public String extractRole(String token) {
        return Jwts.parserBuilder().setSigningKey(getKey()).build()
                .parseClaimsJws(token).getBody().get("role", String.class);
    }

    public boolean isExpired(String token) {
        return Jwts.parserBuilder().setSigningKey(getKey()).build()
                .parseClaimsJws(token).getBody().getExpiration().before(new Date());
    }
}

// Verifies if the token is correctly signed and not expired.
//     public boolean isTokenValid(String token) {
//         try {
//             Jwts.parserBuilder()
//                     .setSigningKey(getSignInKey())
//                     .build()
//                     .parseClaimsJws(token);
//             return true;
//         }
//         catch (Exception e) {
//             return false;
//         }
//     }
//@Service
//public class JwtService {
//    private static final String SECRET_KEY = "12345678901234567890123456789012"; // at least 32 characters
//    private static final long EXPIRATION_TIME = 86400000; // 24 hours
//
//    private Key getSignInKey() {
//        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
//    }
//
//
//    //JWT contains:
//    //sub (subject) = your email
//    //iat (issued at)
//    //exp (expiration)
//    //And a signature to prevent tampering
//    public String generateToken(String email) {
//        return Jwts.builder()
//                .setSubject(email)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
//                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    public String extractUsername(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(getSignInKey())
//                .build()
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject(); /// this is the user's email we stored in token
//    }
//
//    public boolean isTokenValid(String token) {
//        try {
//            Jwts.parserBuilder()
//                    .setSigningKey(getSignInKey())
//                    .build()
//                    .parseClaimsJws(token);
//            return true;
//        }
//        catch (Exception e) {
//            return false;
//        }
//    }
//
//}