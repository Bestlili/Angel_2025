package org.example.angelbacked.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.angelbacked.entity.PostSave;

public interface PostSaveMapper extends BaseMapper<PostSave> {
    
    @Select("SELECT EXISTS(SELECT 1 FROM post_save WHERE post_id = #{postId} AND user_id = #{userId})")
    boolean isPostSavedByUser(@Param("postId") Integer postId, @Param("userId") Integer userId);
}