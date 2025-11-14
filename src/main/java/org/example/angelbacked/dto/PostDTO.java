package org.example.angelbacked.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostDTO {
    private Integer id;
    private String content;
    private LocalDateTime createdAt;
    private Integer likes;
    private Integer commentsCount;
    private List<String> images;
    private List<String> tags;
    private Boolean isLiked;
    private Boolean isSaved;
    private AuthorDTO author;

    @Data
    public static class AuthorDTO {
        private Integer id;
        private String name;
        private String username;
        private Boolean isFollowing;
    }
}