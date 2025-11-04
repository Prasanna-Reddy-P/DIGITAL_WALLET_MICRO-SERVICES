package com.example.user_service_micro.config; // (or user_service_micro.config)

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key;
    private final long expiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expiration;
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}

/*
Keys.hmacShaKeyFor(...) is a utility method provided by the JJWT (Java JWT) library for creating SecretKey
instances specifically for use with HMAC-SHA algorithms when signing and verifying JSON Web Tokens (JWTs).

Purpose:
This method simplifies the process of generating a SecretKey suitable for HMAC-based signing algorithms
(like HS256, HS384, HS512) from a raw byte array representing the secret key material.
It ensures that the generated SecretKey is correctly formatted and compatible with the cryptographic operations required for JWTs.

How it works:
Input: It takes a byte[] array as its argument. This byte array represents the secret key material.
This secret should be a secure, randomly generated sequence of bytes,
and its length should meet the minimum requirements for the chosen HMAC-SHA algorithm (e.g., at least 256 bits for HS256).

Key Generation: It uses the provided byte array to construct a SecretKey object.
This SecretKey is specifically designed for use with HMAC-SHA algorithms.

Output: It returns a SecretKey instance, which can then be used with the signWith() method of JwtBuilder to sign a JWT
or with the setSigningKey() method of JwtParserBuilder to verify a JWT.
 */