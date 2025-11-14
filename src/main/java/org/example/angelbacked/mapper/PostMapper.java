package org.example.angelbacked.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.angelbacked.entity.Post;

import java.util.List;

public interface PostMapper extends BaseMapper<Post> {
    
    @Select("SELECT * FROM post ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<Post> selectPostsByPage(@Param("limit") int limit, @Param("offset") int offset);
    
    @Select("SELECT COUNT(*) FROM post")
    int selectTotalPosts();
}