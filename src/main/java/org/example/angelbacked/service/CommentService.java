package org.example.angelbacked.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.angelbacked.entity.Comment;
import org.example.angelbacked.util.Result;

import java.util.List;

public interface CommentService extends IService<Comment> {
    
    Result getComments(Integer postId);
    
    Result createComment(Integer postId, String content, Integer userId);
    
    Result likeComment(Integer id, Integer userId);
    
    List<Comment> enrichComments(List<Comment> comments);
}