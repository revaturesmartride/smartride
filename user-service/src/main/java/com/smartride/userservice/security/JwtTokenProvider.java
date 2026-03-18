package com.smartride.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {
    private final String SECRET_KEY="smartride-secret-key";
    private final long EXPIRATION_TIME = 86400000;
    public String generateToken(String userEmail, String role) {

        return Jwts.builder()
                .setSubject(userEmail)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }
    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }

    }
    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }


}
