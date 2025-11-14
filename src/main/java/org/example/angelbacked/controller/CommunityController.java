package org.example.angelbacked.controller;

import org.example.angelbacked.entity.User;
import org.example.angelbacked.service.*;
import org.example.angelbacked.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/community")
public class CommunityController {
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private UserRelationService userRelationService;
    
    @Autowired
    private TopicService topicService;
    
    /**
     * 获取帖子列表
     */
    @GetMapping("/posts")
    public Result getPosts(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(required = false) String filter,
                          @RequestParam(required = false) String sort,
                          HttpServletRequest httpRequest) {
        User currentUser = (User) httpRequest.getAttribute("currentUser");
        Integer userId = currentUser != null ? currentUser.getId() : null;
        return postService.getPosts(page, filter, sort, userId);
    }
    
    /**
     * 发布帖子
     */
    @PostMapping("/posts")
    public Result createPost(@RequestBody CreatePostRequest request, HttpServletRequest httpRequest) {
        User currentUser = (User) httpRequest.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        // 处理图片数组和标签数组转换为JSON字符串存储
        String imagesJson = null;
        String tagsJson = null;
        
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            imagesJson = "[" + String.join(",", request.getImages().stream()
                    .map(img -> "\"" + img + "\"")
                    .toArray(String[]::new)) + "]";
        }
        
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            tagsJson = "[" + String.join(",", request.getTags().stream()
                    .map(tag -> "\"" + tag + "\"")
                    .toArray(String[]::new)) + "]";
        }
        
        return postService.createPost(request.getContent(), imagesJson, tagsJson, currentUser.getId());
    }
    
    /**
     * 编辑帖子
     */
    @PutMapping("/posts/{id}")
    public Result updatePost(@PathVariable Integer id, 
                            @RequestBody UpdatePostRequest request, 
                            HttpServletRequest httpRequest) {
        User currentUser = (User) httpRequest.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        // 处理标签数组转换为JSON字符串存储
        String tagsJson = null;
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            tagsJson = "[" + String.join(",", request.getTags().stream()
                    .map(tag -> "\"" + tag + "\"")
                    .toArray(String[]::new)) + "]";
        }
        
        return postService.updatePost(id, request.getContent(), tagsJson, currentUser.getId());
    }
    
    /**
     * 删除帖子
     */
    @DeleteMapping("/posts/{id}")
    public Result deletePost(@PathVariable Integer id, HttpServletRequest httpRequest) {
        User currentUser = (User) httpRequest.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        return postService.deletePost(id, currentUser.getId());
    }
    
    /**
     * 点赞/取消点赞帖子
     */
    @PostMapping("/posts/{id}/like")
    public Result likePost(@PathVariable Integer id, HttpServletRequest httpRequest) {
        User currentUser = (User) httpRequest.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        return postService.likePost(id, currentUser.getId());
    }
    
    /**
     * 收藏/取消收藏帖子
     */
    @PostMapping("/posts/{id}/save")
    public Result savePost(@PathVariable Integer id, HttpServletRequest httpRequest) {
        User currentUser = (User) httpRequest.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        return postService.savePost(id, currentUser.getId());
    }
    
    /**
     * 获取帖子评论
     */
    @GetMapping("/posts/{id}/comments")
    public Result getComments(@PathVariable Integer id) {
        return commentService.getComments(id);
    }
    
    /**
     * 发布评论
     */
    @PostMapping("/posts/{id}/comments")
    public Result createComment(@PathVariable Integer id, 
                               @RequestBody CreateCommentRequest request, 
                               HttpServletRequest httpRequest) {
        User currentUser = (User) httpRequest.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        return commentService.createComment(id, request.getContent(), currentUser.getId());
    }
    
    /**
     * 点赞/取消点赞评论
     */
    @PostMapping("/comments/{id}/like")
    public Result likeComment(@PathVariable Integer id, HttpServletRequest httpRequest) {
        User currentUser = (User) httpRequest.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        return commentService.likeComment(id, currentUser.getId());
    }
    
    /**
     * 关注/取消关注用户
     */
    @PostMapping("/users/{id}/follow")
    public Result followUser(@PathVariable Integer id, HttpServletRequest httpRequest) {
        User currentUser = (User) httpRequest.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        return userRelationService.followUser(id, currentUser.getId());
    }
    
    /**
     * 获取推荐关注用户
     */
    @GetMapping("/users/recommended")
    public Result getRecommendedUsers(HttpServletRequest httpRequest) {
        User currentUser = (User) httpRequest.getAttribute("currentUser");
        Integer currentUserId = currentUser != null ? currentUser.getId() : null;
        return userRelationService.getRecommendedUsers(currentUserId);
    }
    
    /**
     * 获取热门话题
     */
    @GetMapping("/topics/hot")
    public Result getHotTopics() {
        return topicService.getHotTopics();
    }
    
    // 请求体内部类
    public static class CreatePostRequest {
        private String content;
        private List<String> images;
        private List<String> tags;
        
        // Getters and Setters
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        public List<String> getImages() {
            return images;
        }
        
        public void setImages(List<String> images) {
            this.images = images;
        }
        
        public List<String> getTags() {
            return tags;
        }
        
        public void setTags(List<String> tags) {
            this.tags = tags;
        }
    }
    
    public static class UpdatePostRequest {
        private String content;
        private List<String> tags;
        
        // Getters and Setters
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        public List<String> getTags() {
            return tags;
        }
        
        public void setTags(List<String> tags) {
            this.tags = tags;
        }
    }
    
    public static class CreateCommentRequest {
        private String content;
        
        // Getter and Setter
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
}