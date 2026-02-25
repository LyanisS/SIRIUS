package fr.episen.sirius.pcc.back.services.voyageur;

import fr.episen.sirius.pcc.back.models.voyageur.Utilisateur;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(this.secret.getBytes());
    }

    public String generateToken(Utilisateur utilisateur) {
        return Jwts.builder()
                .setSubject(utilisateur.getId().toString())
                .claim("email", utilisateur.getEmail())
                .claim("name", utilisateur.getNom())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + this.expirationMs))
                .signWith(this.getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Long extractUserId(Claims claims) {
        try {
            return Long.parseLong(claims.getSubject());
        } catch (NullPointerException | NumberFormatException e) {
            return null;
        }
    }

    public Long extractUserId(String token) {
        try {
            Claims claims = this.parseClaims(token);
            return this.extractUserId(claims);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractEmail(Claims claims) {
        try {
            return claims.get("email").toString();
        } catch (NullPointerException e) {
            return null;
        }
    }

    public String extractEmail(String token) {
        try {
            Claims claims = this.parseClaims(token);
            return this.extractEmail(claims);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractName(Claims claims) {
        try {
            return claims.get("name").toString();
        } catch (NullPointerException e) {
            return null;
        }
    }

    public String extractName(String token) {
        try {
            Claims claims = this.parseClaims(token);
            return this.extractName(claims);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);

            if (claims.getExpiration().before(new Date())) return false;
            if (this.extractUserId(claims) == null) return false;
            if (this.extractEmail(claims) == null) return false;
            if (this.extractName(claims) == null) return false;

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(this.getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
