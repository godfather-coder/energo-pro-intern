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
        try {
            SignResponseDto res = authenticationService.signin(request);
            return ResponseEntity.ok(res);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("მონაცემები არასწორია.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("სერვერზე დავიქსირდა შეცდომა.");
        }
    }



    @GetMapping("/user")
    public ResponseEntity<?> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication.getPrincipal());
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
}