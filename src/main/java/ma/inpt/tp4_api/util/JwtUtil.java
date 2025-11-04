package ma.inpt.tp4_api.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(secret);
    }

    /**
     * Generate JWT token for a user
     */
    public String generateToken(String username) {
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(expiration, ChronoUnit.MILLIS))
                .withIssuer("tp4-api")
                .sign(getAlgorithm());
    }

    /**
     * Validate and decode JWT token
     */
    public DecodedJWT validateToken(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(getAlgorithm())
                .withIssuer("tp4-api")
                .build();
        return verifier.verify(token);
    }

    /**
     * Extract username from JWT token
     */
    public String getUsernameFromToken(String token) {
        try {
            return validateToken(token).getSubject();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    /**
     * Check if token is valid
     */
    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }
}
