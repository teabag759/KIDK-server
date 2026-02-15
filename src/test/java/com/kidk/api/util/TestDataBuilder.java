package com.kidk.api.util;

import com.kidk.api.domain.account.entity.Account;
import com.kidk.api.domain.friend.entity.Friend;
import com.kidk.api.domain.friend.enums.FriendStatus;
import com.kidk.api.domain.mission.entity.Mission;
import com.kidk.api.domain.mission.enums.MissionStatus;
import com.kidk.api.domain.mission.enums.MissionType;
import com.kidk.api.domain.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 테스트 데이터 생성 유틸리티
 */
public class TestDataBuilder {

    public static User createUser(String firebaseUid, String email, String name, String userType) {
        return User.builder()
                .firebaseUid(firebaseUid)
                .email(email)
                .name(name)
                .userType(userType)
                .status("ACTIVE")
                .phone("010-0000-0000")
                .build();
    }

    public static User createParent(String firebaseUid, String email, String name) {
        return createUser(firebaseUid, email, name, "PARENT");
    }

    public static User createChild(String firebaseUid, String email, String name) {
        return createUser(firebaseUid, email, name, "CHILD");
    }

    public static Account createAccount(User user, String accountName, BigDecimal balance) {
        return Account.builder()
                .user(user)
                .accountName(accountName)
                .accountType("SAVINGS")
                .balance(balance)
                .active(true)
                .primary(false)
                .build();
    }

    public static Friend createFriendRequest(User user, User friendUser) {
        return Friend.builder()
                .user(user)
                .friendUser(friendUser)
                .status(FriendStatus.REQUESTED)
                .build();
    }

    public static Friend createAcceptedFriend(User user, User friendUser) {
        return Friend.builder()
                .user(user)
                .friendUser(friendUser)
                .status(FriendStatus.ACCEPTED)
                .build();
    }
}
