package com.kidk.api.domain.friend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidk.api.domain.friend.dto.FriendRequest;
import com.kidk.api.domain.friend.dto.FriendResponse;
import com.kidk.api.domain.friend.enums.FriendStatus;
import com.kidk.api.domain.friend.service.FriendService;
import com.kidk.api.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FriendController.class)
@DisplayName("FriendController 통합 테스트")
class FriendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FriendService friendService;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser
    @DisplayName("내 친구 목록 조회 API 성공")
    void getMyFriends_Success() throws Exception {
        // given
        when(userService.getUserIdByFirebaseUid(any())).thenReturn(1L);

        FriendResponse friend = FriendResponse.builder()
                .id(1L)
                .userId(1L)
                .userName("User1")
                .friendUserId(2L)
                .friendUserName("User2")
                .status(FriendStatus.ACCEPTED)
                .createdAt(LocalDateTime.now())
                .build();

        List<FriendResponse> friends = Arrays.asList(friend);
        when(friendService.getUserFriends(1L)).thenReturn(friends);

        // when & then
        mockMvc.perform(get("/api/v1/friends/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].status").value("ACCEPTED"));
    }

    @Test
    @WithMockUser
    @DisplayName("친구 요청 전송 API 성공")
    void sendFriendRequest_Success() throws Exception {
        // given
        when(userService.getUserIdByFirebaseUid(any())).thenReturn(1L);

        FriendRequest.SendRequest request = FriendRequest.SendRequest.builder()
                .friendUserId(2L)
                .build();

        FriendResponse response = FriendResponse.builder()
                .id(1L)
                .userId(1L)
                .userName("User1")
                .friendUserId(2L)
                .friendUserName("User2")
                .status(FriendStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();

        when(friendService.sendFriendRequest(anyLong(), anyLong())).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/friends/request")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REQUESTED"));
    }

    @Test
    @WithMockUser
    @DisplayName("친구 요청 승인 API 성공")
    void acceptFriendRequest_Success() throws Exception {
        // given
        when(userService.getUserIdByFirebaseUid(any())).thenReturn(2L);

        FriendResponse response = FriendResponse.builder()
                .id(1L)
                .userId(1L)
                .userName("User1")
                .friendUserId(2L)
                .friendUserName("User2")
                .status(FriendStatus.ACCEPTED)
                .createdAt(LocalDateTime.now())
                .build();

        when(friendService.acceptFriendRequest(1L, 2L)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/friends/requests/1/accept")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }

    @Test
    @WithMockUser
    @DisplayName("친구 요청 거부 API 성공")
    void rejectFriendRequest_Success() throws Exception {
        // given
        when(userService.getUserIdByFirebaseUid(any())).thenReturn(2L);

        // when & then
        mockMvc.perform(post("/api/v1/friends/requests/1/reject")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("친구 삭제 API 성공")
    void removeFriend_Success() throws Exception {
        // given
        when(userService.getUserIdByFirebaseUid(any())).thenReturn(1L);

        // when & then
        mockMvc.perform(delete("/api/v1/friends/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("받은 친구 요청 목록 조회 API 성공")
    void getReceivedRequests_Success() throws Exception {
        // given
        when(userService.getUserIdByFirebaseUid(any())).thenReturn(1L);

        FriendResponse request = FriendResponse.builder()
                .id(1L)
                .userId(2L)
                .userName("User2")
                .friendUserId(1L)
                .friendUserName("User1")
                .status(FriendStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();

        List<FriendResponse> requests = Arrays.asList(request);
        when(friendService.getReceivedRequests(1L)).thenReturn(requests);

        // when & then
        mockMvc.perform(get("/api/v1/friends/requests/received"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("REQUESTED"));
    }
}
