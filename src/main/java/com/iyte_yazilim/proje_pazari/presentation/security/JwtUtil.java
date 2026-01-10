package com.iyte_yazilim.proje_pazari.presentation.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for JWT token operations.
 *
 * <p>Provides methods for:
 *
 * <ul>
 *   <li>Token generation
 *   <li>Token validation
 *   <li>Claim extraction
 * </ul>
 *
 * <h2>Configuration:</h2>
 *
 * <p>Requires the following properties in application.properties:
 *
 * <ul>
 *   <li>{@code jwt.secret} - Secret key for signing (min 256 bits)
 *   <li>{@code jwt.expiration} - Token expiration time in milliseconds
 * </ul>
 *
 * <h2>Security Notes:</h2>
 *
 * <p>Tokens are signed using HMAC-SHA256 algorithm.
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see JwtAuthenticationFilter
 */
@Component
public class JwtUtil {

    /** Secret key for JWT signing. Must be at least 256 bits. */
    @Value("${jwt.secret}")
    private String secret;

    /** Token expiration time in milliseconds. */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Creates the signing key from the secret.
     *
     * @return HMAC-SHA key for signing/verification
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Extracts the username (subject) from a JWT token.
     *
     * @param token the JWT token
     * @return the username stored in the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from a JWT token.
     *
     * @param <T> the type of the claim value
     * @param token the JWT token
     * @param claimsResolver function to extract the desired claim
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from a JWT token.
     *
     * @param token the JWT token
     * @return all claims contained in the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Checks if a token has expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Generates a new JWT token for a user.
     *
     * @param username the username to include as subject
     * @return signed JWT token string
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    /**
     * Creates a signed JWT token with claims and subject.
     *
     * @param claims additional claims to include
     * @param subject the token subject (username)
     * @return signed JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validates a JWT token against a username.
     *
     * @param token the JWT token
     * @param username the expected username
     * @return true if token is valid and not expired, false otherwise
     */
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
}
