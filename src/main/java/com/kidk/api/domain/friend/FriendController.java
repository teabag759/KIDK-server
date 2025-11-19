package com.kidk.api.domain.friend;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping("/{userId}")
    public List<Friend> getFriends(@PathVariable Long userId) {
        return friendService.getUserFriends(userId);
    }
}
