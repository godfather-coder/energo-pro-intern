package com.example.mssqll.controller.Auth;

import com.example.mssqll.dto.response.SignResponseDto;
import com.example.mssqll.dto.response.UserResponseDto;
import com.example.mssqll.models.JwtAuthenticationResponse;
import com.example.mssqll.models.SignInRequest;
import com.example.mssqll.models.SignUpRequest;
import com.example.mssqll.models.User;
import com.example.mssqll.service.impl.AuthenticationService;
import com.example.mssqll.utiles.exceptions.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest request) {
        try {
            JwtAuthenticationResponse response = authenticationService.signup(request);
            return ResponseEntity.ok(response);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("სერვერზე დავიქსირდა შეცდომა.");
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody SignInRequest request) {
        HashMap<String, String> response = new HashMap<>();
        try {
            SignResponseDto res = authenticationService.signin(request);
            return ResponseEntity.ok(res);
        } catch (BadCredentialsException e) {
            response.put("error", "მონაცემები არასწორია.");
            response.put("Exception", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        } catch (Exception e) {
            response.put("error", "INTERNAL_SERVER_ERROR");
            response.put("Exception", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }


    @GetMapping("/user")
    public ResponseEntity<?> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();
        UserResponseDto dto = UserResponseDto.builder()
                .id(userDetails.getId())
                .role(userDetails.getRole())
                .email(userDetails.getEmail())
                .updatedAt(userDetails.getUpdatedAt())
                .firstName(userDetails.getFirstName())
                .lastName(userDetails.getLastName())
                .createdAt(userDetails.getCreatedAt())
                .build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestHeader(name = "Authorization") String authorization) {
        return authenticationService.logout(authorization);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(@RequestHeader(name="AuthRefresh") String refreshToken) {
        try {
            String newAccessToken = authenticationService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(new JwtAuthenticationResponse(newAccessToken, refreshToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token.");
        }
    }
}