package com.kidk.api.domain.family.service;

import com.kidk.api.domain.family.entity.Family;
import com.kidk.api.domain.family.repository.FamilyRepository;
import com.kidk.api.domain.familymember.entity.FamilyMember;
import com.kidk.api.domain.familymember.repository.FamilyMemberRepository;
import com.kidk.api.domain.user.entity.User;
import com.kidk.api.domain.user.repository.UserRepository;
import com.kidk.api.global.exception.CustomException;
import com.kidk.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FamilyService {

        private final FamilyRepository familyRepository;
        private final UserRepository userRepository;
        private final FamilyMemberRepository familyMemberRepository;

        // 1. 가족 생성 (부모)
        @Transactional
        public Family createFamily(Long userId, String familyName) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                Family family = Family.builder()
                                .familyName(familyName)
                                .build();
                familyRepository.save(family);

                // 생성자를 주 보호자(FamilyMember)로 자동 등록
                FamilyMember member = FamilyMember.builder()
                                .family(family)
                                .user(user)
                                .role("PARENT")
                                .primaryParent(true)
                                .status("ACCEPTED")
                                .build();

                familyMemberRepository.save(member);

                return family;
        }

        // 2. 가족 가입(자녀/배우자) - 초대 코드 입력
        @Transactional
        public FamilyMember joinFamily(Long userId, String inviteCode) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                Family family = familyRepository.findByInviteCode(inviteCode)
                                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INVITE_CODE));

                // 초대 코드 만료 체크 (생성 후 7일)
                if (family.getCreatedAt().plusDays(7).isBefore(LocalDateTime.now())) {
                        throw new CustomException(ErrorCode.INVITE_CODE_EXPIRED);
                }

                // 이미 가입된 멤버인지 확인
                if (familyMemberRepository.findByFamilyAndUser(family, user).isPresent()) {
                        throw new CustomException(ErrorCode.ALREADY_IN_FAMILY);
                }

                // 주 보호자 찾기(초대자 기록용)
                User inviter = familyMemberRepository.findByFamily(family).stream()
                                .filter(FamilyMember::isPrimaryParent)
                                .findFirst()
                                .map(FamilyMember::getUser)
                                .orElse(null);

                // 멤버 등록
                FamilyMember member = FamilyMember.builder()
                                .family(family)
                                .user(user)
                                .role(user.getUserType())
                                .primaryParent(false)
                                .invitedBy(inviter)
                                .status("ACCEPTED")
                                .build();

                return familyMemberRepository.save(member);
        }

        // 조회 로직들
        public List<Family> findAll() {
                return familyRepository.findAll();
        }

        public Family findById(Long id) {
                return familyRepository.findById(id)
                                .orElseThrow(() -> new CustomException(ErrorCode.FAMILY_NOT_FOUND));
        }

        // 내 가족 조회
        public Family getMyFamily(Long userId) {
                return familyMemberRepository.findByUserId(userId).stream()
                                .filter(fm -> "ACCEPTED".equals(fm.getStatus()))
                                .findFirst()
                                .map(FamilyMember::getFamily)
                                .orElseThrow(() -> new CustomException(ErrorCode.FAMILY_NOT_FOUND));
        }

        // 초대 생성 (초대 코드 발급)
        @Transactional
        public String createInvite(Long familyId, Long userId) {
                Family family = findById(familyId);

                // 권한 확인 (가족 구성원인지)
                boolean isMember = familyMemberRepository.findByFamily(family).stream()
                                .anyMatch(fm -> fm.getUser().getId().equals(userId)
                                                && "ACCEPTED".equals(fm.getStatus()));

                if (!isMember) {
                        throw new CustomException(ErrorCode.ACCESS_DENIED);
                }

                // 초대 코드 재생성
                family.generateInviteCode();
                familyRepository.save(family);

                return family.getInviteCode();
        }

        // 초대 승인
        @Transactional
        public void acceptInvitation(Long familyId, Long memberId, Long userId) {
                FamilyMember member = familyMemberRepository.findById(memberId)
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                if (!member.getFamily().getId().equals(familyId)) {
                        throw new CustomException(ErrorCode.FAMILY_NOT_FOUND);
                }

                // 본인 초대만 승인 가능
                if (!member.getUser().getId().equals(userId)) {
                        throw new CustomException(ErrorCode.ACCESS_DENIED);
                }

                if (!"PENDING".equals(member.getStatus())) {
                        throw new CustomException(ErrorCode.ALREADY_REVIEWED);
                }

                member.setStatus("ACCEPTED");
                member.setAcceptedAt(LocalDateTime.now());
                familyMemberRepository.save(member);
        }

        // 초대 거부
        @Transactional
        public void rejectInvitation(Long familyId, Long memberId, Long userId) {
                FamilyMember member = familyMemberRepository.findById(memberId)
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                if (!member.getFamily().getId().equals(familyId)) {
                        throw new CustomException(ErrorCode.FAMILY_NOT_FOUND);
                }

                // 본인 초대만 거부 가능
                if (!member.getUser().getId().equals(userId)) {
                        throw new CustomException(ErrorCode.ACCESS_DENIED);
                }

                if (!"PENDING".equals(member.getStatus())) {
                        throw new CustomException(ErrorCode.ALREADY_REVIEWED);
                }

                member.setStatus("REJECTED");
                familyMemberRepository.save(member);
        }

        // 대기 중인 초대 목록 조회
        public List<FamilyMember> getPendingInvitations(Long familyId) {
                Family family = findById(familyId);
                return familyMemberRepository.findByFamily(family).stream()
                                .filter(fm -> "PENDING".equals(fm.getStatus()))
                                .toList();
        }

        // 가족 구성원 목록 조회
        public List<FamilyMember> getFamilyMembers(Long familyId) {
                Family family = findById(familyId);
                return familyMemberRepository.findByFamily(family);
        }

        // 가족 구성원 삭제 (주 보호자 또는 본인)
        @Transactional
        public void removeMember(Long familyId, Long memberId, Long requesterId) {
                FamilyMember member = familyMemberRepository.findById(memberId)
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                if (!member.getFamily().getId().equals(familyId)) {
                        throw new CustomException(ErrorCode.FAMILY_NOT_FOUND);
                }

                boolean isPrimaryParent = familyMemberRepository.findByFamily(member.getFamily()).stream()
                                .anyMatch(fm -> fm.getUser().getId().equals(requesterId)
                                                && fm.isPrimaryParent()
                                                && "ACCEPTED".equals(fm.getStatus()));

                boolean isSelf = member.getUser().getId().equals(requesterId);

                if (!isPrimaryParent && !isSelf) {
                        throw new CustomException(ErrorCode.ACCESS_DENIED);
                }

                // 주 보호자는 스스로 삭제 불가 (최소 1명 유지)
                if (member.isPrimaryParent()) {
                        throw new CustomException(ErrorCode.ACCESS_DENIED);
                }

                familyMemberRepository.delete(member);
        }

        // 주 보호자 변경
        @Transactional
        public void setPrimaryParent(Long familyId, Long memberId, Long requesterId) {
                Family family = findById(familyId);

                boolean isPrimaryParent = familyMemberRepository.findByFamily(family).stream()
                                .anyMatch(fm -> fm.getUser().getId().equals(requesterId)
                                                && fm.isPrimaryParent()
                                                && "ACCEPTED".equals(fm.getStatus()));

                if (!isPrimaryParent) {
                        throw new CustomException(ErrorCode.ACCESS_DENIED);
                }

                FamilyMember target = familyMemberRepository.findById(memberId)
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                if (!target.getFamily().getId().equals(familyId)) {
                        throw new CustomException(ErrorCode.FAMILY_NOT_FOUND);
                }

                // 기존 주 보호자 해제
                familyMemberRepository.findByFamily(family).stream()
                                .filter(FamilyMember::isPrimaryParent)
                                .forEach(fm -> fm.setPrimaryParent(false));

                target.setPrimaryParent(true);
        }

        // 가족 삭제 (주 보호자만 가능)
        @Transactional
        public void deleteFamily(Long familyId, Long userId) {
                Family family = findById(familyId);

                // 주 보호자 확인
                boolean isPrimaryParent = familyMemberRepository.findByFamily(family).stream()
                                .anyMatch(fm -> fm.getUser().getId().equals(userId)
                                                && fm.isPrimaryParent()
                                                && "ACCEPTED".equals(fm.getStatus()));

                if (!isPrimaryParent) {
                        throw new CustomException(ErrorCode.ACCESS_DENIED);
                }

                // 가족 구성원 모두 삭제
                familyMemberRepository.deleteAll(familyMemberRepository.findByFamily(family));

                // 가족 삭제
                familyRepository.delete(family);
        }
}
