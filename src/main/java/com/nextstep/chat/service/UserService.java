package com.nextstep.chat.service;

import com.nextstep.chat.model.User;
import com.nextstep.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    @Transactional
    public User createUser(User user) {
        // Check username exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        user.setOnline(false);
        return userRepository.save(user);
    }


    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    @Transactional
    public User updateUser(Long userId, User updateData) {
        User user = getUserById(userId);

        if (updateData.getUsername() != null) {
            user.setUsername(updateData.getUsername());
        }
        if (updateData.getEmail() != null) {
            user.setEmail(updateData.getEmail());
        }
        if (updateData.getProfilePicture() != null) {
            user.setProfilePicture(updateData.getProfilePicture());
        }

        return userRepository.save(user);
    }

    @Transactional
    public User updateUserStatus(Long userId, boolean online) {
        User user = getUserById(userId);
        user.setOnline(online);
        if (!online) {
            user.setLastSeen(LocalDateTime.now());
        }
        return userRepository.save(user);
    }


    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }


    public List<User> searchUsers(String query) {
        return userRepository.findAll().stream()
                .filter(u -> u.getUsername().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }
}