package com.sep490.vtuber_fanhub.services;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sep490.vtuber_fanhub.models.SystemAccount;
import com.sep490.vtuber_fanhub.models.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JWTService {
    private final StringRedisTemplate redisTemplate;

    private SecretKey getSecretKey() {
        String secret = "aec5162f0ed647d4bb3cc9c926b2fb6af809992b56055bed2130adb9a1c3de8da55bfd2ef287029cb7a7e36b0d15b14b04d68cde8b060c3e5fbc6fcc7891bbe9";
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
    }

    public String generateToken(User user) {
        try {
            // Header
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS512)
                    .type(JOSEObjectType.JWT)
                    .build();

            // Payload (claim)
            Date now = new Date();
            long validityInMs = 3600_000;
            Date exp = new Date(now.getTime() + validityInMs);
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issuer("vhub.com")
                    .issueTime(now)
                    .expirationTime(exp)
                    .claim("scope", user.getRole())
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claims);
            JWSSigner signer = new MACSigner(getSecretKey());
            signedJWT.sign(signer);

            return signedJWT.serialize();

        } catch (JOSEException e) {
            throw new RuntimeException("Cannot generate JWT", e);
        }
    }

    public String generateTokenSystemAccount(SystemAccount user) {
        try {
            // Header
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS512)
                    .type(JOSEObjectType.JWT)
                    .build();

            // Payload (claim)
            Date now = new Date();
            long validityInMs = 3600_000;
            Date exp = new Date(now.getTime() + validityInMs);
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issuer("vhub.com")
                    .issueTime(now)
                    .expirationTime(exp)
                    .claim("scope", user.getRole())
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claims);
            JWSSigner signer = new MACSigner(getSecretKey());
            signedJWT.sign(signer);

            return signedJWT.serialize();

        } catch (JOSEException e) {
            throw new RuntimeException("Cannot generate JWT", e);
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            return signedJWT.getJWTClaimsSet().getSubject();

        } catch (ParseException e) {
            throw new RuntimeException("Invalid JWT token format", e);
        }
    }

    public String getCurrentToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    public boolean isTokenValid(String token) {
        try {
            // Check if token is blacklisted in Redis
            if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token))) {
                return false;
            }

            SignedJWT signedJWT = SignedJWT.parse(token);
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            
            if (expirationTime == null) {
                return false;
            }
            
            // Check if token is expired
            if (expirationTime.before(new Date())) {
                return false;
            }
            
            // Verify signature
            JWSVerifier verifier = new MACVerifier(getSecretKey());
            return signedJWT.verify(verifier);
            
        } catch (ParseException | JOSEException e) {
            return false;
        }
    }

    public Date getExpirationTimeFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getExpirationTime();
        } catch (ParseException e) {
            return null;
        }
    }
}
