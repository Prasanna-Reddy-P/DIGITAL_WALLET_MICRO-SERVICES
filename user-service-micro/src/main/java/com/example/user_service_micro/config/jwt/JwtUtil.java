    package com.example.user_service_micro.config.jwt; // (or user_service_micro.config)

    import io.jsonwebtoken.*;
    import io.jsonwebtoken.Claims;
    import io.jsonwebtoken.Jwts;
    import io.jsonwebtoken.JwtException;
    import io.jsonwebtoken.SignatureAlgorithm;
    import io.jsonwebtoken.security.Keys;
    import java.security.Key;

    import io.jsonwebtoken.security.Keys;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Component;

    import java.security.Key;
    import java.util.Date;

    // JwtUtil class is mainly constructed to generate JwtToken and to perform validation on it.
    @Component
    public class JwtUtil {

        private final Key key;
        /*
        Field holding the cryptographic key used to sign and verify tokens.
        Key is an interface, it comes from java.security.Key, represents a cryptographic key.
         */
        private final long expiration;

        public JwtUtil(
                @Value("${jwt.secret}") String secret, // @Value tells Spring to inject a value from the application properties file.
                @Value("${jwt.expiration}") long expiration
        ) {
            this.key = Keys.hmacShaKeyFor(secret.getBytes());
            this.expiration = expiration;
        }

        /*
        secret.getBytes()
        → Converts that text into a sequence of bytes (since cryptographic functions work with bytes, not plain text).

Keys.hmacShaKeyFor(...)
→ Comes from the io.jsonwebtoken.security.Keys class (part of the JJWT library).
→ It creates a secure HMAC-SHA key that the JWT library can use for signing tokens.

Example:

When you generate a JWT: it uses this key to sign the token (HS256 algorithm).
When validating a JWT: it uses the same key to verify that the token wasn’t tampered with.

this.key = ...
→ Stores the generated key into your class variable key,
so you can reuse it in both generateToken() and validateToken() methods.
         */

        // jwts is a utility class from JJWT library. builder creates a JWT builder instance.
        public String generateToken(String email) {
            return Jwts.builder()
                    .setSubject(email)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact(); // builds full JWT and returns it as a string.
        }

        public boolean validateToken(String token) {
            try {
                Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
                return true;
            } catch (JwtException e) {
                return false;
            }
        }
        /*
        Jwts.parserBuilder() → Starts creating a JWT parser (an object that can read & verify tokens).
        .setSigningKey(key) → Sets the same secret key that was used to sign the token originally. This key is needed to verify that the token’s signature is genuine.
        .build() → Builds the parser object.

        .parseClaimsJws(token)
    → Actually tries to decode and verify the token using the key.
    If the token is valid, parsing succeeds.
    If the token is expired, tampered, or malformed, it throws a JwtException.
         */

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
    Claims is an interface in the JJWT library that represents the payload section of a JWT token.
    It contains all the standard JWT fields (claims) such as:

sub → Subject (e.g., email or user ID)
iat → Issued At
exp → Expiration Time

When you parse a JWT, JJWT gives you the claims using this interface so you can read values like
claims.getSubject();      // sub
claims.getIssuedAt();      // iat
claims.getExpiration();    // exp

     */

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