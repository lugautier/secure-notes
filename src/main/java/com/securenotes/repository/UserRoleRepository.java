package com.securenotes.repository;

import com.securenotes.domain.Role;
import com.securenotes.domain.UserRole;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
  Set<UserRole> findByUserId(UUID userId);

  Optional<UserRole> findByUserIdAndRole(UUID userId, Role role);
}
