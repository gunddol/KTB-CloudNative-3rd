package com.ktb.community.service;

import com.ktb.community.domain.User;
import com.ktb.community.dto.UserDtos.*;
import com.ktb.community.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository users;
    public UserService(UserRepository users) { this.users = users; }

    public User signup(SignupRequest req) {
        User u = new User();
        u.setEmail(req.email()); u.setPassword(req.password()); u.setNickname(req.nickname()); u.setProfileImageUrl(req.profile_image_url());
        return users.save(u);
    }
}
