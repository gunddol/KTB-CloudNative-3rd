package com.ktb.community.controller;

import com.ktb.community.domain.User;
import com.ktb.community.dto.ApiResponse;
import com.ktb.community.dto.UserDtos.*;
import com.ktb.community.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService users;
    public UserController(UserService users) { this.users = users; }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Validated SignupRequest req) {
        User u = users.signup(req);
        return ResponseEntity.ok(new ApiResponse<>("register_success", java.util.Map.of("userId", u.getId())));
    }
}
