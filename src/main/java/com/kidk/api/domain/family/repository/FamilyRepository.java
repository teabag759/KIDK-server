package com.kidk.api.domain.family.repository;

import com.kidk.api.domain.family.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FamilyRepository extends JpaRepository<Family, Long> {
    Optional<Family> findByInviteCode(String inviteCode);
}
