package com.kidk.api.domain.familymember.repository;

import com.kidk.api.domain.family.entity.Family;
import com.kidk.api.domain.familymember.entity.FamilyMember;
import com.kidk.api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {

    // 가족 기준 조회
    List<FamilyMember> findByFamily(Family family);

    List<FamilyMember> findByFamilyId(Long familyId);

    // 유저 기준 조회
    List<FamilyMember> findByUser(User user);

    List<FamilyMember> findByUserId(Long userId);

    // 한 가족 내 특정 유저
    Optional<FamilyMember> findByFamilyAndUser(Family family, User user);
}