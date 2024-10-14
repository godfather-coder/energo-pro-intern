package com.example.mssqll.service.impl;

import com.example.mssqll.models.JwtAuthenticationResponse;
import com.example.mssqll.models.SignInRequest;
import com.example.mssqll.models.SignUpRequest;
import com.example.mssqll.models.User;
import com.example.mssqll.repository.UserRepository;
import com.example.mssqll.utiles.exceptions.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public JwtAuthenticationResponse signup(SignUpRequest request) throws UserAlreadyExistsException {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException();
        }
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        user = userService.save(user);
        var jwt = jwtService.generateToken(user, false);
        return JwtAuthenticationResponse.builder()
                .token(jwt)
                .build();
    }


    public JwtAuthenticationResponse signin(SignInRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        var jwt = jwtService.generateToken(user, false);
        return JwtAuthenticationResponse.builder().token(jwt).build();
    }

    public ResponseEntity<?> logout(@RequestHeader(name = "Authorization") String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            jwtService.logout(token);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logged out successfully.");
            return ResponseEntity.status(200).body(response);
        }
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid authorization.");

        return ResponseEntity.badRequest().body(errorResponse);
    }
}