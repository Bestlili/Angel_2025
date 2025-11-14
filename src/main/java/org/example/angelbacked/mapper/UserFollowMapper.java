package org.example.angelbacked.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.angelbacked.entity.User;
import org.example.angelbacked.entity.UserFollow;

import java.util.List;

public interface UserFollowMapper extends BaseMapper<UserFollow> {
    
    @Select("SELECT EXISTS(SELECT 1 FROM user_follow WHERE follower_id = #{followerId} AND following_id = #{followingId})")
    boolean isFollowing(@Param("followerId") Integer followerId, @Param("followingId") Integer followingId);
    
    @Select("SELECT u.* FROM user u INNER JOIN user_follow uf ON u.id = uf.following_id WHERE uf.follower_id = #{userId} LIMIT 10")
    List<User> selectFollowingUsers(@Param("userId") Integer userId);
    
    @Select("SELECT u.* FROM user u INNER JOIN user_follow uf ON u.id = uf.follower_id WHERE uf.following_id = #{userId} LIMIT 10")
    List<User> selectFollowerUsers(@Param("userId") Integer userId);
}