package org.example.angelbacked.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.angelbacked.entity.CommentLike;

public interface CommentLikeMapper extends BaseMapper<CommentLike> {
    
    @Select("SELECT COUNT(*) FROM comment_like WHERE comment_id = #{commentId}")
    int selectCountByCommentId(@Param("commentId") Integer commentId);
    
    @Select("SELECT EXISTS(SELECT 1 FROM comment_like WHERE comment_id = #{commentId} AND user_id = #{userId})")
    boolean isCommentLikedByUser(@Param("commentId") Integer commentId, @Param("userId") Integer userId);
}