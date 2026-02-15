package com.kidk.api.domain.friend.controller;

import com.kidk.api.domain.friend.dto.FriendRequest;
import com.kidk.api.domain.friend.dto.FriendResponse;
import com.kidk.api.domain.friend.service.FriendService;
import com.kidk.api.domain.user.service.UserService;
import com.kidk.api.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final UserService userService;

    private Long currentUserId(UserDetails userDetails) {
        return userService.getUserIdByFirebaseUid(userDetails.getUsername());
    }

    /**
     * 내 친구 목록 조회 (ACCEPTED 상태만)
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getMyFriends(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<FriendResponse> friends = friendService.getUserFriends(currentUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success(friends));
    }

    /**
     * 친구 요청 전송
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<FriendResponse>> sendFriendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody FriendRequest.SendRequest request) {
        FriendResponse response = friendService.sendFriendRequest(
                currentUserId(userDetails),
                request.getFriendUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 친구 요청 승인
     */
    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<ApiResponse<FriendResponse>> acceptFriendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId) {
        FriendResponse response = friendService.acceptFriendRequest(
                requestId,
                currentUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 친구 요청 거부
     */
    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectFriendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId) {
        friendService.rejectFriendRequest(requestId, currentUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 친구 삭제 (친구 관계 해제)
     */
    @DeleteMapping("/{friendId}")
    public ResponseEntity<ApiResponse<Void>> removeFriend(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long friendId) {
        friendService.removeFriend(friendId, currentUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 받은 친구 요청 목록
     */
    @GetMapping("/requests/received")
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getReceivedRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<FriendResponse> requests = friendService.getReceivedRequests(currentUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    /**
     * 보낸 친구 요청 목록
     */
    @GetMapping("/requests/sent")
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getSentRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<FriendResponse> requests = friendService.getSentRequests(currentUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success(requests));
    }
}
