package org.example.angelbacked.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.angelbacked.entity.PostLike;

public interface PostLikeMapper extends BaseMapper<PostLike> {
    
    @Select("SELECT COUNT(*) FROM post_like WHERE post_id = #{postId}")
    int selectCountByPostId(@Param("postId") Integer postId);
    
    @Select("SELECT EXISTS(SELECT 1 FROM post_like WHERE post_id = #{postId} AND user_id = #{userId})")
    boolean isPostLikedByUser(@Param("postId") Integer postId, @Param("userId") Integer userId);
}