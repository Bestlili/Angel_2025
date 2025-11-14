package org.example.angelbacked.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.angelbacked.entity.Post;
import org.example.angelbacked.util.Result;

import java.util.List;

public interface PostService extends IService<Post> {
    
    Result getPosts(int page, String filter, String sort, Integer userId);
    
    Result createPost(String content, String images, String tags, Integer userId);
    
    Result updatePost(Integer id, String content, String tags, Integer userId);
    
    Result deletePost(Integer id, Integer userId);
    
    Result likePost(Integer id, Integer userId);
    
    Result savePost(Integer id, Integer userId);
    
    List<Post> enrichPosts(List<Post> posts, Integer userId);
    
    Post enrichPost(Post post, Integer userId);
}