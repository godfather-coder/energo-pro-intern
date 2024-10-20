package com.example.mssqll.repository;

import com.example.mssqll.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    @Query("select e from User e where e.role!='ROLE_ADMIN'")
    List<User> findByIsAdminFalse();
}