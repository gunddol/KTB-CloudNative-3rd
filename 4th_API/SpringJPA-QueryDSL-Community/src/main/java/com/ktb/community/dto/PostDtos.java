package com.ktb.community.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class PostDtos {
    public record CreatePostRequest(@NotBlank String title, @NotBlank String content, List<String> image_urls) { }
    public record UpdatePostRequest(String title, String content, List<String> image_urls) { }
}
