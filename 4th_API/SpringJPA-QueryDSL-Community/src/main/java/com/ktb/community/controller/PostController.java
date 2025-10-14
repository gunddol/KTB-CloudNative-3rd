package com.ktb.community.controller;

import com.ktb.community.domain.Post;
import com.ktb.community.dto.ApiResponse;
import com.ktb.community.dto.PostDtos.*;
import com.ktb.community.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostService posts;
    public PostController(PostService posts) { this.posts = posts; }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required=false) String query,
                                  @RequestParam(required=false) Integer authorId,
                                  @RequestParam(required=false) Boolean hasImage,
                                  @RequestParam(required=false) java.time.LocalDateTime from,
                                  @RequestParam(required=false) java.time.LocalDateTime to,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(defaultValue = "LATEST") String sort) {
        Page<Post> result = posts.search(query, authorId, hasImage, from, to, page, size, sort);
        return ResponseEntity.ok(new ApiResponse<>("get_posts_success", java.util.Map.of(
                "posts", result.getContent(),
                "pagination", java.util.Map.of("total_count", result.getTotalElements())
        )));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> detail(@PathVariable Integer postId,
                                    @RequestParam(defaultValue = "true") boolean increaseView) {
        Post p = posts.get(postId, increaseView);
        return ResponseEntity.ok(new ApiResponse<>("get_post_detail_success", p));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestHeader("X-USER-ID") Integer userId,
                                    @RequestBody @Validated CreatePostRequest req) {
        Post p = posts.create(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("create_post_success", java.util.Map.of("postId", p.getId())));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<?> update(@PathVariable Integer postId,
                                    @RequestHeader("X-USER-ID") Integer userId,
                                    @RequestBody @Validated UpdatePostRequest req) {
        Post p = posts.update(userId, postId, req);
        return ResponseEntity.ok(new ApiResponse<>("update_post_success", java.util.Map.of("postId", p.getId())));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<?> deleteSoft(@PathVariable Integer postId,
                                        @RequestHeader("X-USER-ID") Integer userId) {
        posts.softDelete(userId, postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> like(@PathVariable Integer postId,
                                  @RequestHeader("X-USER-ID") Integer userId) {
        long count = posts.like(userId, postId);
        return ResponseEntity.ok(new ApiResponse<>("like_success", java.util.Map.of("like", count)));
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<?> unlike(@PathVariable Integer postId,
                                    @RequestHeader("X-USER-ID") Integer userId) {
        long count = posts.unlike(userId, postId);
        return ResponseEntity.ok(new ApiResponse<>("unlike_success", java.util.Map.of("like", count)));
    }

    @GetMapping("/me/likes")
    public ResponseEntity<?> myLikes(@RequestHeader("X-USER-ID") Integer userId,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        Page<Post> liked = posts.likedBy(userId, page, size);
        return ResponseEntity.ok(new ApiResponse<>("get_liked_posts_success",
                java.util.Map.of("posts", liked.getContent(), "pagination", java.util.Map.of("total_count", liked.getTotalElements()))));
    }
}
