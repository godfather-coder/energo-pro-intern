package com.example.mssqll.service;

import com.example.mssqll.dto.response.UserResponseDto;
import com.example.mssqll.models.User;

import java.util.List;

public interface UserService {
    UserResponseDto updateUser(User user,Long id);
    String deleteUser(Long id);
    List<UserResponseDto> getAllUsers();
}
