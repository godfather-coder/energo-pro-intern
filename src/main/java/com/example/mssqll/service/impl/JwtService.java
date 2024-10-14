package com.example.mssqll.service.impl;

import com.example.mssqll.dto.response.TokenValidationResult;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;


@Service
public class JwtService {

    @Value("${token.secret.key}")
    String jwtSecretKey;

    SecretKey secretKey;

    @Value("${token.expirationms}")
    Long jwtExpirationMs;

    @Autowired
    private final TokenBlacklistService tokenBlacklistService;

    @Autowired
    private final UserService userService;

    public JwtService(TokenBlacklistService tokenBlacklistService, UserService userService) {
        this.tokenBlacklistService = tokenBlacklistService;
        this.userService = userService;
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(UserDetails userDetails, Boolean logout) {
        return generateToken(new HashMap<>(), userDetails, logout);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    public boolean isLogOut(String token) {
        Claims claims = extractAllClaims(token);
        Object logOutClaim = claims.get("logOut");
        if (logOutClaim instanceof Boolean) {
            return (Boolean) logOutClaim;
        }
        return false;
    }

    public void logout(String token) {
        UserDetails userDetails = userService.userDetailsService().loadUserByUsername(extractUserName(token));
        if (validateToken(token, userDetails).isValid()) {
            Date expiryDate = extractExpiration(token);
            tokenBlacklistService.blacklistToken(token, expiryDate);
        }
    }

    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, Boolean logout) {
        try {
            String tok = Jwts
                    .builder()
                    .setClaims(extraClaims)
                    .setSubject(userDetails.getUsername())
                    .claim("logOut", logout)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                    .signWith(getKey(), SignatureAlgorithm.HS256)
                    .compact();
            return tok;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretKey));
    }


    public TokenValidationResult validateToken(String token, UserDetails userDetails) {
        try {
            Claims claims = extractAllClaims(token);

            String username = claims.getSubject();
            if (!username.equals(userDetails.getUsername())) {
                return new TokenValidationResult(false, "Username does not match token.");
            }

            if (isTokenExpired(token)) {
                return new TokenValidationResult(false, "JWT token is expired.");
            }

            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                return new TokenValidationResult(false, "JWT token is blacklisted.");
            }

            return new TokenValidationResult(true, "Token is valid.");
        } catch (ExpiredJwtException e) {
            return new TokenValidationResult(false, "JWT token is expired.");
        } catch (UnsupportedJwtException e) {
            return new TokenValidationResult(false, "Unsupported JWT token.");
        } catch (MalformedJwtException e) {
            return new TokenValidationResult(false, "Invalid JWT token.");
        } catch (SignatureException e) {
            return new TokenValidationResult(false, "Invalid JWT signature.");
        } catch (IllegalArgumentException e) {
            return new TokenValidationResult(false, "JWT claims string is empty.");
        } catch (Exception e) {
            return new TokenValidationResult(false, "JWT token validation failed.");
        }
    }


}