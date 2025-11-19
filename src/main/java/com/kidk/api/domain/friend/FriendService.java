package com.kidk.api.domain.friend;

import com.kidk.api.domain.user.User;
import com.kidk.api.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    public List<Friend> getUserFriends(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Friend> sent = friendRepository.findByUser(user);
        List<Friend> received = friendRepository.findByFriendUser(user);

        return Stream.concat(sent.stream(), received.stream())
                .filter(f -> f.getStatus().equals("ACCEPTED"))
                .collect(Collectors.toList());
    }
}
