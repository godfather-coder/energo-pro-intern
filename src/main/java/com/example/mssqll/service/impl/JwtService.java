package com.example.mssqll.service.impl;

import com.example.mssqll.dto.response.TokenValidationResult;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;


@Service
public class JwtService {

    @Value("${token.secret.key}")
    String jwtSecretKey;

    @Value("${token.refresh.expirationms}")
    Long refreshExpirationMs;

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
        return generateToken(new HashMap<>(), userDetails, logout,jwtExpirationMs);
    }

      public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails, false, refreshExpirationMs);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    public void logout(String token) {
        UserDetails userDetails = userService.userDetailsService().loadUserByUsername(extractUserName(token));
        if (validateToken(token, userDetails).isValid()) {
            Date expiryDate = extractExpiration(token);
            tokenBlacklistService.blacklistToken(token, expiryDate);
        }
    }

    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, Boolean logout,Long expirationMs) {
        try {
            List<String> roles = new ArrayList<>();
            for (GrantedAuthority authority : userDetails.getAuthorities()) {
                roles.add(authority.getAuthority());
            }
            extraClaims.put("roles", roles);
            return Jwts
                    .builder()
                    .setClaims(extraClaims)
                    .setSubject(userDetails.getUsername())
                    .claim("logOut", logout)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                    .signWith(getKey(), SignatureAlgorithm.HS256)
                    .compact();
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

            List<String> tokenRoles = claims.get("roles", List.class);

            Collection<? extends GrantedAuthority> userRoles = userDetails.getAuthorities();

            if (tokenRoles.contains("SOFT_DELETED")) {
                return new TokenValidationResult(false, "User is deleted . Access denied.");
            }

            boolean hasMatchingRole = userRoles.stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(tokenRoles::contains);

            if (!hasMatchingRole) {
                return new TokenValidationResult(false, "User role does not match token.");
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

    public TokenValidationResult validateTokenWithoutUserName(String token) {
        try {
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

    public TokenValidationResult validateRefreshToken(String refreshToken, String userName) {
        try {
            String username = extractUserName(refreshToken);
            boolean res = (username.equals(userName)) && !isTokenExpired(refreshToken);
            return new TokenValidationResult(res,"Refresh Token is valid");
        } catch (Exception e) {
            return new TokenValidationResult(false, "JWT token validation failed."+e.getMessage());
        }
    }

    public String refreshAccessToken(String refreshToken) {
        Claims claims = extractAllClaims(refreshToken);
        String username = claims.getSubject();

        if (isTokenExpired(refreshToken) || tokenBlacklistService.isTokenBlacklisted(refreshToken) || !validateRefreshToken(refreshToken,username).isValid()) {
            throw new JwtException("Refresh token is invalid or expired.");
        }

        UserDetails userDetails = userService.userDetailsService().loadUserByUsername(username);
        return generateToken(userDetails, false);
    }

}