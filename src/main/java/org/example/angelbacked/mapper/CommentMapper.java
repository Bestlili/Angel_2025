package org.example.angelbacked.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.angelbacked.entity.Comment;

import java.util.List;

public interface CommentMapper extends BaseMapper<Comment> {
    
    @Select("SELECT * FROM comment WHERE post_id = #{postId} ORDER BY created_at ASC")
    List<Comment> selectCommentsByPostId(@Param("postId") Integer postId);
}