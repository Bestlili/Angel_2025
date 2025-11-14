package org.example.angelbacked.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.angelbacked.dto.PostDTO;
import org.example.angelbacked.entity.*;
import org.example.angelbacked.mapper.*;
import org.example.angelbacked.service.PostService;
import org.example.angelbacked.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {
    
    @Autowired
    private PostMapper postMapper;
    
    @Autowired
    private PostLikeMapper postLikeMapper;
    
    @Autowired
    private PostSaveMapper postSaveMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private UserFollowMapper userFollowMapper;
    
    private static final int PAGE_SIZE = 10;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public Result getPosts(int page, String filter, String sort, Integer userId) {
        int offset = (page - 1) * PAGE_SIZE;
        
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        
        // 只返回审核通过的帖子
        queryWrapper.eq("status", 1);
        
        // 根据筛选条件添加查询条件
        if ("recommended".equals(filter)) {
            // 推荐逻辑可以根据具体需求实现
        } else if ("latest".equals(filter)) {
            queryWrapper.orderByDesc("created_at");
        } else if ("popular".equals(filter)) {
            queryWrapper.orderByDesc("likes");
        } else if ("following".equals(filter)) {
            // 关注逻辑需要关联用户关注表
        }
        
        // 根据排序方式添加排序条件
        if ("time".equals(sort)) {
            queryWrapper.orderByDesc("created_at");
        } else if ("hot".equals(sort)) {
            queryWrapper.orderByDesc("likes");
        } else if ("reply".equals(sort)) {
            queryWrapper.orderByDesc("comments_count");
        }
        
        List<Post> posts = postMapper.selectList(queryWrapper.last("LIMIT " + PAGE_SIZE + " OFFSET " + offset));
        int total = postMapper.selectCount(queryWrapper).intValue();
        
        // 丰富帖子信息
        posts = posts.stream().map(post -> enrichPost(post, userId)).collect(Collectors.toList());
        
        // 转换为DTO对象
        List<PostDTO> postDTOs = posts.stream().map(this::convertToDTO).collect(Collectors.toList());
        
        // 设置是否关注作者
        if (userId != null) {
            for (PostDTO dto : postDTOs) {
                if (dto.getAuthor() != null) {
                    dto.getAuthor().setIsFollowing(
                        userFollowMapper.isFollowing(userId, dto.getAuthor().getId())
                    );
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("posts", postDTOs);
        result.put("hasMore", offset + posts.size() < total);
        result.put("total", total);
        
        return Result.success().setData(result);
    }
    
    @Override
    @Transactional
    public Result createPost(String content, String images, String tags, Integer userId) {
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(content);
        post.setImages(images);
        post.setTags(tags);
        
        postMapper.insert(post);
        
        // 查询完整信息
        Post createdPost = postMapper.selectById(post.getId());
        createdPost.setAuthor(userMapper.selectById(userId));
        
        Map<String, Object> result = new HashMap<>();
        result.put("post", createdPost);
        
        return Result.success().setData(result);
    }
    
    @Override
    @Transactional
    public Result updatePost(Integer id, String content, String tags, Integer userId) {
        Post post = postMapper.selectById(id);
        
        if (post == null) {
            return Result.error("帖子不存在");
        }
        
        if (!post.getUserId().equals(userId)) {
            return Result.error("没有权限编辑该帖子");
        }
        
        post.setContent(content);
        post.setTags(tags);
        
        postMapper.updateById(post);
        
        // 查询完整信息
        Post updatedPost = postMapper.selectById(id);
        updatedPost.setAuthor(userMapper.selectById(userId));
        
        Map<String, Object> result = new HashMap<>();
        result.put("post", updatedPost);
        
        return Result.success().setData(result);
    }
    
    @Override
    @Transactional
    public Result deletePost(Integer id, Integer userId) {
        Post post = postMapper.selectById(id);
        
        if (post == null) {
            return Result.error("帖子不存在");
        }
        
        if (!post.getUserId().equals(userId)) {
            return Result.error("没有权限删除该帖子");
        }
        
        postMapper.deleteById(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        
        return Result.success().setData(result);
    }
    
    @Override
    @Transactional
    public Result likePost(Integer id, Integer userId) {
        Post post = postMapper.selectById(id);
        
        if (post == null) {
            return Result.error("帖子不存在");
        }
        
        QueryWrapper<PostLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", id).eq("user_id", userId);
        
        PostLike existingLike = postLikeMapper.selectOne(queryWrapper);
        
        boolean isLiked;
        if (existingLike != null) {
            // 取消点赞
            postLikeMapper.deleteById(existingLike.getId());
            post.setLikes(post.getLikes() - 1);
            isLiked = false;
        } else {
            // 点赞
            PostLike postLike = new PostLike();
            postLike.setPostId(id);
            postLike.setUserId(userId);
            postLikeMapper.insert(postLike);
            post.setLikes(post.getLikes() + 1);
            isLiked = true;
        }
        
        postMapper.updateById(post);
        
        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", isLiked);
        result.put("likes", post.getLikes());
        
        return Result.success().setData(result);
    }
    
    @Override
    @Transactional
    public Result savePost(Integer id, Integer userId) {
        Post post = postMapper.selectById(id);
        
        if (post == null) {
            return Result.error("帖子不存在");
        }
        
        QueryWrapper<PostSave> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", id).eq("user_id", userId);
        
        PostSave existingSave = postSaveMapper.selectOne(queryWrapper);
        
        boolean isSaved;
        if (existingSave != null) {
            // 取消收藏
            postSaveMapper.deleteById(existingSave.getId());
            isSaved = false;
        } else {
            // 收藏
            PostSave postSave = new PostSave();
            postSave.setPostId(id);
            postSave.setUserId(userId);
            postSaveMapper.insert(postSave);
            isSaved = true;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("isSaved", isSaved);
        
        return Result.success().setData(result);
    }
    
    @Override
    public List<Post> enrichPosts(List<Post> posts, Integer userId) {
        return posts.stream().map(post -> enrichPost(post, userId)).collect(Collectors.toList());
    }
    
    @Override
    public Post enrichPost(Post post, Integer userId) {
        // 获取作者信息
        User author = userMapper.selectById(post.getUserId());
        post.setAuthor(author);
        
        // 设置用户名，便于前端直接访问
        if (author != null) {
            post.setUsername(author.getUsername());
        }

        // 设置当前用户是否点赞/收藏
        if (userId != null) {
            post.setIsLiked(postLikeMapper.isPostLikedByUser(post.getId(), userId));
            post.setIsSaved(postSaveMapper.isPostSavedByUser(post.getId(), userId));
        }
        
        return post;
    }
    
    /**
     * 将Post实体转换为PostDTO
     */
    private PostDTO convertToDTO(Post post) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setLikes(post.getLikes());
        dto.setCommentsCount(post.getCommentsCount());
        dto.setIsLiked(post.getIsLiked());
        dto.setIsSaved(post.getIsSaved());
        
        // 解析images字段
        if (post.getImages() != null && !post.getImages().isEmpty()) {
            try {
                List<String> images = objectMapper.readValue(post.getImages(), new TypeReference<List<String>>() {});
                dto.setImages(images);
            } catch (Exception e) {
                // 如果解析失败，设置为空列表
                dto.setImages(new ArrayList<>());
            }
        } else {
            dto.setImages(new ArrayList<>());
        }
        
        // 解析tags字段
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            try {
                List<String> tags = objectMapper.readValue(post.getTags(), new TypeReference<List<String>>() {});
                dto.setTags(tags);
            } catch (Exception e) {
                // 如果解析失败，设置为空列表
                dto.setTags(new ArrayList<>());
            }
        } else {
            dto.setTags(new ArrayList<>());
        }
        
        // 设置作者信息
        if (post.getAuthor() != null) {
            PostDTO.AuthorDTO authorDTO = new PostDTO.AuthorDTO();
            authorDTO.setId(post.getAuthor().getId());
            authorDTO.setName(post.getAuthor().getName());
            authorDTO.setUsername(post.getAuthor().getUsername());
            // isFollowing需要在调用此方法前设置
            authorDTO.setIsFollowing(false);
            dto.setAuthor(authorDTO);
        }
        
        return dto;
    }
}