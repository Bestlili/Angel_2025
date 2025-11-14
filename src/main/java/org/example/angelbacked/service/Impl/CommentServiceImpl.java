package org.example.angelbacked.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.angelbacked.entity.Comment;
import org.example.angelbacked.entity.CommentLike;
import org.example.angelbacked.entity.Post;
import org.example.angelbacked.entity.User;
import org.example.angelbacked.mapper.CommentLikeMapper;
import org.example.angelbacked.mapper.CommentMapper;
import org.example.angelbacked.mapper.PostMapper;
import org.example.angelbacked.mapper.UserMapper;
import org.example.angelbacked.service.CommentService;
import org.example.angelbacked.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private CommentLikeMapper commentLikeMapper;
    
    @Autowired
    private PostMapper postMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public Result getComments(Integer postId) {
        List<Comment> comments = commentMapper.selectCommentsByPostId(postId);
        comments = enrichComments(comments);
        
        Map<String, Object> result = new HashMap<>();
        result.put("comments", comments);
        
        return Result.success().setData(result);
    }
    
    @Override
    @Transactional
    public Result createComment(Integer postId, String content, Integer userId) {
        Post post = postMapper.selectById(postId);
        
        if (post == null) {
            return Result.error("帖子不存在");
        }
        
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        
        commentMapper.insert(comment);
        
        // 更新帖子的评论数
        post.setCommentsCount(post.getCommentsCount() + 1);
        postMapper.updateById(post);
        
        // 查询完整信息
        Comment createdComment = commentMapper.selectById(comment.getId());
        createdComment.setAuthor(userMapper.selectById(userId));
        
        Map<String, Object> result = new HashMap<>();
        result.put("comment", createdComment);
        
        return Result.success().setData(result);
    }
    
    @Override
    @Transactional
    public Result likeComment(Integer id, Integer userId) {
        Comment comment = commentMapper.selectById(id);
        
        if (comment == null) {
            return Result.error("评论不存在");
        }
        
        // 检查是否已点赞
        boolean isLiked = commentLikeMapper.isCommentLikedByUser(id, userId);
        
        if (isLiked) {
            // 取消点赞
            commentLikeMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CommentLike>()
                    .eq("comment_id", id).eq("user_id", userId));
            comment.setLikes(comment.getLikes() - 1);
        } else {
            // 点赞
            CommentLike commentLike = new CommentLike();
            commentLike.setCommentId(id);
            commentLike.setUserId(userId);
            commentLikeMapper.insert(commentLike);
            comment.setLikes(comment.getLikes() + 1);
        }
        
        commentMapper.updateById(comment);
        
        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", !isLiked); // 状态已切换
        result.put("likes", comment.getLikes());
        
        return Result.success().setData(result);
    }
    
    @Override
    public List<Comment> enrichComments(List<Comment> comments) {
        return comments.stream().map(comment -> {
            User author = userMapper.selectById(comment.getUserId());
            comment.setAuthor(author);
            return comment;
        }).collect(Collectors.toList());
    }
}