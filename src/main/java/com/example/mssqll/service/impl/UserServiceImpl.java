package com.example.mssqll.service.impl;

import com.example.mssqll.dto.response.UserResponseDto;
import com.example.mssqll.models.Role;
import com.example.mssqll.models.User;
import com.example.mssqll.repository.UserRepository;
import com.example.mssqll.service.UserService;
import com.example.mssqll.utiles.exceptions.AdminNotEditException;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public UserResponseDto updateUser(User user, Long id) {
        Optional<User> user12 = userRepository.findById(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();
        if (user12.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        } else {
            User user1 = user12.get();
            if (userDetails.getRole() == user1.getRole() && !user1.getId().equals(userDetails.getId())) {
                throw new AdminNotEditException("You cannot update an admin");
            }
            user1.setFirstName(user.getFirstName());
            user1.setLastName(user.getLastName());
            if (user.getPassword() != (null)) {
                user1.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            user1.setEmail(user.getEmail());
            user1.setRole(user.getRole());
            user1.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user1);
            return UserResponseDto.builder()
                    .email(user1.getEmail())
                    .firstName(user1.getFirstName())
                    .lastName(user1.getLastName())
                    .updatedAt(user1.getUpdatedAt())
                    .role(user1.getRole())
                    .createdAt(user1.getCreatedAt())
                    .id(user1.getId())
                    .build();
        }
    }

    @Override
    public UserResponseDto deleteUser(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();
        Optional<User> user = userRepository.findById(id);
        User user1;
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        user1 = user.get();
        if (user1.getRole() == Role.ROLE_ADMIN) {
            throw new AdminNotEditException("You can't edit admin from Role " + user1.getRole() +
                    " to role " + Role.SOFT_DELETED);
        }
        user1.setRole(Role.SOFT_DELETED);
        user1 = userRepository.save(user1);
        return UserResponseDto.builder()
                .email(user1.getEmail())
                .firstName(user1.getFirstName())
                .lastName(user1.getLastName())
                .updatedAt(user1.getUpdatedAt())
                .role(user1.getRole())
                .createdAt(user1.getCreatedAt())
                .id(user1.getId())
                .build();
    }

    @Override
    public List<UserResponseDto> getAllUsers() {

        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("activeFilter").setParameter("role", "SOFT_DELETED");
        List<User> users = session.createQuery("FROM User", User.class).getResultList();

        List<UserResponseDto> userResponseDtos = new ArrayList<>();
        for (User e : users) {
            userResponseDtos.add(
                    UserResponseDto.builder()
                            .email(e.getEmail())
                            .firstName(e.getFirstName())
                            .lastName(e.getLastName())
                            .updatedAt(e.getUpdatedAt())
                            .role(e.getRole())
                            .createdAt(e.getCreatedAt())
                            .id(e.getId())
                            .build()
            );
        }
        return userResponseDtos;

    }
}
