package com.collectiverse.collectiverse.dominio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.collectiverse.collectiverse.dominio.entity.UserProfile;

public interface RepoUserProfile extends JpaRepository<UserProfile, String> {
    boolean existsByEmail(String email);
}
