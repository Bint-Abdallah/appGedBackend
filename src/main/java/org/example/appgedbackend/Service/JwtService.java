package org.example.appgedbackend.Service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long EXPIRATION_MS;

    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) throws ExpiredJwtException, MalformedJwtException, SignatureException {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) throws ExpiredJwtException, MalformedJwtException, SignatureException {
        return (String) extractAllClaims(token).get("role");
    }

    public boolean isTokenValid(String token, String username) {
        try {
            String tokenUsername = extractUsername(token);
            return (tokenUsername.equals(username) && !isTokenExpired(token));
        } catch (ExpiredJwtException e) {
            // Token expiré
            return false;
        } catch (Exception e) {
            // Autres erreurs (signature invalide, token malformé, etc.)
            return false;
        }
    }

    private boolean isTokenExpired(String token) throws ExpiredJwtException, MalformedJwtException, SignatureException {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) throws ExpiredJwtException, MalformedJwtException, SignatureException {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Méthode utilitaire pour vérifier si un token est expiré (sans lever d'exception)
    public boolean isTokenExpiredSafe(String token) {
        try {
            return isTokenExpired(token);
        } catch (Exception e) {
            return true; // Si erreur, considérer comme expiré/invalide
        }
    }

    // Méthode pour obtenir la date d'expiration
    public Date getExpirationDate(String token) {
        try {
            return extractAllClaims(token).getExpiration();
        } catch (Exception e) {
            return null;
        }
    }
}