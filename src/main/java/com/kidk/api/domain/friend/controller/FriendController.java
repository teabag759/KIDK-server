package com.kidk.api.domain.friend.controller;

import com.kidk.api.domain.friend.service.FriendService;
import com.kidk.api.domain.friend.entity.Friend;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /// 친구 추가
    @GetMapping("/{userId}/friends")
    public ResponseEntity<List<Friend>> getFriends(@PathVariable Long userId) {
        return ResponseEntity.ok(friendService.getUserFriends(userId));
    }
}
